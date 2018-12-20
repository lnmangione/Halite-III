package genetics;

import java.util.HashMap;

/**
 * An Individual has a fitness value and a chromosome map (String -> Double)
 * Each entry in the chromosome maps a String gene name to a Double allele value
 */
public class Individual {
    private HashMap<String, Double> chromosome;
    private double fitness;

    public Individual(HashMap<String, Double> chromosome){
        this.chromosome = chromosome;
    }

    public HashMap<String, Double> getChromosome() {
        return chromosome;
    }

    public void setChromosome(HashMap<String, Double> chromosome) {
        this.chromosome = chromosome;
    }

    public Double getAllele(String gene){
        return chromosome.get(gene);
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}