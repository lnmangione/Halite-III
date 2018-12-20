import hlt.*;
import pep.*;

public class GeneticBot {

    private static boolean isEvolving = false;

    public static void main(final String[] args) {
        Game game = new Game();

        if (args.length == 1) {
            isEvolving = Integer.parseInt(args[0]) == 1;
        }
        Parameters.evolvedDefaults();
        if (isEvolving){
            Parameters.populateFromXML();
        }

        game.ready("GeneticBot");
        Log.log("Successfully created bot! My Player ID is " + game.myId + ".");

        for (;;) {
            game.updateFrame();
            if (isEvolving){
                if (game.turnNumber == 1) {
                    Parameters.saveInitialState(game);
                } else if (game.turnNumber == Constants.MAX_TURNS){
                    Parameters.writeFitness(game);
                }
            }
            BehaviorManager.updateBehaviors(game);
            CommandManager.updateCommands(game);
            // CommandManager.logCommands();
            game.endTurn(CommandManager.getCommands());
        }
    }
}