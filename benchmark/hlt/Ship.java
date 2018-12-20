package hlt;

public class Ship extends Entity {
    private final int halite;

    public Ship(final PlayerId owner, final EntityId id, final Position position, final int halite) {
        super(owner, id, position);
        this.halite = halite;
    }

    public boolean isFull() {
        return halite >= Constants.MAX_HALITE;
    }

    public int getHalite() {
        return halite;
    }

    public Command makeDropoff() {
        return Command.transformShipIntoDropoffSite(id);
    }

    public Command move(final Direction direction) {
        return Command.move(id, direction);
    }

    public Command stayStill() {
        return Command.move(id, Direction.STILL);
    }

    static Ship _generate(final PlayerId playerId) {
        final Input input = Input.readInput();

        final EntityId shipId = new EntityId(input.getInt());
        final int x = input.getInt();
        final int y = input.getInt();
        final int halite = input.getInt();

        return new Ship(playerId, shipId, new Position(x, y), halite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Ship ship = (Ship) o;

        return halite == ship.halite;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + halite;
        return result;
    }
}
