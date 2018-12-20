package genetics;

import java.util.Collection;
import java.util.HashMap;

/**
 * A Population maintains an array of individuals, and a generation number
 */
public class Population {

    private int generation;
    private Individual[] individuals;

    /**
     * Creates an empty population of size 'size'
     */
    public Population(int size, int generation){
        individuals = new Individual[size];
        this.generation = generation;
    }

    /**
     * Generates a population of size 'size'
     * Uses 'genes' to generate a chromosome for each individual
     */
    public Population(int size, int generation, Collection<Gene> genes){
        individuals = new Individual[size];
        this.generation = generation;
        for (int i = 0; i < size(); i++) {
            Individual newIndividual = new Individual(generateChromosome(genes));
            setIndividual(i, newIndividual);
        }
    }

    private HashMap<String, Double> generateChromosome(Collection<Gene> genes){
        HashMap<String, Double> chromosome = new HashMap<>();
        for (Gene gene : genes){
            chromosome.put(gene.getName(), gene.generateAllele());
        }
        return chromosome;
    }

    public Individual[] getIndividuals() {
        return individuals;
    }

    public Individual getIndividual(int index) {
        return individuals[index];
    }

    public void setIndividual(int index, Individual individual) {
        individuals[index] = individual;
    }

    public Individual getFittest() {
        Individual fittest = individuals[0];
        for (int i = 1; i < size(); i++) {
            if (fittest.getFitness() < getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    public double getAverageFitness() {
        double total = 0.0;
        for (Individual ind : individuals){
            total += ind.getFitness();
        }
        double average = total / individuals.length;
        average = Math.floor(average * 1000) / 1000;
        return average;
    }

    public int getGeneration() {
        return generation;
    }

    public int size() {
        return individuals.length;
    }
}