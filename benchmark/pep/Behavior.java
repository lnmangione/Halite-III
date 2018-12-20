package pep;

import hlt.*;

/**
 * A behavior defines how a ship will behave given a particular game state
 */
public abstract class Behavior {

    enum Type {
        DEPOSITOR, HARVESTER, SCRUBBER, SCOUT, BLOCKER
    }

    /**
     * @param currentBehavior The current behavior of this ship
     * @return @return True if this ship meets the criteria to take on this behavior
     */
    public abstract boolean meetsCriteria(Ship ship, Type currentBehavior, Game game);

    /**
     * @return The target position for this ship
     */
    public abstract Position getTarget(Ship ship, Game game);

    /**
     * @return The type enum of this behavior
     */
    public abstract Type getType();

    /**
     * @return True if this behavior seeks low movement cost
     */
    public abstract boolean minimizesCost();

    @Override
    public String toString(){
        Type type = getType();
        if (type == Behavior.Type.HARVESTER){
            return "HARVESTER";
        } else if (type == Behavior.Type.DEPOSITOR){
            return "DEPOSITOR";
        } else if(type == Behavior.Type.SCRUBBER){
            return "SCRUBBER";
        } else if (type == Behavior.Type.SCOUT) {
            return "SCOUT";
        } else if (type == Behavior.Type.BLOCKER) {
            return "BLOCKER";
        }
        return "NONE";
    }
}