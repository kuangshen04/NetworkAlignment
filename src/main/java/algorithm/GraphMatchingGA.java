package algorithm;

import java.util.*;

/**
 * This class implements a genetic algorithm for finding a mapping between two graphs.
 * It uses a population of chromosomes, each representing a potential mapping.
 * The algorithm evolves the population through selection, crossover, and mutation.
 * @param <V> Type of vertices in the graphs
 */
public class GraphMatchingGA<V> {
    private final ChromosomeManager<V> chromosomeManager;
    private final CrossoverOperator<V> crossoverOperator;
    private final MutationOperator<V> mutationOperator;
    private final ImprovementOperator<V> improvementOperator;

    private int populationSize = 100;
    private int elitismCount = 5;
    private int maxGenerations = 1000;
    private double resetRate = 0.3;
    private int catastropheThreshold = 100;
    private Random random = new Random();

    /**
     * Constructor to initialize the genetic algorithm with chromosome manager, crossover and mutation operators.
     * @param chromosomeManager Chromosome manager for generating and evaluating chromosomes.
     * @param crossoverOperator Crossover operator for generating offspring.
     * @param mutationOperator Mutation operator for mutating chromosomes.
     */
    public GraphMatchingGA(ChromosomeManager<V> chromosomeManager,
                           CrossoverOperator<V> crossoverOperator,
                           MutationOperator<V> mutationOperator,
                           ImprovementOperator<V> improvementOperator) {
        this.chromosomeManager = chromosomeManager;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.improvementOperator = improvementOperator;
    }

    /**
     * Set the size of the population.
     * @param size Size of the population
     */
    public void setPopulationSize(int size) { this.populationSize = size; }

    /**
     * Set the number of elite chromosomes to carry over to the next generation.
     * @param count Number of elite chromosomes
     */
    public void setElitismCount(int count) { this.elitismCount = count; }

    /**
     * Set the maximum number of generations to run the algorithm.
     * @param generations Maximum number of generations
     */
    public void setMaxGenerations(int generations) { this.maxGenerations = generations; }

    /**
     * Set the rate at which to reset part of the population during a catastrophe.
     * @param resetRate Rate of population reset
     */
    public void setResetRate(double resetRate) { this.resetRate = resetRate; }

    /**
     * Set the threshold for triggering a catastrophe.
     * @param catastropheThreshold Number of generations without improvement before triggering a catastrophe
     */
    public void setCatastropheThreshold(int catastropheThreshold) { this.catastropheThreshold = catastropheThreshold; }

    /**
     * Set the random number generator for the algorithm.
     * @param random Random number generator
     */
    public void setRandom(Random random) {
        this.random = random;
        this.chromosomeManager.setRandom(random);
        this.crossoverOperator.setRandom(random);
        this.mutationOperator.setRandom(random);
        this.improvementOperator.setRandom(random);
    }

    /**
     * Find the best mapping using a genetic algorithm.
     * @return The best mapping found.
     */
    public Map<V, V> findBestMapping() {
        List<Chromosome<V>> population = initializePopulation();
        evaluatePopulation(population);

        Chromosome<V> globalBest = population.getFirst().copy();
        int stagnantGenerations = 0;


        for (int generation = 0; generation < maxGenerations; generation++) {
            // Check for stagnation and trigger catastrophe if needed
            if (stagnantGenerations >= catastropheThreshold) {
                System.out.println("Catastrophe triggered. Reinitializing part of the population.");

                int resetCount = (int) (populationSize * resetRate);
                for (int i = 0; i < resetCount; i++) {
                    population.set(random.nextInt(populationSize), chromosomeManager.randomChromosome());
                }

                // Keep the global best chromosome
                population.set(random.nextInt(populationSize), globalBest.copy());

                evaluatePopulation(population);
                stagnantGenerations = 0;
            }

            List<Chromosome<V>> offspring = crossoverOperator.crossover(population, populationSize - elitismCount);

            mutationOperator.mutate(offspring, stagnantGenerations);

            // keep the best chromosomes from the current population
            if (elitismCount > 0) {
                for (int i = 0; i < elitismCount; i++) {
                    offspring.add(population.get(i));
                }
            }

            population = offspring;

            evaluatePopulation(population);

            Chromosome<V> currentBest = population.getFirst();

            // Perform improvement if needed
            if (improvementOperator.isImprovementNeeded(currentBest, chromosomeManager, generation, stagnantGenerations)) {
                Chromosome<V> improved = improvementOperator.improve(currentBest, chromosomeManager);
                if (improved.getFitness() > currentBest.getFitness()) {
                    currentBest = improved;
                    population.set(0, improved);
                    System.out.println("Improved fitness: " + improved.getFitness());
                }
            }

            System.out.println("Generation " + generation + ": Current Best fitness = " + currentBest.getFitness() + " / " + chromosomeManager.getTargetFitness());

            // Check for stagnation
            if (currentBest.getFitness() > globalBest.getFitness()) {
                globalBest = currentBest.copy();
                stagnantGenerations = 0;
            } else {
                stagnantGenerations++;
            }

            // Check for early stopping
            if (globalBest.getFitness() >= chromosomeManager.getTargetFitness()) {
                System.out.println("Target fitness reached. Early stopping.");
                break;
            }

        }


        return globalBest.getMapping();
    }

