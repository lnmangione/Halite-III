package pep;

import genetics.Individual;
import genetics.Population;
import hlt.Log;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;

/**
 * Writes/reads populations to/from XML
 */
public class PopulationXML {

    private static final String directory = "C:\\Users\\Pep\\Documents\\HaliteIII_519\\benchmark\\";
    private static DocumentBuilder documentBuilder;
    static int currentIndividual;

    private static void initializeDocBuilder(){
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void writeToXML(String fileName, Population population, int currentId){
        try {
            if (documentBuilder == null){
                initializeDocBuilder();
            }
            Document document = documentBuilder.newDocument();

            // root element
            Element root = document.createElement("population");
            document.appendChild(root);
            root.setAttribute("generation", Integer.toString(population.getGeneration()));
            root.setAttribute("currentId", Integer.toString(currentId));
            root.setAttribute("avgFitness", Double.toString(population.getAverageFitness()));
            root.setAttribute("fittest", Double.toString(population.getFittest().getFitness()));

            Individual[] individuals = population.getIndividuals();
            for (int i = 0; i < individuals.length; i++){
                Individual individual = individuals[i];
                Element indElt = document.createElement("individual");
                root.appendChild(indElt);
                indElt.setAttribute("id", Integer.toString(i));
                indElt.setAttribute("fitness", Double.toString(individual.getFitness()));
                Element genes = document.createElement("genes");
                indElt.appendChild(genes);
                for (HashMap.Entry<String, Double> entry : individual.getChromosome().entrySet()){
                    genes.setAttribute(entry.getKey(), entry.getValue().toString());
                }
            }

            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(directory + fileName));
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            Log.log(e.getMessage());
        }
    }

    /**
     * Saves the current individual in static field 'currentIndividual'
     * @return The population stored in the given XML file
     */
    public static Population readXML(String fileName){
        Population population = null;

        try {
            if (documentBuilder == null){
                initializeDocBuilder();
            }
            File file = new File(directory + fileName);
            Document document = documentBuilder.parse(file);

            Element root = document.getDocumentElement();
            int generation = Integer.parseInt((root.getAttribute("generation")));
            population = new Population(GA.POPULATION_SIZE, generation);
            int currentId = Integer.parseInt((root.getAttribute("currentId")));
            currentIndividual = currentId;

            NodeList indNodes = document.getElementsByTagName("individual");
            for (int i = 0; i < indNodes.getLength(); i++){
                HashMap<String, Double> chromosome = new HashMap<>();
                Node indNode = indNodes.item(i);
                Node genesNode = indNode.getFirstChild().getNextSibling();
                NamedNodeMap genes = genesNode.getAttributes();
                for (int iGene = 0; iGene < genes.getLength(); iGene++){
                    Node gene = genes.item(iGene);
                    chromosome.put(gene.getNodeName(), Double.parseDouble(gene.getNodeValue()));
                }
                double fitness = Double.parseDouble(indNode.getAttributes().getNamedItem("fitness").getNodeValue());
                Individual individual = new Individual(chromosome);
                individual.setFitness(fitness);
                population.setIndividual(i, individual);
            }
        } catch (Exception e) {
            Log.log(e.getMessage());
        }
        return population;
    }
}