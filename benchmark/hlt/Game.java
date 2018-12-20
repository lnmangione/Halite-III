package hlt;

import java.util.ArrayList;
import java.util.Collection;

public class Game {
    public int turnNumber;
    public final PlayerId myId;
    public final ArrayList<Player> players = new ArrayList<>();
    public final Player me;
    public final GameMap gameMap;

    public Game() {
        Constants.populateConstants(Input.readLine());

        final Input input = Input.readInput();
        final int numPlayers = input.getInt();
        myId = new PlayerId(input.getInt());

        Log.open(myId.id);

        for (int i = 0; i < numPlayers; ++i) {
            players.add(Player._generate());
        }
        me = players.get(myId.id);
        gameMap = GameMap._generate();
    }

    public void ready(final String name) {
        System.out.println(name);
    }

    public void updateFrame() {
        turnNumber = Input.readInput().getInt();
        Log.log("=============== TURN " + turnNumber + " ================");

        for (int i = 0; i < players.size(); ++i) {
            final Input input = Input.readInput();

            final PlayerId currentPlayerId = new PlayerId(input.getInt());
            final int numShips = input.getInt();
            final int numDropoffs = input.getInt();
            final int halite = input.getInt();

            players.get(currentPlayerId.id)._update(numShips, numDropoffs, halite);
        }

        gameMap._update();
        if (turnNumber % 5 == 0){
            gameMap.updateTotalHalite();
        }

        for (final Player player : players) {
            for (final Ship ship : player.ships.values()) {
                gameMap.at(ship).markUnsafe(ship);
            }

            gameMap.at(player.shipyard).structure = player.shipyard;

            for (final Dropoff dropoff : player.dropoffs.values()) {
                gameMap.at(dropoff).structure = dropoff;
            }
        }
    }

    public void endTurn(final Collection<Command> commands) {
        for (final Command command : commands) {
            System.out.print(command.command);
            System.out.print(' ');
        }
        System.out.println();
    }
}
