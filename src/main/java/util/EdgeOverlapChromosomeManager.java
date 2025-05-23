package util;

import algorithm.GraphMatchingGA;
import algorithm.GraphMatchingGA.Chromosome;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;


/**
 * This class manages the chromosomes for a genetic algorithm that finds a mapping between two graphs
 * based on edge overlap.
 * @param <V> Type of vertices in the graphs
 */
public class EdgeOverlapChromosomeManager<V> implements GraphMatchingGA.ChromosomeManager<V> {
    private final Graph<V, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
    private final Graph<V, DefaultEdge> graph2 = new SimpleGraph<>(DefaultEdge.class);
    private final double targetFitness;
    private Random random = new Random();

    /**
     * Constructor to initialize the chromosome manager with two graphs and a target rate.
     * @param graph1 First graph
     * @param graph2 Second graph
     * @param targetRate Target rate for fitness calculation
     */
    public EdgeOverlapChromosomeManager(Graph<V, DefaultEdge> graph1, Graph<V, DefaultEdge> graph2, double targetRate) {
        Graphs.addGraph(this.graph1, graph1);
        Graphs.addGraph(this.graph2, graph2);
        this.targetFitness = Math.max(this.graph1.edgeSet().size(), this.graph2.edgeSet().size()) * targetRate;
    }

    /**
     * Set the random number generator for the algorithm.
     * @param random Random number generator
     */
    public void setRandom(Random random) { this.random = random; }

    @Override
    public Chromosome<V> randomChromosome(){
        List<V> vertices1 = new ArrayList<>(graph1.vertexSet());
        List<V> vertices2 = new ArrayList<>(graph2.vertexSet());
        Collections.shuffle(vertices2, random);
        Map<V, V> mapping = new HashMap<>();
        for (int j = 0; j < Math.min(vertices1.size(), vertices2.size()); j++) {
            mapping.put(vertices1.get(j), vertices2.get(j));
        }
        return new Chromosome<>(mapping);
    }

    @Override
    public double getTargetFitness(){
        return targetFitness;
    }

    /**
     * Calculate the fitness of a given mapping based on edge overlap.
     * @param mapping Mapping between vertices of the two graphs
     * @return Fitness value based on edge overlap
     */
    @Override
    public double calculateFitness(Map<V, V> mapping) {
        int edgeOverlap = 0;

        for (DefaultEdge edge : graph1.edgeSet()) {
            V source1 = graph1.getEdgeSource(edge);
            V target1 = graph1.getEdgeTarget(edge);

            if (mapping.containsKey(source1) && mapping.containsKey(target1)) {
                V source2 = mapping.get(source1);
                V target2 = mapping.get(target1);

                if (graph2.containsEdge(source2, target2)) {
                    edgeOverlap++;
                }
            }
        }

        return edgeOverlap;
    }
}