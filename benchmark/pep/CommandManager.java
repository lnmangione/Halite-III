package pep;

import hlt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static HashMap<EntityId, Command> commands = new HashMap<>();
    private static Player me;
    private static Game game;
    private static GameMap gameMap;

    public static void updateCommands(Game game){
        commands = new HashMap<>();
        CommandManager.me = game.me;
        CommandManager.game = game;
        CommandManager.gameMap = game.gameMap;

        addMoves(getTargets());
        addSpawns();
    }

    private static void addSpawns(){
        if (me.ships.size() == 0){
            commands.put(me.shipyard.id, Command.spawnShip());
            me.halite -= Constants.SHIP_COST;
            gameMap.at(me.shipyard).markUnsafe(new Ship(me.id, EntityId.NONE, me.shipyard.position, 0));
        }
    }

    /**
     * @return A map specifying the target position of each ship
     */
    private static Map<EntityId, Position> getTargets() {
        Map<EntityId, Position> targets = new HashMap<>();
        for (final Ship ship : me.ships.values()) {
            if (!commands.containsKey(ship.id)){
                Behavior behavior = BehaviorManager.behaviorForShip(ship);
                targets.put(ship.id, behavior.getTarget(ship, game));
            }
        }
        return targets;
    }

    /**
     * Adds moves to the command queue based on target positions
     */
    private static void addMoves(Map<EntityId, Position> targets){
        // add moves for ships
        for (Map.Entry<EntityId, Position> entry : targets.entrySet()){
            Ship ship = me.ships.get(entry.getKey());
            Position target = entry.getValue();
            if (ship.position.equals(target) || !gameMap.canMove(ship)){
                commands.put(ship.id, ship.stayStill());
            } else {
                Behavior behavior = BehaviorManager.behaviorForShip(ship);
                Direction dir = gameMap.naiveNavigate(ship, target, behavior.minimizesCost());
                commands.put(ship.id, ship.move(dir));
                gameMap.at(ship.position).markSafe();
            }
        }
    }

    public static void logCommands(){
        for (Command cmd : commands.values()) {
            Log.log(cmd.command);
        }
    }

    public static Collection<Command> getCommands(){
        return commands.values();
    }
}