package util;

import algorithm.GraphMatchingGA;
import algorithm.GraphMatchingGA.Chromosome;

import java.util.*;

/**
 * This class implements a mutation operator for a genetic algorithm that swaps two vertices in the mapping.
 * The mutation rate increases with the number of stagnant generations.
 * @param <V> Type of vertices in the graphs
 */
public class SwapMutation<V> implements GraphMatchingGA.MutationOperator<V> {
    private final double baseMutationRate;
    private final double stagnantRate;
    private Random random = new Random();

    /**
     * Constructor to initialize the mutation operator with base mutation rate and stagnant rate.
     * @param baseMutationRate Base mutation rate
     * @param stagnantRate Rate of increase in mutation rate with stagnant generations
     */
    public SwapMutation(double baseMutationRate, double stagnantRate) {
        this.baseMutationRate = baseMutationRate;
        this.stagnantRate = stagnantRate;
    }

    /**
     * Set the random number generator for the algorithm.
     * @param random Random number generator
     */
    public void setRandom(Random random) { this.random = random; }

    private double getMutationRate(int stagnantGenerations){
        return Math.min(1.0, baseMutationRate * (1 + stagnantGenerations * stagnantRate));
    }

    /**
     * Mutate the population by swapping two vertices in the mapping of each chromosome.
     * The mutation rate is determined by the number of stagnant generations.
     * @param population Current population of chromosomes
     * @param stagnantGenerations Number of stagnant generations
     */
    @Override
    public void mutate(List<Chromosome<V>> population, int stagnantGenerations) {
        var mutationRate = getMutationRate(stagnantGenerations);

        for (var chromosome : population) {
            if (random.nextDouble() < mutationRate) {
                Map<V, V> mapping = chromosome.getMapping();
                List<V> keys = new ArrayList<>(mapping.keySet());

                if (keys.size() >= 2) {
                    int index1 = random.nextInt(keys.size());
                    int index2 = random.nextInt(keys.size());

                    if (index1 != index2) {
                        V key1 = keys.get(index1);
                        V key2 = keys.get(index2);

                        V temp = mapping.get(key1);
                        mapping.put(key1, mapping.get(key2));
                        mapping.put(key2, temp);

                        chromosome.setEvaluated(false);
                    }
                }
            }
        }
    }
}
