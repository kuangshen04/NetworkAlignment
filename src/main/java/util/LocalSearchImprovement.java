package util;

import algorithm.GraphMatchingGA;
import algorithm.GraphMatchingGA.Chromosome;

import java.util.*;

public class LocalSearchImprovement<V> implements GraphMatchingGA.ImprovementOperator<V> {
    private final int interval;
    private Random random = new Random();

    public LocalSearchImprovement(int interval) {
        this.interval = interval;
    }

    /**
     * Set the random number generator for the algorithm.
     * @param random Random number generator
     */
    public void setRandom(Random random) { this.random = random; }

    @Override
    public boolean isImprovementNeeded(Chromosome<V> chromosome, GraphMatchingGA.ChromosomeManager<V> chromosomeManager, int generation, int stagnantGenerations) {
        return (generation % interval == 0) || (chromosome.getFitness() >= chromosomeManager.getTargetFitness() * 0.9);
    }

    /**
     * Perform local search improvement on the given chromosome.
     * @param chromosome Chromosome to be improved
     * @param chromosomeManager Chromosome manager for fitness calculation
     * @return Improved chromosome
     */
    @Override
    public Chromosome<V> improve(Chromosome<V> chromosome, GraphMatchingGA.ChromosomeManager<V> chromosomeManager) {
        Map<V, V> mapping = chromosome.getMapping();
        double bestFitness = chromosome.getFitness();
        List<V> keys = new ArrayList<>(mapping.keySet());
        Collections.shuffle(keys, random);

        while (!keys.isEmpty()) {
            V first = keys.removeFirst();
            V bestSwap = first;

            for (V second : keys) {
                if (first.equals(second)) continue;

                Map<V, V> tempMapping = new HashMap<>(mapping);
                tempMapping.put(first, mapping.get(second));
                tempMapping.put(second, mapping.get(first));

                double after = chromosomeManager.calculateFitness(tempMapping);

                if (after > bestFitness) {
                    bestFitness = after;
                    bestSwap = second;
                }
            }


            if (!bestSwap.equals(first)) {
                V tmp = mapping.get(first);
                mapping.put(first, mapping.get(bestSwap));
                mapping.put(bestSwap, tmp);
                keys.remove(bestSwap);
            }
        }

        Chromosome<V> improvedChromosome = new Chromosome<>(mapping);
        improvedChromosome.setFitness(bestFitness);
        return improvedChromosome;
    }
}
