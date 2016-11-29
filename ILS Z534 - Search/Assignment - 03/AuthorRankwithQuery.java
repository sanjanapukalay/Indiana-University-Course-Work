

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

/**
 * @author spukalay
 *
 */
public class AuthorRankwithQuery {
	private static final String INDEX_PATH = "C:/Users/user/Desktop/Search/assignment3/assignment3/author_index";
	private static final String GRAPH_PATH = "C:/Users/user/Desktop/Search/assignment3/assignment3/author.net";

	private Graph<Integer, String> graph = new UndirectedSparseGraph<>();
	private Map<String, String> authorMap = new HashMap<>();
	private String[] queries = { "Data Mining", "Information Retrieval" };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new AuthorRankwithQuery().rankAuthorWithQuery();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void rankAuthorWithQuery() throws IOException, ParseException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		indexSearcher.setSimilarity(new BM25Similarity());

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("content", analyzer);

		for (String q : queries) {
			Query query = parser.parse(q);
			TopScoreDocCollector collector = TopScoreDocCollector.create(300, true);
			indexSearcher.search(query, collector);
			TopDocs docs = collector.topDocs();

			Map<String, Float> tempRankingMap = new HashMap<String, Float>();
			float scoreSum = 0f;
			for (ScoreDoc doc : docs.scoreDocs) {
				Document document = indexSearcher.doc(doc.doc);
				String authorName = document.getField("authorName").stringValue();
				String authorId = document.getField("authorid").stringValue();
				authorMap.put(authorId, authorName);

				scoreSum += doc.score;
				if (!tempRankingMap.containsKey(authorId)) {
					tempRankingMap.put(authorId, doc.score);
				} else {
					tempRankingMap.put(authorId, tempRankingMap.get(authorId) + doc.score);
				}
			}

			Map<String, Float> rankingScoreMap = new HashMap<String, Float>();
			rankingScoreMap.putAll(tempRankingMap);

			// Normalize probabilities
			for (Entry<String, Float> entry : tempRankingMap.entrySet()) {
				rankingScoreMap.put(entry.getKey(), entry.getValue() / scoreSum);
			}

			PajekNetReader netReader = new PajekNetReader<>(FactoryUtils.instantiateFactory(Object.class));
			netReader.load(GRAPH_PATH, graph);

			Transformer<Integer, Double> vertex_prior = new Transformer<Integer, Double>() {
				@Override
				public Double transform(Integer v) {
					if (rankingScoreMap.containsKey(netReader.getVertexLabeller().transform(v))) {
						return (double) rankingScoreMap.get(netReader.getVertexLabeller().transform(v));
					} else {
						return 0d;
					}

				}
			};
			PageRankWithPriors<Integer, String> pageRankPriors = new PageRankWithPriors<>(graph, vertex_prior, 0.7);
			pageRankPriors.setMaxIterations(1000);
			pageRankPriors.evaluate();

			Map<Object, Double> result = new HashMap<Object, Double>();
			for (Integer v : graph.getVertices()) {
				result.put(netReader.getVertexLabeller().transform(v), pageRankPriors.getVertexScore(v));
			}
			System.out.println("---------------");
			System.out.println(q);
			System.out.println("Author ID -> Author Name");
			System.out.println("---------------");
			
			result.entrySet().stream().sorted(Map.Entry.<Object, Double> comparingByValue().reversed()).limit(10)
					.forEach(e -> {
						System.out.println(e.getKey() + " -> " + authorMap.get(e.getKey()));
					});
		}
		reader.close();
	}
}
