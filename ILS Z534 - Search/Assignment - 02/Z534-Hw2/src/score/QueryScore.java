/**
 * 
 */
package score;

import static com.Constants.OUTPUT_FILE_DELIMITER;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author spukalay
 *
 */
public class QueryScore {

	public List<QueryTermsScore> queryTermsScoreList;
	public Set<String> relevantDocumentIDs;
	private String queryID;

	/**
	 * Creates object QueryScore for query with specified ID
	 * 
	 * @param queryID
	 *            Query ID
	 */
	public QueryScore(String queryID) {
		this.queryID = queryID;
		queryTermsScoreList = new ArrayList<QueryTermsScore>();
		relevantDocumentIDs = new HashSet<String>();
	}

	/**
	 * Add the QueryTermScores which holds the document score for each term of
	 * query.
	 * 
	 * @param queryTermScores
	 *            Object of QueryTermScore class
	 */
	public void addQueryTermScores(QueryTermsScore queryTermsScore) {
		queryTermsScoreList.add(queryTermsScore);
	}

	/**
	 * Calculate and returns the document score for specified document.
	 * 
	 * @param docNo
	 *            document number
	 * @return score of document w.r.t to the query
	 */
	public double getDocumentScore(String docNo) {
		double documentScoreForQuery = 0;

		for (QueryTermsScore queryTermsScore : queryTermsScoreList) {
			documentScoreForQuery += queryTermsScore.getDocumentScore(docNo);
		}

		return documentScoreForQuery;
	}

	/**
	 * Add the specified document number to the relevant document number for the
	 * query.
	 * 
	 * @param docNO
	 *            document number
	 */
	public void addRelevantDocument(String docNO) {
		relevantDocumentIDs.add(docNO);
	}

	/**
	 * Returns the map containing document id to it's score w.r.t query.
	 * 
	 * @return map containing document id to it's score w.r.t query
	 */
	public Map<String, Double> getDocumentIdToScoreMap() {
		Map<String, Double> documentScoreMap = new HashMap<String, Double>();

		for (String docNo : relevantDocumentIDs) {
			double score = getDocumentScore(docNo);
			documentScoreMap.put(docNo, score);
		}

		return documentScoreMap;
	}

	/**
	 * This functions writes the top 1000 result document for the query in
	 * 'treceval' format to the specified output file.
	 * 
	 * @param outputFilePath
	 *            output file path
	 * 
	 * @throws IOException
	 */
	public void writeTop1KResults(String outputFilePath) throws IOException {
		Map<String, Double> documentIdToScoreMap = getDocumentIdToScoreMap();

		File outputFile = new File(outputFilePath);
		FileWriter fileWriter = new FileWriter(outputFile, true);

		AtomicInteger documentRank = new AtomicInteger(1);
		documentIdToScoreMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.limit(1000).forEachOrdered(entry -> {
					try {
						fileWriter.append(this.queryID);
						fileWriter.append(OUTPUT_FILE_DELIMITER + "Q0");
						fileWriter.append(OUTPUT_FILE_DELIMITER + entry.getKey());
						fileWriter.append(OUTPUT_FILE_DELIMITER + documentRank.getAndIncrement());
						fileWriter.append(OUTPUT_FILE_DELIMITER + entry.getValue());
						fileWriter.append(OUTPUT_FILE_DELIMITER + "run-1 \n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		fileWriter.flush();
		fileWriter.close();
	}

}
