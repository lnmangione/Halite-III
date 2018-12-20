package pep;

import hlt.*;

import java.util.ArrayList;

public class Blocker extends Behavior {

    private Position dropoff;
    private Position target;

    //TODO: shitty constants
    @Override
    public boolean meetsCriteria(Ship ship, Type currentBehavior, Game game) {
        return false;
//        if (currentBehavior == Type.BLOCKER && ship.getHalite() < .45 * Constants.MAX_HALITE){
//            return true;
//        }
//        boolean lateGame = Constants.MAX_TURNS - game.turnNumber < Parameters.getBlockerTurns();
//        return game.players.size() == 2 && lateGame && ship.getHalite() < Parameters.getDepositorLateAmount();
    }

    @Override
    public Position getTarget(Ship ship, Game game) {
        if (target == null){
            dropoff = closestEnemyDropoff(ship.position, game);
            int offsetX = (int)(Math.random() * 5) - 2;
            int offsetY = (int)(Math.random() * 5) - 2;
            target = game.gameMap.normalize(dropoff.offset(offsetX, offsetY));
        }
        if (ship.position.equals(dropoff)){
            return dropoff;
        }
        if (ship.position.equals(target)){
            int offsetX = (int)(Math.random() * 5) - 2;
            int offsetY = (int)(Math.random() * 5) - 2;
            target = game.gameMap.normalize(dropoff.offset(offsetX, offsetY));
        }
        return target;
    }

    /**
     * @return The closest enemy dropoff to origin
     */
    public static Position closestEnemyDropoff(Position origin, Game game){
        ArrayList<Position> dropoffs = new ArrayList<>();
        for (Player player : game.players){
            if (!player.id.equals(game.me.id)){
                dropoffs.add(game.gameMap.closestDropoff(player, origin));
            }
        }
        return game.gameMap.closest(origin, dropoffs);
    }

    @Override
    public Type getType() {
        return Type.BLOCKER;
    }

    @Override
    public boolean minimizesCost() {
        return false;
    }
}
