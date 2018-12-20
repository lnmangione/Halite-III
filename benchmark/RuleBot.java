import hlt.*;

import java.util.*;

/**
 * This benchmark bot has basic navigation optimizations (ships don't collide with spawns or deadlock)
 * Ships harvest from the best adjacent cell, and deposit when approximately full
 */
public class RuleBot {

    private static Random rng;
    private static Game game;
    private static Player me;
    private static GameMap gameMap;
    private static Map<EntityId, Status> statuses = new HashMap<>();

    private enum Status {
        EXPLORING, RETURNING
    }

    public static void main(final String[] args) {
        rng = new Random(System.nanoTime());
        game = new Game();
        game.ready("RuleBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId);

        for (;;) {
            game.updateFrame();
            me = game.me;
            gameMap = game.gameMap;
            Map<EntityId, Command> commands = new HashMap<>();

            updateStatuses();
            addSpawns(commands);
            addMoves(getTargets(), commands);
            // logCommands(commands);
            game.endTurn(commands.values());
        }
    }

    private static void logCommands(Map<EntityId, Command> commands){
        for (Command cmd : commands.values()) {
            Log.log(cmd.command);
        }
    }

    /**
     * Update the status of each ship
     */
    private static void updateStatuses() {
        for (final Ship ship : me.ships.values()) {
            if (!statuses.containsKey(ship.id)){
                statuses.put(ship.id, Status.EXPLORING);
            } else if (statuses.get(ship.id) == Status.EXPLORING && ship.getHalite() > 0.9 * Constants.MAX_HALITE){
                statuses.put(ship.id, Status.RETURNING);
            } else if (statuses.get(ship.id) == Status.RETURNING && ship.position.equals(me.shipyard.position)){
                statuses.put(ship.id, Status.EXPLORING);
            } else if (game.turnNumber > 0.9 * Constants.MAX_TURNS && ship.getHalite() > 0.3 * Constants.MAX_HALITE){
                statuses.put(ship.id, Status.RETURNING);
            }
        }
    }

    /**
     * @return A map specifying the target position of each ship
     */
    private static Map<EntityId, Position> getTargets() {
        Map<EntityId, Position> targets = new HashMap<>();

        for (final Ship ship : me.ships.values()) {
            Position target = ship.position;

            if (statuses.get(ship.id) == Status.RETURNING) {
                target = me.shipyard.position;
            } else if (statuses.get(ship.id) == Status.EXPLORING && gameMap.at(ship.position).halite < Constants.MAX_HALITE / 10) {
                final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                target = ship.position.directionalOffset(randomDirection);
                for (Position pos : ship.position.adjacentPositions()) {
                    if (gameMap.at(pos).halite > 1.3 * gameMap.at(target).halite) {
                        target = pos;
                    }
                }
            }
            targets.put(ship.id, target);
        }
        return targets;
    }

    /**
     * Adds moves to the command queue based on target positions
     */
    private static void addMoves(Map<EntityId, Position> targets, Map<EntityId, Command> commands){
        // add moves for ships
        for (Map.Entry<EntityId, Position> entry : targets.entrySet()){
            Ship ship = me.ships.get(entry.getKey());
            Position target = entry.getValue();
            if (ship.position.equals(target) || !gameMap.canMove(ship)){
                commands.put(ship.id, ship.stayStill());
            } else {
                Direction dir = gameMap.naiveNavigate(ship, target, false);
                commands.put(ship.id, ship.move(dir));
                gameMap.at(ship.position).markSafe();
            }
        }
    }

    private static void addSpawns(Map<EntityId, Command> commands){
        if (me.ships.size() == 0) {
            commands.put(me.shipyard.id, Command.spawnShip());
        }
    }
}