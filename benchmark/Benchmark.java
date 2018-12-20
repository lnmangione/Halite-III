package benchmark;

import hlt.*;

import java.util.*;

/**
 * This benchmark bot has basic navigation optimizations (ships don't collide with spawns or deadlock)
 * Ships harvest from the best adjacent cell, and deposit when approximately full
 */
public class Benchmark {

    private static Random rng;
    private static Game game;
    private static Player me;
    private static GameMap gameMap;
    private static Map<EntityId, Status> statuses = new HashMap<>();

    private enum Status {
        EXPLORING, RETURNING
    }

    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }

        rng = new Random(rngSeed);
        game = new Game();
        game.ready("Benchmark");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();
            me = game.me;
            gameMap = game.gameMap;
            Map<EntityId, Command> commands = new HashMap<>();

            updateStatuses();
            addSpawns(commands);
            addMoves(getTargets(), commands);
            logCommands(commands);
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
        // map of obstructed ship id's to ship target positions
        ArrayList<Ship> obstructedShips = new ArrayList<>();

        // add moves for unobstructed ships
        for (Map.Entry<EntityId, Position> entry : targets.entrySet()){
            Ship ship = me.ships.get(entry.getKey());
            Position target = entry.getValue();
            if (ship.position.equals(target) || !gameMap.canMove(ship)){
                commands.put(ship.id, ship.stayStill());
            } else {
                Direction dir = gameMap.naiveNavigate(ship, target, false);
                if (dir != Direction.STILL){
                    commands.put(ship.id, ship.move(dir));
                    gameMap.at(ship.position).markSafe();
                } else {
                    obstructedShips.add(ship);
                }
            }
        }

        // swap obstructed ships that block each other
        for (int i = 0; i < obstructedShips.size(); i++){
            Ship ship = obstructedShips.get(i);
            Position target = targets.get(ship.id);

            for (Position unsafePos : gameMap.getUnsafePositions(ship.position, target)){
                Ship neighbor = gameMap.at(unsafePos).ship;
                if (neighbor != null && obstructedShips.contains(neighbor)){
                    Position neighborsTarget = targets.get(neighbor.id);
                    if (gameMap.getUnsafePositions(neighbor.position, neighborsTarget).contains(ship.position)){
                        swapShips(ship, neighbor, commands);
                        obstructedShips.remove(i--);
                        obstructedShips.remove(neighbor);
                        break;
                    }
                }
            }
            if (!commands.containsKey(ship.id)){
                commands.put(ship.id, ship.stayStill());
            }
        }
    }

    /**
     * Adds moves to commandQueue to swap ship1 and ship2
     * Assumes ship1 and ship2 are at adjacent positions
     */
    private static void swapShips(Ship ship1, Ship ship2, Map<EntityId, Command> commands){
        Command move1 = ship1.move(gameMap.getUnsafeMoves(ship1.position, ship2.position).get(0));
        Command move2 = ship2.move(gameMap.getUnsafeMoves(ship2.position, ship1.position).get(0));
        commands.put(ship1.id, move1);
        commands.put(ship2.id, move2);
    }

    private static void addSpawns(Map<EntityId, Command> commands){
        int numShips = me.ships.size();
        boolean canSpawn = me.halite >= Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied();
        if ((numShips < 5 || numShips < 0.7 * me.halite / Constants.MAX_HALITE) && canSpawn){
            commands.put(me.shipyard.id, Command.spawnShip());
            //TODO: creating a dummy ship here is a shitty way of avoiding collisions with spawned ships
            gameMap.at(me.shipyard).markUnsafe(new Ship(me.id, EntityId.NONE, me.shipyard.position, 0));
        }
    }

    /**
     * @return A map of ship id -> ship of ships with given status
     */
    private static Map<EntityId, Ship> shipsWithStatus(Status status){
        Map<EntityId, Ship> ships = new HashMap<>();
        for (Ship ship : me.ships.values()){
            if (statuses.containsKey(ship.id) && statuses.get(ship.id) == status){
                ships.put(ship.id, ship);
            }
        }
        return ships;
    }
}