package hlt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Ripple implements Iterator<Position> {

    final private GameMap gameMap;
    final private Position origin;
    final private int maxDistance;
    private ArrayList<Position> positions = new ArrayList<>();
    private int currentDistance = 0;

    /**
     * @return The number of cells within a ripple of size 'distance'. Includes origin position
     */
    public static int positionsWithinDist(int distance){
        int num = 1;
        for (int i = 1; i <= distance; i++){
            num += 4 * i;
        }
        return num;
    }

    /**
     * Creates a ripple-like iterator of positions in increasing distance away from origin
     * The ripple ends at distance maxDistance
     */
    public Ripple(GameMap gameMap, Position origin, int maxDistance){
        this.gameMap = gameMap;
        this.origin = origin;
        this.maxDistance = maxDistance;
        positions.add(origin);
    }

    @Override
    public boolean hasNext() {
        return !positions.isEmpty();
    }

    @Override
    public Position next() {
        Position next = gameMap.normalize(positions.remove(0));
        if (positions.isEmpty() && currentDistance < maxDistance){
            currentDistance++;
            Set<Position> positionsSet = new HashSet<>();
            for (int i = 0; i <= currentDistance; i++){
                int dx = i;
                int dy = currentDistance - dx;
                positionsSet.add(origin.offset(dx,   dy));
                positionsSet.add(origin.offset(-dx,  dy));
                positionsSet.add(origin.offset(dx,  -dy));
                positionsSet.add(origin.offset(-dx, -dy));
            }
            positions = new ArrayList<>(positionsSet);
        }
        return next;
    }
}