package pep;

import hlt.*;

import java.util.ArrayList;
import java.util.Random;

public class Harvester extends Behavior {
    private static Random rng = new Random(System.nanoTime());
    private InnerHarvester innerHarvester;

    Harvester(){
        boolean isBH = Math.random() <= Parameters.getPercentBH();
        innerHarvester = isBH ? new BestHarvester() : new ThresholdHarvester();
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
        // make room for new spawns early game
        if (game.turnNumber < 20 && game.gameMap.at(ship).hasStructure()){
            return getFreeAdjacent(ship, game);
        }
        Position target = innerHarvester.getTarget(ship, game);
        return (target != null) ? target : getAdjacent(ship, game);
    }

    //TODO: tune parameters for attacking
    public Position getAttackMove(Ship ship, Game game){
        if (game.turnNumber > 0.67 * Constants.MAX_TURNS){
            ArrayList<Ship> nearby = game.gameMap.shipsInRadius(ship.position, 3);
            for (Ship neighbor : game.gameMap.shipsInRadius(ship.position, 1)){
                if (!neighbor.owner.equals(game.me.id)){
                    int allies = myShips(nearby, game);
                    if (allies >= 0.67 * nearby.size() && ship.getHalite() * 2 < neighbor.getHalite() && neighbor.getHalite() >= 400){
                        return neighbor.position;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return The number of ships in "ships" that I own
     */
    private int myShips(ArrayList<Ship> ships, Game game){
        int mine = 0;
        for (Ship ship : ships){
            if (ship.owner.equals(game.me.id)){
                mine++;
            }
        }
        return mine;
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
     * @return The best free position to explore adjacent this ship
     */
    private Position getFreeAdjacent(Ship ship, Game game){
        GameMap gameMap = game.gameMap;
        Position target = ship.position;
        for (Position pos : ship.position.adjacentPositions()) {
            if (!gameMap.at(pos).isOccupied() && gameMap.at(pos).halite > gameMap.at(target).halite) {
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