    /**
     * Initialize the population with random chromosomes.
     * @return A list of randomly generated chromosomes.
     */
    private List<Chromosome<V>> initializePopulation() {
        List<Chromosome<V>> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeManager.randomChromosome());
        }

        return population;
    }

    /**
     * Evaluate the fitness of the population and sort them.
     * @param population The current population of chromosomes.
     */
    private void evaluatePopulation(List<Chromosome<V>> population) {
        for (Chromosome<V> chromosome : population) {
            if (!chromosome.isEvaluated()) {
                chromosome.setFitness(chromosomeManager.calculateFitness(chromosome.getMapping()));
            }
        }
        Collections.sort(population);
    }

    /**
     * Chromosome class representing a mapping between vertices.
     * @param <V> Type of the vertices in the graph.
     */
    public static class Chromosome<V> implements Comparable<Chromosome<V>> {
        private final Map<V, V> mapping;
        private double fitness;
        private boolean evaluated = false;

        public Chromosome(Map<V, V> mapping) {
            this.mapping = new HashMap<>(mapping);
        }

        public Map<V, V> getMapping() { return mapping; }
        public double getFitness() {
            if (!evaluated) {
                throw new IllegalStateException("Fitness not evaluated yet.");
            }
            return fitness;
        }
        public void setFitness(double fitness) {
            this.fitness = fitness;
            this.evaluated = true;
        }
        public boolean isEvaluated() { return evaluated; }
        public void setEvaluated(boolean evaluated) { this.evaluated = evaluated; }

        @Override
        public int compareTo(Chromosome<V> other) {
            return Double.compare(other.fitness, this.fitness); // 降序排序
        }

        public Chromosome<V> copy() {
            var copy = new Chromosome<>(new HashMap<>(this.mapping));
            copy.setFitness(this.fitness);
            copy.setEvaluated(this.evaluated);
            return copy;
        }
    }

    /**
     * Chromosome manager interface for the genetic algorithm.
     * @param <V> Type of the vertices in the graph.
     */
    public interface ChromosomeManager<V> {
        void setRandom(Random random);
        /**
         * Generate a random chromosome.
         * @return Randomly generated chromosome.
         */
        Chromosome<V> randomChromosome();
        /**
         * Get the target fitness for the algorithm.
         * @return Target fitness value.
         */
        double getTargetFitness();
        /**
         * Calculate the fitness of a given mapping.
         * @param mapping Mapping to evaluate.
         * @return Fitness value of the mapping.
         */
        double calculateFitness(Map<V, V> mapping);
    }

    /**
     * Crossover operator interface for the genetic algorithm.
     * @param <V> Type of the vertices in the graph.
     */
    public interface CrossoverOperator<V> {
        void setRandom(Random random);
        /**
         * Crossover method to generate offspring from the selected parents.
         * @param population Current population
         * @param offspringSize Number of offspring to generate
         * @return List of offspring chromosomes.
         */
        List<Chromosome<V>> crossover(List<Chromosome<V>> population, int offspringSize);
    }

    /**
     * Mutation operator interface for the genetic algorithm.
     * @param <V> Type of the vertices in the graph.
     */
    public interface MutationOperator<V> {
        void setRandom(Random random);
        /**
         * Mutate the population of chromosomes.
         * @param population The current population of chromosomes.
         * @param stagnantGenerations The number of generations since the last improvement.
         */
        void mutate(List<Chromosome<V>> population, int stagnantGenerations);
    }

    /**
     * Improvement operator interface for the genetic algorithm.
     * @param <V> Type of the vertices in the graph.
     */
    public interface ImprovementOperator<V> {
        void setRandom(Random random);
        /**
         * Check if improvement is needed based on the current generation and stagnant generations.
         * @param chromosome The current chromosome.
         * @param chromosomeManager The chromosome manager for fitness calculation.
         * @param generation The current generation number.
         * @param stagnantGenerations The number of generations since the last improvement.
         * @return True if improvement is needed, false otherwise.
         */
        boolean isImprovementNeeded(Chromosome<V> chromosome, ChromosomeManager<V> chromosomeManager, int generation, int stagnantGenerations);
        /**
         * Perform improvement on the given chromosome.
         * @param chromosome The current chromosome.
         * @param chromosomeManager The chromosome manager for fitness calculation.
         * @return The improved chromosome.
         */
        Chromosome<V> improve(Chromosome<V> chromosome, ChromosomeManager<V> chromosomeManager);
    }
}
