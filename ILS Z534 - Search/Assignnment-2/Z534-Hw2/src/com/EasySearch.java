/**
 * 
 */
package com;

import static com.Constants.INDEX_PATH;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import score.QueryScore;
import score.QueryTermsScore;

/**
 * @author spukalay
 *
 */
class EasySearch {
	public static void main(String[] args) {
		try {
			String queryString = "New York";
			String queryID = "1";

			QueryScore queryScore = calculateRelevenceScore(queryString, queryID);

			/*
			 * Task-1 [3]: Calculating relevance score for query w.r.t. to
			 * documents
			 */
			for (String docNo : queryScore.relevantDocumentIDs) {
				System.out.println("DocumentID: " + docNo + "     score:" + queryScore.getDocumentScore(docNo));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculates relevance score of terms w.r.t. documents.
	 * 
	 * @param queryString
	 * @param queryID
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static QueryScore calculateRelevenceScore(String queryString, String queryID)
			throws IOException, ParseException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		// Create searcher
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);

		// Get the pre-processed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		Set<Term> queryTerms = new LinkedHashSet<Term>();

		query.createWeight(indexSearcher, false).extractTerms(queryTerms);

		int documentCount = reader.maxDoc();
		QueryScore queryScore = new QueryScore(queryID);

		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		ClassicSimilarity similarity = new ClassicSimilarity();

		for (Term term : queryTerms) {
			int documentFrequencyForTerm = reader.docFreq(term);
			QueryTermsScore queryTermsScore = new QueryTermsScore(term.text());

			for (LeafReaderContext leafContext : leafContexts) {
				
				PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT",
						new BytesRef(term.text()));
				
				if (postingsEnum != null) {
					while ((postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

						int termFrequencyInDocument = postingsEnum.freq();
						int documentID = postingsEnum.docID() + leafContext.docBase;
						String docNO = indexSearcher.doc(documentID).get("DOCNO");

						long docLength = leafContext.reader().getNormValues("TEXT").get(postingsEnum.docID());
						float normDocLength = similarity.decodeNormValue(docLength);

						double documentLength = 1 / (normDocLength * normDocLength);
						queryTermsScore.addDocumentScore(docNO, calculateTFIDFScoreForTerm(termFrequencyInDocument,
								documentLength, documentFrequencyForTerm, documentCount));
						queryScore.addRelevantDocument(docNO);
					}
				}
			}
			queryScore.addQueryTermScores(queryTermsScore);
		}
		reader.close();
		return queryScore;
	}

	/**
	 * Calculates TF-IDF score of the specified term
	 * 
	 * @param termFrequencyInDocument
	 * @param docLength
	 * @param documentFrequencyForTerm
	 * @param documentCount
	 * @return
	 */
	private static double calculateTFIDFScoreForTerm(double termFrequencyInDocument, double docLength,
			double documentFrequencyForTerm, double documentCount) {
		double tFScore = (termFrequencyInDocument / docLength);
		double iDFScore = Math.log(1 + (documentCount / documentFrequencyForTerm));
		return tFScore * iDFScore;
	}
}
