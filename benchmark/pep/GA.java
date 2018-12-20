package pep;

import genetics.*;
import java.util.HashMap;

/**
 * The Genetic Algorithm generates the offspring of the current population
 * It saves the old generation to XML, and writes the new generation to XML
 *
 * GA also defines the genes that will be used for this bot
 */
public class GA {
    static int POPULATION_SIZE = 20;
    static HashMap<String, Gene> genes = new HashMap<>();

    /**
     * Initializes genes with the proper names and min/max values
     */
    static void initializeGenes(){
        genes.put("depositorAmount",     new Gene("depositorAmount",     0.5, 1.0));
        genes.put("depositorLateAmount", new Gene("depositorLateAmount", 0.01, 0.7));
        genes.put("depositorLateBuffer", new Gene("depositorLateBuffer", 9.0, 15.0));
//        genes.put("percentBH",           new Gene("percentBH",           0.1, 0.9));
        genes.put("scarceMult",          new Gene("scarceMult",          0.3, 1.5));
        genes.put("scanDistanceBH",      new Gene("scanDistanceBH",      1.0, 10.0));
        genes.put("scanDistanceTH",      new Gene("scanDistanceTH",      1.0, 20.0));
        genes.put("thresholdTH",         new Gene("thresholdTH",         0.0, 0.2));
        genes.put("scarceLocalDist",     new Gene("scarceLocalDist",     1.0, 5.1));
        genes.put("scarceLocalMult",     new Gene("scarceLocalMult",     0.3, 1.5));
    }

    /**
     * Reads the current population from XML, reproduces, then writes offspring to XML
     */
    public static void main(String[] args) {
        initializeGenes();
        Population population = PopulationXML.readXML("population.xml");
        int oldGen = population.getGeneration();
        PopulationXML.writeToXML("population" + oldGen + ".xml", population, 0);
        Population newPopulation = Reproduction.reproduce(population, genes);
        PopulationXML.writeToXML("population.xml", newPopulation, 0);
    }
}