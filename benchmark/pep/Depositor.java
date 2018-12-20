package pep;

import hlt.*;

public class Depositor extends Behavior {
    @Override
    public boolean meetsCriteria(Ship ship, Type currentBehavior, Game game) {
        boolean sufficientHalite = ship.getHalite() > Parameters.getDepositorLateAmount();
        int turnsRemaining = Constants.MAX_TURNS - game.turnNumber;
        if (turnsRemaining <= Parameters.getDepositorLateTurns(game, ship) && sufficientHalite) {
            return true;
        }
        return ship.getHalite() >= Parameters.getDepositorAmount();
    }

    @Override
    public Position getTarget(Ship ship, Game game) {
        return game.gameMap.closestDropoff(game.me, ship.position);
    }

    @Override
    public Type getType() {
        return Type.DEPOSITOR;
    }

    @Override
    public boolean minimizesCost() {
        return true;
    }
}