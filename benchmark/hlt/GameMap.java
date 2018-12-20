package hlt;

import java.util.ArrayList;

public class GameMap {
    public final int width;
    public final int height;
    private final MapCell[][] cells;
    private int totalHalite;

    private GameMap(final int width, final int height) {
        this.width = width;
        this.height = height;

        cells = new MapCell[height][];
        for (int y = 0; y < height; ++y) {
            cells[y] = new MapCell[width];
        }
    }

    public MapCell at(final Position position) {
        final Position normalized = normalize(position);
        return cells[normalized.y][normalized.x];
    }

    public MapCell at(final Entity entity) {
        return at(entity.position);
    }

    public int calculateDistance(final Position source, final Position target) {
        final Position normalizedSource = normalize(source);
        final Position normalizedTarget = normalize(target);

        final int dx = Math.abs(normalizedSource.x - normalizedTarget.x);
        final int dy = Math.abs(normalizedSource.y - normalizedTarget.y);

        final int toroidal_dx = Math.min(dx, width - dx);
        final int toroidal_dy = Math.min(dy, height - dy);

        return toroidal_dx + toroidal_dy;
    }

    public Position normalize(final Position position) {
        final int x = ((position.x % width) + width) % width;
        final int y = ((position.y % height) + height) % height;
        return new Position(x, y);
    }

    public ArrayList<Direction> getUnsafeMoves(final Position source, final Position destination) {
        final ArrayList<Direction> possibleMoves = new ArrayList<>();

        final Position normalizedSource = normalize(source);
        final Position normalizedDestination = normalize(destination);

        final int dx = Math.abs(normalizedSource.x - normalizedDestination.x);
        final int dy = Math.abs(normalizedSource.y - normalizedDestination.y);
        final int wrapped_dx = width - dx;
        final int wrapped_dy = height - dy;

        if (normalizedSource.x < normalizedDestination.x) {
            possibleMoves.add(dx > wrapped_dx ? Direction.WEST : Direction.EAST);
        } else if (normalizedSource.x > normalizedDestination.x) {
            possibleMoves.add(dx < wrapped_dx ? Direction.WEST : Direction.EAST);
        }

        if (normalizedSource.y < normalizedDestination.y) {
            possibleMoves.add(dy > wrapped_dy ? Direction.NORTH : Direction.SOUTH);
        } else if (normalizedSource.y > normalizedDestination.y) {
            possibleMoves.add(dy < wrapped_dy ? Direction.NORTH : Direction.SOUTH);
        }

        return possibleMoves;
    }

    /**
     * @return An ArrayList of normalized unsafe positions adjacent to source by which to reach destination
     */
    public ArrayList<Position> getUnsafePositions(final Position source, final Position destination){
        final ArrayList<Position> possiblePositions = new ArrayList<>();
        for (Direction direction : getUnsafeMoves(source, destination)){
            possiblePositions.add(normalize(source.directionalOffset(direction)));
        }
        return possiblePositions;
    }

    /**
     * @param minimizeCost Whether to seek the lower halite cell (smaller cost next turn)
     */
    public Direction naiveNavigate(final Ship ship, final Position destination, boolean minimizeCost) {
        if (minimizeCost) return lowCostNavigate(ship, destination);

        // getUnsafeMoves normalizes for us
        for (final Direction direction : getUnsafeMoves(ship.position, destination)) {
            final Position targetPos = ship.position.directionalOffset(direction);
            if (!at(targetPos).isOccupied()) {
                at(targetPos).markUnsafe(ship);
                return direction;
            }
        }

        return Direction.STILL;
    }

    /**
     * Seek adjacent cell with less halite (smaller cost next turn)
     */
    private Direction lowCostNavigate(final Ship ship, final Position destination) {
        Direction targetDir = Direction.STILL;
        Position targetPos = null;

        // getUnsafeMoves normalizes for us
        for (final Direction direction : getUnsafeMoves(ship.position, destination)) {
            final Position adjacentPos = ship.position.directionalOffset(direction);
            if (!at(adjacentPos).isOccupied() && (targetPos == null || at(targetPos).halite > at(adjacentPos).halite)){
                targetDir = direction;
                targetPos = adjacentPos;
            }
        }
        if (targetPos != null) at(targetPos).markUnsafe(ship);
        return targetDir;
    }

    /**
     * @return Whether ship has enough halite to move
     */
    public boolean canMove(Ship ship){
        return ship.getHalite() >= Math.ceil(at(ship).halite * (1.0 / Constants.MOVE_COST_RATIO));
    }

