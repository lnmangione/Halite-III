package pep;

import genetics.Population;

/**
 * Creates the initial population for the GA and writes it to XML
 */
public class InitializeGA {

    public static void main(String[] args) {
        GA.initializeGenes();
        Population initialPopulation = new Population(GA.POPULATION_SIZE, 1, GA.genes.values());
        PopulationXML.writeToXML("population.xml", initialPopulation, 0);
    }
}