package pep;

import hlt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static HashMap<EntityId, Command> commands = new HashMap<>();
    private static Player me;
    private static Game game;
    private static GameMap gameMap;

    static boolean creatingScoutDropoff = false;

    public static void updateCommands(Game game){
        commands = new HashMap<>();
        CommandManager.me = game.me;
        CommandManager.game = game;
        CommandManager.gameMap = game.gameMap;

        addSpawns();
        if (!addScoutDropoff()){
            addDropoff();
        }
        if (game.players.size() == 2){
            addAttacks();
        }
        addMoves(getTargets());
        addSpawns();
    }

    private static void addSpawns(){
        int numShips = me.ships.size();
        boolean canSpawn = me.halite >= Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied();
        boolean isLateGame = game.turnNumber > Parameters.getMaxSpawnTurn();
        if (numShips < Parameters.getMinShips(game) && canSpawn && !isLateGame && !creatingScoutDropoff){
            commands.put(me.shipyard.id, Command.spawnShip());
            me.halite -= Constants.SHIP_COST;
            //TODO: creating a dummy ship here is a shitty way of avoiding collisions with spawned ships
            gameMap.at(me.shipyard).markUnsafe(new Ship(me.id, EntityId.NONE, me.shipyard.position, 0));
        }
    }

    // TODO: rather than density, just consider total amount of halite in a radius
    /**
     * Creates at most one dropoff
     * @return True if a dropoff was created
     */
    private static boolean addDropoff() {
        for (Ship ship : me.ships.values()) {
            double minDropoffDist = Parameters.getMinDropoffDistance();
            double dropoffDensity = Parameters.getDropoffDensity();
            boolean hasDenseHalite = gameMap.haliteWithinDistance(ship.position, 4) > dropoffDensity;
            boolean isolated = gameMap.calculateDistance(ship.position, gameMap.closestDropoff(me, ship.position)) > minDropoffDist;
            boolean canAfford = canAffordDropoff(ship);
            boolean spaceAvailable = !gameMap.at(ship).hasStructure();
            boolean isEarly = game.turnNumber < Parameters.getDropoffLateTurn();
            if (hasDenseHalite && isolated && canAfford && spaceAvailable && isEarly && !creatingScoutDropoff) {
                createDropoff(ship);
                return true;
            }
        }
        return false;
    }

    private static boolean addScoutDropoff(){
        for (EntityId scoutId : BehaviorManager.shipsForBehavior(Behavior.Type.SCOUT)){
            if (game.me.ships.containsKey(scoutId)) {
                Ship scout = game.me.ships.get(scoutId);

                // if scout is at target and no structure, create dropoff
                Position target = BehaviorManager.behaviorForShip(scout).getTarget(scout, game);
                if (scout.position.equals(target) && !gameMap.at(target).hasStructure() && canAffordDropoff(scout)) {
                    createDropoff(scout);
                    creatingScoutDropoff = false;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canAffordDropoff(Ship ship){
        return me.halite + ship.getHalite() + gameMap.at(ship).halite >= Constants.DROPOFF_COST;
    }

    private static void createDropoff(Ship ship){
        commands.put(ship.id, ship.makeDropoff());
        me.halite -= Constants.DROPOFF_COST - ship.getHalite() - gameMap.at(ship).halite;
    }

    private static void addAttacks(){
        for (final Ship ship : me.ships.values()) {
            if (!gameMap.canMove(ship)){
                continue;
            }
            if (!commands.containsKey(ship.id)){
                Behavior behavior = BehaviorManager.behaviorForShip(ship);
                if (behavior.getType() == Behavior.Type.HARVESTER){
                    Position attack = ((Harvester)behavior).getAttackMove(ship, game);
                    if (attack != null){
                        Direction direction = gameMap.getUnsafeMoves(ship.position, attack).get(0);
                        commands.put(ship.id, ship.move(direction));
                    }
                }
            }
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
        // map of obstructed ship id's to ship target positions
        ArrayList<Ship> obstructedShips = new ArrayList<>();

        // add moves for unobstructed ships
        for (Map.Entry<EntityId, Position> entry : targets.entrySet()){
            Ship ship = me.ships.get(entry.getKey());
            Position target = entry.getValue();
            if (ship.position.equals(target) || !gameMap.canMove(ship)){
                commands.put(ship.id, ship.stayStill());
            } else {
                Behavior behavior = BehaviorManager.behaviorForShip(ship);
                Direction dir = gameMap.naiveNavigate(ship, target, behavior.minimizesCost());
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

        // don't get blocked out of shipyard/dropoff
        for (int i = 0; i < obstructedShips.size(); i++){
            Ship ship = obstructedShips.get(i);
            // adjacent to target, target is friendly dropoff, occupied by an enemy:
            Position target = targets.get(ship.id);
            MapCell cell = gameMap.at(target);
            if (cell.hasStructure() && cell.structure.owner.equals(game.me.id)){
                if (gameMap.areAdjacent(ship.position, target) && cell.isOccupied() && !cell.ship.owner.equals(game.me.id)){
                    Direction direction = gameMap.getUnsafeMoves(ship.position, target).get(0);
                    commands.put(ship.id, ship.move(direction));
                    cell.markUnsafe(ship);
                    gameMap.at(ship).markSafe();
                    obstructedShips.remove(i--);
                }
            }
        }

        // in end-game collide on shipyard
        if (Constants.MAX_TURNS - game.turnNumber < 13){
            for (Ship ship : obstructedShips){
                Position target = targets.get(ship.id);
                Position dropoff = gameMap.closestDropoff(me, ship.position);
                if (target.equals(dropoff) && gameMap.calculateDistance(ship.position, target) == 1){
                    Direction direction = gameMap.getUnsafeMoves(ship.position, target).get(0);
                    commands.put(ship.id, ship.move(direction));
                }
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

    public static void logCommands(){
        for (Command cmd : commands.values()) {
            Log.log(cmd.command);
        }
    }

    public static Collection<Command> getCommands(){
        return commands.values();
    }
}