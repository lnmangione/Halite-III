package pep;

import hlt.*;
import java.util.ArrayList;

public class Scrubber extends Behavior {
    private static ArrayList<Position> scrubbedOrigins = new ArrayList<>();
    private Spiral spiral = null;
    private Position next;

    @Override
    public boolean meetsCriteria(Ship ship, Type currentBehavior, Game game) {
        if (currentBehavior == Type.SCRUBBER && spiral != null && spiral.hasNext() && !ship.isFull()){
            return true;
        }
        boolean notEarlyGame = game.turnNumber > Constants.MAX_TURNS / 4;
        boolean atDropoff = game.gameMap.at(ship).hasStructure() && game.gameMap.at(ship).structure.owner.equals(game.me.id);
        return notEarlyGame && atDropoff && !scrubbedOrigins.contains(ship.position);
    }

    @Override
    public Position getTarget(Ship ship, Game game) {
        if (spiral == null){
            scrubbedOrigins.add(ship.position);
            spiral = new Spiral(game.gameMap, ship.position, 7);
            next = spiral.next();
        }
        if (game.gameMap.at(ship.position).halite > Constants.MAX_HALITE / 50) {
            return ship.position;
        }
        if (!ship.position.equals(next)){
            return next;
        }
        if (spiral.hasNext()){
            next = spiral.next();
            return next;
        }
        return ship.position;
    }

    @Override
    public Type getType() {
        return Type.SCRUBBER;
    }

    @Override
    public boolean minimizesCost() {
        return false;
    }
}
