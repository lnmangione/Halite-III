package hlt;

import java.util.ArrayList;
import java.util.Collection;

public class Position {
    public final int x;
    public final int y;

    public Position(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Position directionalOffset(final Direction d) {
        final int dx;
        final int dy;

        switch (d) {
            case NORTH:
                dx = 0;
                dy = -1;
                break;
            case SOUTH:
                dx = 0;
                dy = 1;
                break;
            case EAST:
                dx = 1;
                dy = 0;
                break;
            case WEST:
                dx = -1;
                dy = 0;
                break;
            case STILL:
                dx = 0;
                dy = 0;
                break;
            default:
                throw new IllegalStateException("Unknown direction " + d);
        }

        return new Position(x + dx, y + dy);
    }

    /**
     * Does not normalize the position
     * @return The position at offset (dx, dy) from this position
     */
    public Position offset(final int dx, final int dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Does not normalize the position
     * @return A collection of the 4 positions adjacent to this position
     */
    public Collection<Position> adjacentPositions(){
        ArrayList<Position> surrounding = new ArrayList<>();
        for (Direction d : Direction.ALL_CARDINALS){
            surrounding.add(directionalOffset(d));
        }
        return surrounding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        return y == position.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
