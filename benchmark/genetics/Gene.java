package genetics;

/**
 * Defines a gene. Genes have a name, a minimum possible value, and a maximum possible value
 */
public class Gene {

    private String name;
    private double min;
    private double max;

    /**
     * Defines a gene with name 'name' and max value 'max' and min value 'min'
     */
    public Gene(String name, double min, double max){
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public String getName(){
        return name;
    }

    /**
     * @return Generates a random allele for this gene within the proper range
     */
    double generateAllele(){
        double range = max - min;
        return truncate(min + (Math.random() * range));
    }

    /**
     * Mutates the given value according to the min and max of this gene
     * Mutated value = old value +/- up to 10% of gene range
     */
    double mutate(double value){
        double change = Math.random() * ((max - min) / 7);
        int sign = Math.random() >= 0.5 ? 1 : -1;
        value += sign * change;
        return truncate(clamp(value));
    }

    private double clamp(double value){
        return Math.max(min, Math.min(max, value));
    }

    private double truncate(double value){
        return Math.floor(value * 1000) / 1000;
    }
}