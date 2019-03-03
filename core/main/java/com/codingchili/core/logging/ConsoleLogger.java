package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.AnsiConsole;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ShutdownListener;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Implementation of a console logger, filters some key/value combinations to better display the messages.
 */
public class ConsoleLogger extends DefaultLogger implements StringLogger {
    public static final String RESET = "\u001B[0m";
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private static final Set<String> filtered = new HashSet<>(Arrays.asList(
            ID_TOKEN, LOG_EVENT, LOG_APPLICATION, LOG_CONTEXT, LOG_HOST, LOG_VERSION
    ));

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(getClass().getName());
            thread.setDaemon(false);
            return thread;
        }
    });

    static {
        ShutdownListener.subscribe(executor::shutdown);
    }

    public ConsoleLogger() {
        super(ConsoleLogger.class);
    }

    public ConsoleLogger(Class aClass) {
        this(null, aClass);
    }

    public ConsoleLogger(CoreContext context, Class aClass) {
        super(context, aClass);
        logger = this;
        AnsiConsole.systemInstall();
    }

    private Consumer<JsonObject> log = (json) -> {
        write(parseJsonLog(json, consume(json, LOG_EVENT)));
    };

    /**
     * @return a set of keys that will not be printed to the console.
     */
    public Set<String> getFilteredKeys() {
        return filtered;
    }

    @Override
    public Logger log(JsonObject data) {
        if (enabled.get()) {
            if (executor.isShutdown()) {
                log.accept(data);
            } else {
                executor.submit(() -> log.accept(data));
            }
        }
        return this;
    }

    private void write(String line) {
        line = replaceTags(line, LOG_HIDDEN_TAGS);
        AnsiConsole.out.println(line);
        AnsiConsole.out.flush();
    }

    protected String parseJsonLog(JsonObject data, String event) {
        Level level = consumeLevel(data);
        String message = consume(data, LOG_MESSAGE);
        StringBuilder text = new StringBuilder()
                .append(formatLevel(level))
                .append("\t")
                .append("[")
                .append(Level.SPECIAL.color)
                .append(consumeTimestamp(data))
                .append(RESET)
                .append("] ")
                .append((hasValue(event)) ? pad(event, 15) : "")
                .append(" [")
                .append(level.color)
                .append(pad(consume(data, LOG_SOURCE), 15))
                .append(RESET)
                .append("] ")
                .append((hasValue(message) ? message + " " : ""));

        data.forEach(entry -> {
            if (entry.getValue() != null && !filtered.contains(entry.getKey())) {
                text.append(
                        String.format("%s%-1s%s=%s ",
                                level.color,
                                entry.getKey(),
                                RESET,
                                entry.getValue().toString()));
            }
        });
        return text.toString();
    }

    private static boolean hasValue(String text) {
        return (text != null && !text.equals(""));
    }

    private Level consumeLevel(JsonObject data) {
        String level = (String) data.remove(LOG_LEVEL);
        if (level == null) {
            return Level.INFO;
        } else {
            return Level.valueOf(level);
        }
    }

    private String consume(JsonObject data, String key) {
        if (data.containsKey(key)) {
            return (String) data.remove(key);
        } else {
            return "";
        }
    }

    private String consumeTimestamp(JsonObject data) {
        if (data.containsKey(LOG_TIME)) {
            return timestamp(Long.parseLong(data.remove(LOG_TIME).toString()));
        } else {
            return timestamp(Instant.now().toEpochMilli()) + "";
        }
    }

    private String pad(String text, int spaces) {
        int padding = spaces - text.length();
        if (padding > 0) {
            return text + Collections.nCopies(padding, " ").stream().collect(Collectors.joining());
        } else {
            return text;
        }
    }

    private static String formatLevel(Level level) {
        return level.color + level.name() + RESET;
    }

    private String compactPath(String path) {
        StringBuilder text = new StringBuilder();

        if (path != null) {
            String[] folders = path.split(DIR_SEPARATOR);

            for (int i = 0; i < folders.length; i++) {
                if (i == 0) {
                    text.append(folders[i]);

                    if (folders.length > 1) {
                        text.append(DIR_SEPARATOR);
                    }
                } else if (i == folders.length - 1) {
                    text.append(folders[i]);
                } else {
                    text.append(DIR_UP);
                }
            }
        }
        return text.toString();
    }

    public Logger setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }
}
