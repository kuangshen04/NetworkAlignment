import algorithm.GraphMatchingGA;
import util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

public class Main {
    private static final Random rand = new Random();

    public static void main(String[] args) {
        Graph<String, DefaultEdge> graph1 = generateRandomUndirectedGraph(15, 30);
        Graph<String, DefaultEdge> graph2 = generateRandomUndirectedGraph(15, 30);

        GraphMatchingGA<String> ga = new GraphMatchingGA<>(
                new EdgeOverlapChromosomeManager<>(graph1, graph2, 0.8),
                new PartiallyMappedCrossover<>(3,0.85),
                new SwapMutation<>(0.15, 0.5),
                new LocalSearchImprovement<>(10)
        );

        ga.setPopulationSize(50);
        ga.setElitismCount(5);
        ga.setMaxGenerations(100);
        ga.setResetRate(0.5);
        ga.setCatastropheThreshold(20);
        ga.setRandom(rand);

        long startTime = System.currentTimeMillis();

        Map<String, String> bestMapping = ga.findBestMapping();

        System.out.println("Best mapping found:");
        for (Map.Entry<String, String> entry : bestMapping.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime - startTime));
    }


    /**
     * Generates a random undirected graph with the specified number of vertices and edges.
     *
     * @param numVertices Number of vertices in the graph
     * @param numEdges    Number of edges in the graph
     * @return A random undirected graph
     */
    public static Graph<String, DefaultEdge> generateRandomUndirectedGraph(int numVertices, int numEdges) {
        if (numEdges > numVertices * (numVertices - 1) / 2) {
            throw new IllegalArgumentException("The number of edges exceeds the maximum possible edges for the given number of vertices.");
        }

        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        List<String> vertices = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            String v = "v" + i;
            graph.addVertex(v);
            vertices.add(v);
        }

        Set<String> existingEdges = new HashSet<>();
        int edgeCount = 0;

        while (edgeCount < numEdges) {
            int i = rand.nextInt(numVertices);
            int j = rand.nextInt(numVertices);
            if (i == j) continue;

            String u = vertices.get(i);
            String v = vertices.get(j);

            String edgeKey = u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u;

            if (!existingEdges.contains(edgeKey)) {
                graph.addEdge(u, v);
                existingEdges.add(edgeKey);
                edgeCount++;
            }
        }

        return graph;
    }
}
