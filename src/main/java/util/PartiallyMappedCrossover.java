package util;

import algorithm.GraphMatchingGA;
import algorithm.GraphMatchingGA.Chromosome;

import java.util.*;

/**
 * This class implements the Partially Mapped Crossover (PMX) operator for a genetic algorithm.
 * It is used to generate offspring from two parent chromosomes by partially mapping their genes.
 * @param <V> Type of vertices in the graphs
 */
public class PartiallyMappedCrossover<V> implements GraphMatchingGA.CrossoverOperator<V> {

    private final int tournamentSize;
    private final double crossoverRate;
    private Random random = new Random();

    /**
     * Constructor to initialize the crossover operator with tournament size and crossover rate
     * @param tournamentSize Size of the tournament for selection
     * @param crossoverRate Probability of crossover
     */
    public PartiallyMappedCrossover(int tournamentSize, double crossoverRate) {
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
    }


    /**
     * Set the random number generator for the algorithm.
     * @param random Random number generator
     */
    public void setRandom(Random random) { this.random = random; }

    /**
     * Crossover method to generate offspring from the selected parents
     * @param population Current population
     * @param offspringSize Number of offspring to generate
     * @return List of offspring chromosomes
     */
    @Override
    public List<Chromosome<V>> crossover(List<Chromosome<V>> population, int offspringSize) {
        population = tournamentSelect(population, offspringSize);
        List<Chromosome<V>> offspring = new ArrayList<>();

        // Ensure the population size is even
        int size = population.size();
        if (size % 2 != 0) {
            offspring.add(population.getLast());
            size--;
        }

        for (int i = 0; i < size; i += 2) {
            Chromosome<V> parent1 = population.get(i);
            Chromosome<V> parent2 = population.get(i + 1);

            if (random.nextDouble() < crossoverRate) {
                Map<V, V> child1Mapping = pmx(parent1.getMapping(), parent2.getMapping());
                Map<V, V> child2Mapping = pmx(parent2.getMapping(), parent1.getMapping());

                offspring.add(new Chromosome<>(child1Mapping));
                offspring.add(new Chromosome<>(child2Mapping));
            } else {
                offspring.add(new Chromosome<>(new HashMap<>(parent1.getMapping())));
                offspring.add(new Chromosome<>(new HashMap<>(parent2.getMapping())));
            }
        }

        return offspring;
    }

    /**
     * Tournament selection for selecting parents
     * @param population Current population
     * @param offspringSize Number of offspring to select
     * @return Selected chromosomes
     */
    private List<Chromosome<V>> tournamentSelect(List<Chromosome<V>> population, int offspringSize) {
        List<Chromosome<V>> selected = new ArrayList<>();

        while (selected.size() < offspringSize) {
            Chromosome<V> best = population.get(random.nextInt(population.size()));
            for (int i = 1; i < tournamentSize; i++) {
                Chromosome<V> candidate = population.get(random.nextInt(population.size()));
                if (candidate.getFitness() > best.getFitness()) best = candidate;
            }

            selected.add(new Chromosome<>(new HashMap<>(best.getMapping())));
        }

        return selected;
    }

    /**
     * Partially Mapped Crossover (PMX) implementation
     * @param parent1 First parent mapping
     * @param parent2 Second parent mapping
     * @return Child mapping
     */
    private Map<V, V> pmx(Map<V, V> parent1, Map<V, V> parent2) {
        Map<V, V> child = new HashMap<>();
        List<V> keys = new ArrayList<>(parent1.keySet());

        // Randomly select two crossover points
        int point1 = random.nextInt(keys.size());
        int point2 = random.nextInt(keys.size());
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        // Copy the segment from parent1 to child
        for (int i = point1; i <= point2; i++) {
            V key = keys.get(i);
            child.put(key, parent1.get(key));
        }

        // Fill the remaining positions with values from parent2
        for (V key : keys) {
            if (!child.containsKey(key)) {
                V value = parent2.get(key);

                // Check for conflicts
                while (child.containsValue(value)) {
                    V originalKey = findKey(parent1, value);
                    value = parent2.get(originalKey);
                }

                child.put(key, value);
            }
        }

        return child;
    }

    /**
     * Helper method to find the key in a map that corresponds to a given value
     * @param map The map to search
     * @param value The value to find
     * @return The key corresponding to the value, or null if not found
     */
    private V findKey(Map<V, V> map, V value) {
        for (Map.Entry<V, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}