    /**
     * @return True if 'positionA' and 'positionB' are adjacent
     */
    public boolean areAdjacent(Position positionA, Position positionB){
        return calculateDistance(positionA, positionB) == 1;
    }

    /**
     * @return The position in 'positions' closest to origin
     */
    public Position closest(Position origin, ArrayList<Position> positions){
        if (positions == null || positions.isEmpty()){
            return null;
        }
        Position closest = positions.get(0);
        for (Position position : positions){
            if (calculateDistance(position, origin) < calculateDistance(closest, origin)){
                closest = position;
            }
        }
        return closest;
    }

    /**
     * @return The closest map cell to origin within distance that has at least halite halite
     * Returns null if no such cell exists
     */
    public MapCell nearestCellWithHalite(Position origin, int distance, int halite){
        Ripple ripple = new Ripple(this, origin, distance);
        while (ripple.hasNext()){
            Position next = ripple.next();
            if (at(next).halite >= halite){
                return at(next);
            }
        }
        return null;
    }

    /**
     * @return The map cell with the most halite within distance of origin
     */
    public MapCell densestCell(Position origin, int distance){
        Ripple ripple = new Ripple(this, origin, distance);
        MapCell best = at(origin);
        while (ripple.hasNext()){
            Position next = ripple.next();
            if (at(next).halite > best.halite){
                best = at(next);
            }
        }
        return best;
    }

    /**
     * @return A list of ships within distance of origin
     */
    public ArrayList<Ship> shipsInRadius(Position origin, int distance){
        Ripple ripple = new Ripple(this, origin, distance);
        ArrayList<Ship> ships = new ArrayList<Ship>();
        while (ripple.hasNext()){
            MapCell cell = at(ripple.next());
            if (cell.isOccupied()){
                ships.add(cell.ship);
            }
        }
        return ships;
    }

    /**
     * @return The total amount of halite within distance of origin
     */
    public int haliteWithinDistance(Position origin, int distance){
        Ripple ripple = new Ripple(this, origin, distance);
        int totalHalite = 0;
        while (ripple.hasNext()){
            totalHalite += at(ripple.next()).halite;
        }
        return totalHalite;
    }

    /**
     * @return The position of player's closet dropoff to origin
     * The player's shipyard is considered a valid dropoff
     */
    public Position closestDropoff(Player player, Position origin){
        Position closest = player.shipyard.position;
        for (Dropoff dropoff : player.dropoffs.values()){
            if (calculateDistance(dropoff.position, origin) < calculateDistance(closest, origin)){
                closest = dropoff.position;
            }
        }
        return closest;
    }

    /**
     * @return The total amount of halite on the map
     */
    public int getTotalHalite(){
        return totalHalite;
    }

    /**
     * Update the average halite on the map once per turn
     */
    void updateTotalHalite(){
        int total = 0;
        for (MapCell[] row : cells){
            for (MapCell cell : row){
                total += cell.halite;
            }
        }
        totalHalite = total;
    }

    /**
     * @return The average amount of halite on the map
     */
    public double getAverageHalite(){
        return totalHalite / (double)(width * height);
    }

    /**
     * WARNING: Run this method sparingly
     *
     * @return List of positions with halite amount >= 'halite'
     */
    public ArrayList<Position> getPositionsAbove(int halite){
        ArrayList<Position> heavyCells = new ArrayList<>();
        for (MapCell[] row : cells){
            for (MapCell cell : row){
                if (cell.halite >= halite){
                    heavyCells.add(cell.position);
                }
            }
        }
        return heavyCells;
    }

    void _update() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                cells[y][x].ship = null;
            }
        }

        final int updateCount = Input.readInput().getInt();

        for (int i = 0; i < updateCount; ++i) {
            final Input input = Input.readInput();
            final int x = input.getInt();
            final int y = input.getInt();

            cells[y][x].halite = input.getInt();
        }
    }

    static GameMap _generate() {
        final Input mapInput = Input.readInput();
        final int width = mapInput.getInt();
        final int height = mapInput.getInt();

        final GameMap map = new GameMap(width, height);

        for (int y = 0; y < height; ++y) {
            final Input rowInput = Input.readInput();

            for (int x = 0; x < width; ++x) {
                final int halite = rowInput.getInt();
                map.cells[y][x] = new MapCell(new Position(x, y), halite);
            }
        }

        map.updateTotalHalite();

        return map;
    }
}
