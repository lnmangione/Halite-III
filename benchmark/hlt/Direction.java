package hlt;

import java.util.ArrayList;

public enum Direction {
    NORTH('n'),
    EAST('e'),
    SOUTH('s'),
    WEST('w'),
    STILL('o');

    public final char charValue;

    public final static ArrayList<Direction> ALL_CARDINALS = new ArrayList<>();
    static {
        ALL_CARDINALS.add(NORTH);
        ALL_CARDINALS.add(SOUTH);
        ALL_CARDINALS.add(EAST);
        ALL_CARDINALS.add(WEST);
    }

    public Direction invertDirection() {
        switch (this) {
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            case WEST: return EAST;
            case STILL: return STILL;
            default: throw new IllegalStateException("Unknown direction " + this);
        }
    }

    Direction(final char charValue) {
        this.charValue = charValue;
    }
}
