/**
 * 
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.FactoryUtils;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

/**
 * @author spukalay
 *
 */
public class AuthorRank {
	private static final String GRAPH_PATH = "C:/Users/user/Desktop/Search/assignment3/assignment3/author.net";

	private Graph<Integer, String> graph = new UndirectedSparseGraph<>();

	public static void main(String[] args) {
		try {
			new AuthorRank().rankAuthors();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void rankAuthors() throws IOException {
		PajekNetReader netReader = new PajekNetReader<>(FactoryUtils.instantiateFactory(Object.class));
		netReader.load(GRAPH_PATH, graph);

		PageRank<Integer, String> pageRank = new PageRank<>(graph, Double.valueOf(0.85));
		pageRank.setMaxIterations(1000);
		pageRank.evaluate();

		Map<Object, Double> result = new HashMap<Object, Double>();
		for (Integer v : graph.getVertices()) {
			result.put(netReader.getVertexLabeller().transform(v), pageRank.getVertexScore(v));
		}

		System.out.println("---------------");
		System.out.println("Author ID");
		System.out.println("---------------");
		result.entrySet().stream().sorted(Map.Entry.<Object, Double> comparingByValue().reversed())
				.limit(10)
				.forEach(e -> {
					System.out.println(e.getKey());
				});
	}
}
