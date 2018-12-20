package hlt;

import java.util.ArrayList;
import java.util.Iterator;

public class Spiral implements Iterator<Position> {

    private Iterator<Position> iterator;

    /**
     * Creates a counter-clockwise spiral iterator of positions in a grid of dimension:
     * dimension x dimension centered at origin
     */
    public Spiral(GameMap gameMap, Position origin, int dimension){
        ArrayList<Position> positions = new ArrayList<>();
        Position currentPosition = origin;
        positions.add(currentPosition);
        int sign = 1;
        for (int i = 1; i <= dimension; i++){
            for (int xStep = 1; xStep <= i; xStep++){
                currentPosition = currentPosition.offset(sign, 0);
                currentPosition = gameMap.normalize(currentPosition);
                positions.add(currentPosition);
            }
            for (int yStep = 1; yStep <= i; yStep++){
                currentPosition = currentPosition.offset(0, sign);
                currentPosition = gameMap.normalize(currentPosition);
                positions.add(currentPosition);
            }
            sign *= -1;
        }
        iterator = positions.subList(0, dimension * dimension).iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    //TODO: compute next position each call to next() rather than pre-computing the entire iterator
    @Override
    public Position next() {
        return iterator.next();
    }
}
