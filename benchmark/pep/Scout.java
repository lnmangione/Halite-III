package pep;

import hlt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scout extends Behavior {

    private static ArrayList<Position> heavyCells = new ArrayList<>(); // list of all heavy cells on map
    private static HashMap<Position, Integer> pockets = new HashMap<>(); // pocket centers w/ local halite as value
    private static int scouts = -1;

    // TODO: evolve: heavy cell halite amount
    public static void findHeavyCells(Game game){
        heavyCells = game.gameMap.getPositionsAbove(650);

        // filter out redundant cells in 2 cell radius
        for (int i = 0; i < heavyCells.size(); i++){
            Position heavy = heavyCells.get(i);

            Ripple scanRipple = new Ripple(game.gameMap, heavy, 2);
            while (scanRipple.hasNext()){
                Position adjacent = scanRipple.next();
                if (!heavy.equals(adjacent) && heavyCells.contains(adjacent)){
                    heavyCells.remove(adjacent);
                    i = Math.max(i - 1, 0);
                }
            }
        }
    }

    // TODO: evolve: numShipsForScout, pocketRadius, closestEnemyDist
    // TODO: optimization: stop updating pockets at some point in the game
    public static void updatePockets(Game game){
        if (game.me.ships.size() < 8 || game.turnNumber % 5 != 0){
            return;
        }
        pockets = new HashMap<>();

        // remove from consideration heavy cells that we've already harvested near
        for (int i = 0; i < heavyCells.size(); i++){
            Position closestDropoff = game.gameMap.closestDropoff(game.me, heavyCells.get(i));
            if (game.gameMap.calculateDistance(closestDropoff, heavyCells.get(i)) < Parameters.getMinDropoffDistance()){
                heavyCells.remove(i--);
            }
        }

        // good pockets are isolated:
        for (Position pos : heavyCells){
            int localHalite = game.gameMap.haliteWithinDistance(pos, 5);
            int closestShipDist = Integer.MAX_VALUE;

            // get distance to closest ship
            for (Ship ship : getAllShips(game).values()){
                int shipDist = game.gameMap.calculateDistance(ship.position, pos);
                if (shipDist < closestShipDist){
                    closestShipDist = shipDist;
                }
            }
            //only consider isolated pockets
            if (closestShipDist >= 17){
                pockets.put(pos, localHalite);
            }
        }
    }

    private static HashMap<EntityId, Ship> getAllShips(Game game){
        HashMap<EntityId, Ship> ships = new HashMap<>();
        for (Player player : game.players){
            ships.putAll(player.ships);
        }
        return ships;
    }

    private static Position getBestPocket(){
        Map.Entry<Position, Integer> pocket = null;
        for (Map.Entry<Position, Integer> entry : pockets.entrySet()){
            if (pocket == null){
                pocket = entry;
            } else {
                if (entry.getValue() > pocket.getValue()){
                    pocket = entry;
                }
            }
        }
        return pocket == null ? null : pocket.getKey();
    }

    private Position target;

    public Scout(){
        scouts++;
        this.target = getBestPocket();
        if (scouts > 0){
            CommandManager.creatingScoutDropoff = true;
        }
    }

    @Override
    public boolean meetsCriteria(Ship ship, Type currentBehavior, Game game) {
        //TODO: temporary - only make scouts on large maps
        if(game.gameMap.width < 40){
            return false;
        }

        if (currentBehavior == Type.SCOUT){
            Behavior behavior = BehaviorManager.behaviorForShip(ship);
            if (!ship.position.equals(behavior.getTarget(ship, game))){
                return true;
            }
            return !game.gameMap.at(ship.position).hasStructure();
        }
        if (currentBehavior == Type.HARVESTER && ship.getHalite() < 100 && scouts < 3){
            return getBestPocket() != null;
        }
        return false;
    }

    @Override
    public Position getTarget(Ship ship, Game game) {
        return target;
    }

    @Override
    public Type getType() {
        return Type.SCOUT;
    }

    @Override
    public boolean minimizesCost() {
        return true;
    }
}
