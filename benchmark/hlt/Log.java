package hlt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Log {
    private final FileWriter file;

    private static Log INSTANCE;
    private static ArrayList<String> LOG_BUFFER = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new AtExit());
    }

    private static class AtExit extends Thread {
        @Override
        public void run() {
            if (INSTANCE != null) {
                return;
            }

            final long now_in_nanos = System.nanoTime();
            final String filename = "bot-unknown-" + now_in_nanos + ".log";
            try (final FileWriter writer = new FileWriter(filename)) {
                for (final String message : LOG_BUFFER) {
                    writer.append(message).append('\n');
                }
            } catch (final IOException e) {
                // Nothing much we can do here.
            }
        }
    }

    private Log(final FileWriter f) {
        file = f;
    }

    static void open(final int botId) {
        if (INSTANCE != null) {
            Log.log("Error: log: tried to open(" + botId + ") but we have already opened before.");
            throw new IllegalStateException();
        }

        final String filename = "bot-" + botId + ".log";
        final FileWriter writer;
        try {
            writer = new FileWriter(filename);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        INSTANCE = new Log(writer);

        try {
            for (final String message : LOG_BUFFER) {
                writer.append(message).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        LOG_BUFFER.clear();
    }

    public static void log(final String message) {
        if (INSTANCE == null) {
            LOG_BUFFER.add(message);
            return;
        }

        try {
            INSTANCE.file.append(message).append('\n').flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
