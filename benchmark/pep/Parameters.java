package pep;

import genetics.*;
import hlt.*;

import java.util.HashMap;

public class Parameters {
    private static HashMap<String, Double> parameters = new HashMap<>();
    private static Population population;
    private static int currentId; // the ID of the current individual we are using the genes of
    private static int initialHalite;

    //TODO: minShips needs to be evolved
    //TODO: depositor late amount set manually
    public static void evolvedDefaults(){
        parameters.put("depositorAmount", 0.997); // amount of halite at which depositor should return
        parameters.put("depositorLateAmount", 0.040); // amount of halite in late game at which depositor should return
        parameters.put("depositorLateBuffer", 12.0); // buffer for turn at which a depositor should return late game
        parameters.put("percentBH", 0.11); // percentage of harvesters to make BestHarvester's
        parameters.put("scanDistanceBH", 6.566); // the distance scanned by a BestHarvester
        parameters.put("scanDistanceTH", 15.335); // the distance scanned by a ThresholdHarvester
        parameters.put("thresholdTH", 0.086); // the halite threshold a TH seeks
        parameters.put("minShips", 15.0); // the absolute minimum number of ships to have
        parameters.put("maxSpawnTurn", 0.667); // the max turn at which to spawn
        parameters.put("minDropoffDistance", 12.3); // the min distance between dropoffs
        parameters.put("dropoffDensity", 8.0); // the min density at which to create dropoff
        parameters.put("dropoffLateTurn", 0.915); // turn at which not to create dropoff
        parameters.put("scarceMult", 0.579); // multiplier - amount of halite below which is considered scarce/not worth harvesting
        parameters.put("scaledShipsMult", 0.32); // multiplier - how many ships to build based on map halite
        parameters.put("scarceLocalDist", 3.0); // local distance to scan to determine scarce halite
        parameters.put("scarceLocalMult", 0.67); // multiplier - average local halite -> scarce threshold
    }

    // ===== Methods for instantiating parameters from an individual =====

    public static void populateFromXML(){
        population = PopulationXML.readXML("population.xml");
        currentId = PopulationXML.currentIndividual;
        parameters.putAll(population.getIndividual(currentId).getChromosome());
    }

    public static void saveInitialState(Game game){
        initialHalite = game.gameMap.getTotalHalite();
    }

    public static void writeFitness(Game game){
        double gameScore = ((double)game.me.halite) / initialHalite;
        double prevFitness = population.getIndividual(currentId).getFitness();
        double fitness = prevFitness + gameScore;
        fitness = Math.floor(fitness * 1000) / 1000;
        population.getIndividual(currentId).setFitness(fitness);
        currentId = (currentId + 1) % GA.POPULATION_SIZE;
        PopulationXML.writeToXML("population.xml", population, currentId);
    }

    /**
     * Writes to XML the fitness of this individual, fitness based on early game performance
     */
    public static void writeFitnessEarly(Game game){
        double averageScore = 0.0; // average score across other players
        for (Player player : game.players){
            if (!player.id.equals(game.me.id)){
                averageScore += (double)(player.halite + player.ships.size() * Constants.SHIP_COST);
            }
        }
        averageScore = averageScore / (game.players.size() - 1);

        // calculate increase in fitness from this game
        double myScore = (double)(game.me.halite + game.me.ships.size() * Constants.SHIP_COST);
        double diff = myScore - averageScore;
        double gameFitness = diff > 0 ? 1.0 : -1.0;

        // increment fitness and write to XML
        double prevFitness = population.getIndividual(currentId).getFitness();
        double fitness = prevFitness + gameFitness;
        fitness = Math.floor(fitness * 1000) / 1000;
        population.getIndividual(currentId).setFitness(fitness);
        currentId = (currentId + 1) % GA.POPULATION_SIZE;
        PopulationXML.writeToXML("population.xml", population, currentId);
    }

    // ===== Methods for accessing parameters =====

    /**
     * @return Amount of halite below which is considered scarce/not worth harvesting
     */
    static int getScarceHalite(Game game, Ship ship){
        int globalScarce = (int)(parameters.get("scarceMult") * game.gameMap.getAverageHalite());
        int localDistance = parameters.get("scarceLocalDist").intValue();
        int localCells = Ripple.positionsWithinDist(localDistance);
        double localAvgHalite = game.gameMap.haliteWithinDistance(ship.position, localDistance) / (double) localCells;
        int localScarce = (int)(parameters.get("scarceLocalMult") * localAvgHalite);
        return Math.max(globalScarce, localScarce);
    }

    static double getPercentBH(){
        return parameters.get("percentBH");
    }

    /**
     * Based on the absolute minimum number of ships that we always need
     * Also based on a scaled minimum proportional to total map halite
     *
     * @return The minimum number of ships that should be built
     */
    static int getMinShips(Game game){
        int absoluteMin = parameters.get("minShips").intValue();
        double fullShips = (game.gameMap.getTotalHalite() / (double) Constants.MAX_HALITE) / (double) game.players.size();
        int scaledMin = (int)(parameters.get("scaledShipsMult") * fullShips);
        return Math.max(absoluteMin, scaledMin);
    }

    static int getMaxSpawnTurn(){
        return (int)(parameters.get("maxSpawnTurn") * Constants.MAX_TURNS);
    }

    static int getMinDropoffDistance(){
        return parameters.get("minDropoffDistance").intValue();
    }

    static double getDropoffDensity(){
        return parameters.get("dropoffDensity") * Constants.MAX_HALITE;
    }

    static int getDropoffLateTurn(){
        return (int)(parameters.get("dropoffLateTurn") * Constants.MAX_TURNS);
    }

    /**
     * @return The number of turns this ship needs to deposit late game
     */
    static int getDepositorLateTurns(Game game, Ship ship){
//        if (game.players.size() == 2){
//            return getBlockerTurns();
//        }
        GameMap gameMap = game.gameMap;
        int dist = gameMap.calculateDistance(ship.position, gameMap.closestDropoff(game.me, ship.position));
        return parameters.get("depositorLateBuffer").intValue() + dist;
    }

    static int getDepositorLateAmount(){
        return (int)(parameters.get("depositorLateAmount") * Constants.MAX_HALITE);
    }

    static int getDepositorAmount(){
        return (int)(parameters.get("depositorAmount") * Constants.MAX_HALITE);
    }

    static int getScanDistanceBH(){
        return parameters.get("scanDistanceBH").intValue();
    }

    static int getScanDistanceTH(){
        return parameters.get("scanDistanceTH").intValue();
    }

    static int getThresholdTH(Game game, Ship ship){
        return getScarceHalite(game, ship) + (int)(parameters.get("thresholdTH") * Constants.MAX_HALITE);
    }
}