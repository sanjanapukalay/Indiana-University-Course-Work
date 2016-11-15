/**
 * 
 */
package com;

import static com.Constants.DESC;
import static com.Constants.INDEX_PATH;
import static com.Constants.TITLE;
import static com.Constants.TOPICS_FILE;
import static com.Constants.TOPIC_OUTPUT_PATH;
import static com.Constants.OUTPUT_FILE_DELIMITER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * @author spukalay
 *
 */
public class CompareAlgorithms {

	/**
	 * Main Function To execute Task-3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CompareAlgorithms compare = new CompareAlgorithms();
		try {
			compare.searchQueries(new ClassicSimilarity(), "VectorSpace");
			compare.searchQueries(new BM25Similarity(), "BM25");
			compare.searchQueries(new LMDirichletSimilarity(), "LMDirichlet");
			compare.searchQueries(new LMJelinekMercerSimilarity((float) 0.7), "LMJelinek");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function uses the similarity object specified and retrieves the Top
	 * 1000 docs for each query for Trec 51-1000 test set. Further writes th
	 * results in trec_eval file format.
	 * 
	 * @param similarity
	 *            Object of similarity to be used
	 * @param algorithm
	 *            algorithm name
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public void searchQueries(Similarity similarity, String algorithm) throws IOException, ParseException {

		// Output file paths
		String shortFilePath = TOPIC_OUTPUT_PATH + "/" + algorithm + "ShortQuery.txt";
		String longFilePath = TOPIC_OUTPUT_PATH + "/" + algorithm + "LongQuery.txt";
		CommonUtil.cleanDirectory(shortFilePath, longFilePath);

		// Read the queries from TREC topics
		TrecTopicsReader trecTopicReader = new TrecTopicsReader();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(TOPICS_FILE));
		QualityQuery[] qualityQueries = trecTopicReader.readQueries(bufferedReader);

		// Create searcher
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		indexSearcher.setSimilarity(similarity);

		QueryParser queryParser = new QueryParser("TEXT", analyzer);

		for (int queryIndex = 0; queryIndex < qualityQueries.length; queryIndex++) {
			QualityQuery qualityQuery = qualityQueries[queryIndex];
			String queryID = qualityQuery.getQueryID();

			// Queries for TITLE (Short Query)
			{
				String cleanedTitleQuery = SearchTRECtopics.cleanTitleQueryString(qualityQuery.getValue(TITLE));
				Query titleQuery = queryParser.parse(QueryParserUtil.escape(cleanedTitleQuery));
				TopDocs topDocs = indexSearcher.search(titleQuery, 1000);
				writeDocsToFile(topDocs, indexSearcher, queryID, shortFilePath);
			}

			// Queries for DESC (Long Query)
			{
				String cleanedDescQuery = SearchTRECtopics.cleanDescQueryString(qualityQuery.getValue(DESC));
				Query descQuery = queryParser.parse(QueryParserUtil.escape(cleanedDescQuery));
				TopDocs topDocs = indexSearcher.search(descQuery, 1000);
				writeDocsToFile(topDocs, indexSearcher, queryID, longFilePath);
			}

		}
		indexReader.close();
		bufferedReader.close();
		System.out.println("Queries executed successfully For : " + algorithm);
	}

	/**
	 * This function writes the top 1000 documents in the specified TopDocs
	 * object to the OutputFileSpecified according to trec_eval format.
	 * 
	 * @param topDocs
	 *            TopDocs object to process
	 * @param indexSearcher
	 *            IndexSearcher object
	 * @param queryID
	 *            ID of query being processed
	 * @param outputFilePath
	 *            Output file path to write results
	 * 
	 * @throws IOException
	 */
	public static void writeDocsToFile(TopDocs topDocs, IndexSearcher indexSearcher, String queryID,
			String outputFilePath) throws IOException {
		File outputFile = new File(outputFilePath);

		FileWriter fileWriter = new FileWriter(outputFile, true);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;

		for (int docIndex = 0; docIndex < scoreDocs.length; docIndex++) {
			ScoreDoc scoreDoc = scoreDocs[docIndex];
			String docNo = indexSearcher.doc(scoreDoc.doc).get("DOCNO");

			fileWriter.append(queryID);
			fileWriter.append(OUTPUT_FILE_DELIMITER + "Q0");
			fileWriter.append(OUTPUT_FILE_DELIMITER + docNo);
			fileWriter.append(OUTPUT_FILE_DELIMITER + (docIndex + 1));
			fileWriter.append(OUTPUT_FILE_DELIMITER + scoreDoc.score);
			fileWriter.append(OUTPUT_FILE_DELIMITER + "run-1 \n");
		}
		fileWriter.flush();
		fileWriter.close();
	}
}
