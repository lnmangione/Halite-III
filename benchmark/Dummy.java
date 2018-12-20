import hlt.*;
import java.util.ArrayList;

/**
 * A dummy bot sends no commands
 */
public class Dummy {

    public static void main(final String[] args) {
        Game game = new Game();
        game.ready("Dummy");

        for (;;) {
            game.updateFrame();
            game.endTurn(new ArrayList<>());
        }
    }
}