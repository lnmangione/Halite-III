package pep;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the behaviors of each ship
 */
public class BehaviorManager {
    private static HashMap<EntityId, Behavior> behaviors = new HashMap<>();

    // static instances of each behavior for purpose of checking criteria
    private static Depositor depositor = new Depositor();
    private static Harvester harvester = new Harvester();

    public static void updateBehaviors(Game game){
        for (Ship ship : game.me.ships.values()){
            if (!behaviors.containsKey(ship.id)){
                behaviors.put(ship.id, new Harvester());
            }
            Behavior currentBehavior = behaviors.get(ship.id);
            if (currentBehavior.meetsCriteria(ship, currentBehavior.getType(), game)){
                continue;
            }

            if (harvester.meetsCriteria(ship, currentBehavior.getType(), game)){
                behaviors.put(ship.id, new Harvester());
            } else if (depositor.meetsCriteria(ship, currentBehavior.getType(), game)){
                behaviors.put(ship.id, new Depositor());
            }
        }
    }

    public static Behavior behaviorForShip(Ship ship){
        return behaviors.get(ship.id);
    }

    public static Collection<EntityId> shipsForBehavior(Behavior.Type behavior){
        ArrayList<EntityId> shipIds = new ArrayList<>();
        for (Map.Entry<EntityId, Behavior> entry : behaviors.entrySet()){
            if (entry.getValue().getType() == behavior){
                shipIds.add(entry.getKey());
            }
        }
        return shipIds;
    }
}