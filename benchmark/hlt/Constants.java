package hlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    /** The maximum amount of halite a ship can carry. */
    public static int MAX_HALITE;
    /** The cost to build a single ship. */
    public static int SHIP_COST;
    /** The cost to build a dropoff. */
    public static int DROPOFF_COST;
    /** The maximum number of turns a game can last. */
    public static int MAX_TURNS;
    /** 1/EXTRACT_RATIO halite (rounded) is collected from a square per turn. */
    public static int EXTRACT_RATIO;
    /** 1/MOVE_COST_RATIO halite (rounded) is needed to move off a cell. */
    public static int MOVE_COST_RATIO;
    /** Whether inspiration is enabled. */
    public static boolean INSPIRATION_ENABLED;
    /** A ship is inspired if at least INSPIRATION_SHIP_COUNT opponent ships are within this Manhattan distance. */
    public static int INSPIRATION_RADIUS;
    /** A ship is inspired if at least this many opponent ships are within INSPIRATION_RADIUS distance. */
    public static int INSPIRATION_SHIP_COUNT;
    /** An inspired ship mines 1/X halite from a cell per turn instead. */
    public static int INSPIRED_EXTRACT_RATIO;
    /** An inspired ship that removes Y halite from a cell collects X*Y additional halite. */
    public static double INSPIRED_BONUS_MULTIPLIER;
    /** An inspired ship instead spends 1/X% halite to move. */
    public static int INSPIRED_MOVE_COST_RATIO;

    public static void populateConstants(final String stringFromEngine) {
        final String[] rawTokens = stringFromEngine.split("[{}, :\"]+");
        final ArrayList<String> tokens = new ArrayList<>();
        for (int i = 0; i < rawTokens.length; ++i) {
            if (!rawTokens[i].isEmpty()) {
                tokens.add(rawTokens[i]);
            }
        }

        if ((tokens.size() % 2) != 0) {
            Log.log("Error: constants: expected even total number of key and value tokens from server.");
            throw new IllegalStateException();
        }

        final Map<String, String> constantsMap = new HashMap<>();

        for (int i = 0; i < tokens.size(); i += 2) {
            constantsMap.put(tokens.get(i), tokens.get(i+1));
        }

        SHIP_COST = getInt(constantsMap, "NEW_ENTITY_ENERGY_COST");
        DROPOFF_COST = getInt(constantsMap, "DROPOFF_COST");
        MAX_HALITE = getInt(constantsMap, "MAX_ENERGY");
        MAX_TURNS = getInt(constantsMap, "MAX_TURNS");
        EXTRACT_RATIO = getInt(constantsMap, "EXTRACT_RATIO");
        MOVE_COST_RATIO = getInt(constantsMap, "MOVE_COST_RATIO");
        INSPIRATION_ENABLED = getBoolean(constantsMap, "INSPIRATION_ENABLED");
        INSPIRATION_RADIUS = getInt(constantsMap, "INSPIRATION_RADIUS");
        INSPIRATION_SHIP_COUNT = getInt(constantsMap, "INSPIRATION_SHIP_COUNT");
        INSPIRED_EXTRACT_RATIO = getInt(constantsMap, "INSPIRED_EXTRACT_RATIO");
        INSPIRED_BONUS_MULTIPLIER = getDouble(constantsMap, "INSPIRED_BONUS_MULTIPLIER");
        INSPIRED_MOVE_COST_RATIO = getInt(constantsMap, "INSPIRED_MOVE_COST_RATIO");
    }

    private static int getInt(final Map<String, String> map, final String key) {
        return Integer.parseInt(getString(map, key));
    }

    private static double getDouble(final Map<String, String> map, final String key) {
        return Double.parseDouble(getString(map, key));
    }

    private static boolean getBoolean(final Map<String, String> map, final String key) {
        final String stringValue = getString(map, key);
        switch (stringValue) {
            case "true": return true;
            case "false": return false;
            default:
                Log.log("Error: constants: " + key + " constant has value of '" + stringValue +
                    "' from server. Do not know how to parse that as boolean.");
                throw new IllegalStateException();
        }
    }

    private static String getString(final Map<String, String> map, final String key) {
        if (!map.containsKey(key)) {
            Log.log("Error: constants: server did not send " + key + " constant.");
            throw new IllegalStateException();
        }
        return map.get(key);
    }
}
