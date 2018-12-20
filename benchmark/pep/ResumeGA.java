package pep;

import genetics.Individual;
import genetics.Population;

/**
 * Resumes the GA using the current population in population.xml
 * Resets the fitness levels for all individuals in current population
 */
public class ResumeGA {

    public static void main(String[] args) {
        Population population = PopulationXML.readXML("population.xml");
        for (Individual ind : population.getIndividuals()){
            ind.setFitness(0.0);
        }
        PopulationXML.writeToXML("population.xml", population, 0);
    }
}