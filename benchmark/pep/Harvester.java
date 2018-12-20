package pep;

import hlt.*;

import java.util.ArrayList;
import java.util.Random;

public class Harvester extends Behavior {
    private static Random rng = new Random(System.nanoTime());
    private InnerHarvester innerHarvester;

    Harvester(){
        //boolean isBH = Math.random() <= Parameters.getPercentBH();
        //innerHarvester = isBH ? new BestHarvester() : new ThresholdHarvester();
        innerHarvester = new ThresholdHarvester();
    }

    @Override
    public boolean meetsCriteria(Ship ship, Type currentBehavior, Game game) {
        if (currentBehavior == Type.SCRUBBER){
            return true;
        }
        return game.gameMap.at(ship).hasStructure() && game.gameMap.at(ship).structure.owner.equals(game.me.id);
    }

    @Override
    public Position getTarget(Ship ship, Game game) {
        Position target = innerHarvester.getTarget(ship, game);
        return (target != null) ? target : getAdjacent(ship, game);
    }

    @Override
    public boolean minimizesCost() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.HARVESTER;
    }

    /**
     * @return A good position to explore adjacent this ship
     */
    private Position getAdjacent(Ship ship, Game game){
        GameMap gameMap = game.gameMap;
        Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
        Position target = ship.position.directionalOffset(randomDirection);

        for (Position pos : ship.position.adjacentPositions()) {
            if (gameMap.at(pos).halite > 1.3 * gameMap.at(target).halite) {
                target = pos;
            }
        }
        return target;
    }

    /**
     * Subclasses of InnerHarvester allow for slightly different harvesting behavior
     */
    private abstract class InnerHarvester {
        int scanDistance;

        abstract Position getTarget(Ship ship, Game game);
    }

    /**
     * A ThresholdHarvester targets the closest cell with halite amount above some threshold
     */
    private class ThresholdHarvester extends InnerHarvester{

        private ThresholdHarvester(){
            scanDistance = Parameters.getScanDistanceTH();
        }

        @Override
        public Position getTarget(Ship ship, Game game) {
            int scarceHalite = Parameters.getScarceHalite(game, ship);

            GameMap gameMap = game.gameMap;
            if (gameMap.at(ship.position).halite > scarceHalite) {
                return ship.position;
            }
            int threshold = Parameters.getThresholdTH(game, ship);
            MapCell nearestCell = gameMap.nearestCellWithHalite(ship.position, scanDistance, threshold);
            return (nearestCell == null) ? null : nearestCell.position;
        }
    }

    /**
     * A BestHarvester targets the cell with the most halite within a given radius
     */
    private class BestHarvester extends InnerHarvester {
        private BestHarvester(){
            scanDistance = Parameters.getScanDistanceBH();
        }

        @Override
        public Position getTarget(Ship ship, Game game) {
            Log.log("Ship: " + ship.id + ". Pos: " + ship.position);
            GameMap gameMap = game.gameMap;
            if (gameMap.at(ship.position).halite > Parameters.getScarceHalite(game, ship)) {
                return ship.position;
            }
            Position dense = gameMap.densestCell(ship.position, scanDistance).position;
            Log.log("Ship: " + ship.id + ". Densest: " + dense);
            return dense;
        }
    }
}