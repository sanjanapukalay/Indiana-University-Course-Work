/**
 * 
 */
package com;

import static com.Constants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.queryparser.classic.ParseException;

import score.QueryScore;

/**
 * @author spukalay
 *
 */
public class SearchTRECtopics {

	public static void main(String[] args) {
		try {
			new SearchTRECtopics().searchTopics("MyAlgo");
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void searchTopics(String algorithm) throws IOException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ParseException {

		// Output file paths
		String shortFilePath = TOPIC_OUTPUT_PATH + "/" + algorithm + "ShortQuery.txt";
		String longFilePath = TOPIC_OUTPUT_PATH + "/" + algorithm + "LongQuery.txt";
		CommonUtil.cleanDirectory(shortFilePath, longFilePath);

		// Read the queries from TREC topics
		TrecTopicsReader trecTopicReader = new TrecTopicsReader();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(TOPICS_FILE));
		QualityQuery[] qualityQueries = trecTopicReader.readQueries(bufferedReader);

		// Get calculate query score each document
		for (QualityQuery qualityQuery : qualityQueries) {

			String queryID = qualityQuery.getQueryID();

			// Queries for TITLE (Short Query)
			{
				String cleanedTitleQuery = cleanTitleQueryString(qualityQuery.getValue(TITLE));
				QueryScore titleQueryScore = EasySearch.calculateRelevenceScore(cleanedTitleQuery, queryID);
				titleQueryScore.writeTop1KResults(shortFilePath);
			}

			// Queries for DESC (Long Query)
			{
				String cleanedDescQuery = cleanDescQueryString(qualityQuery.getValue(DESC));
				QueryScore descQueryScore = EasySearch.calculateRelevenceScore(cleanedDescQuery, queryID);
				descQueryScore.writeTop1KResults(longFilePath);
			}

		}
		bufferedReader.close();
		System.out.println("Queries executed successfully.");
	}

	/**
	 * This function removes unnecessary content from the title query string.
	 * For example, query string contains "Topic: [queryText]" removes "Topic:"
	 * from the query and return.
	 * 
	 * @param queryString
	 *            Query String to be cleaned
	 * @return Cleaned title query string
	 */
	public static String cleanTitleQueryString(String queryString) {
		String cleanedQuery = null;

		int colonIndex = queryString.indexOf(":");
		cleanedQuery = queryString.substring(colonIndex + 1, queryString.length());
		return cleanedQuery;
	}

	/**
	 * This function removes unnecessary content from the description query
	 * string.
	 * 
	 * @param queryString
	 *            Query String to be cleaned
	 * @return Cleaned description query string
	 */
	public static String cleanDescQueryString(String queryString) {
		String cleanedQuery = null;

		int smryIndex = queryString.indexOf("<smry>");
		if (smryIndex != -1) {
			cleanedQuery = queryString.substring(0, smryIndex);
		}
		return cleanedQuery;
	}
}
