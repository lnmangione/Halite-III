package genetics;

import java.util.HashMap;
import java.util.Map;

/**
 * The Reproduction class defines how a parent population reproduces
 *
 * Offspring of a parent population are generated according to the fitness of the individuals in the parent population
 * The genes of children are generated using crossing over and mutation
 */
public class Reproduction {

    private static final double CROSSOVER_RATE = 0.5;
    private static final double MUTATION_RATE = 0.025;
    private static final double NEW_MUTATION_RATE = 0.025;
    private static final int TOURNAMENT_SIZE = 4;
    private static final boolean ELITISM = true;

    /**
     * @return The offspring of the input population
     */
    public static Population reproduce(Population population, Map<String, Gene> genes) {
        Population offspring = new Population(population.size(), population.getGeneration() + 1);

        // Keep our best individual
        if (ELITISM) {
            offspring.setIndividual(0, population.getFittest());
            offspring.getIndividual(0).setFitness(0.0);
        }
        int elitismOffset = ELITISM ? 1 : 0;

        // Cross the individuals
        for (int i = elitismOffset; i < population.size(); i++) {
            Individual indiv1 = tournamentSelection(population);
            Individual indiv2 = tournamentSelection(population);
            offspring.setIndividual(i, crossover(indiv1, indiv2));
        }

        // Mutate new individuals
        for (int i = elitismOffset; i < offspring.size(); i++) {
            mutate(offspring.getIndividual(i), genes);
        }

        return offspring;
    }

    private static Individual crossover(Individual indA, Individual indB) {
        HashMap<String, Double> chromosome = new HashMap<>();
        for (String gene : indA.getChromosome().keySet()){
            if (Math.random() <= CROSSOVER_RATE) {
                chromosome.put(gene, indA.getAllele(gene));
            } else {
                chromosome.put(gene, indB.getAllele(gene));
            }
        }
        return new Individual(chromosome);
    }

    private static void mutate(Individual individual, Map<String, Gene> genes) {
        HashMap<String, Double> chromosome = new HashMap<>();
        for (HashMap.Entry<String, Double> allele : individual.getChromosome().entrySet()){
            Double value = allele.getValue();
            if (Math.random() <= MUTATION_RATE){
                Gene gene = genes.get(allele.getKey());
                value =  gene.mutate(value);
            } else if (Math.random() <= NEW_MUTATION_RATE){
                Gene gene = genes.get(allele.getKey());
                value =  gene.generateAllele();
            }
            chromosome.put(allele.getKey(), value);
        }
        individual.setChromosome(chromosome);
    }

    /**
     * Creates a tournament population and returns the fittest individual in that population
     */
    private static Individual tournamentSelection(Population population) {
        Population tournament = new Population(TOURNAMENT_SIZE, 0);
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomId = (int) (Math.random() * population.size());
            tournament.setIndividual(i, population.getIndividual(randomId));
        }
        return tournament.getFittest();
    }
}
