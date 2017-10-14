package com.codingchili.core.logging;

import com.codingchili.core.context.CoreContext;
import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.AnsiConsole;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Implementation of a console logger, filters some key/value combinations to better display the messages.
 */
public class ConsoleLogger extends DefaultLogger implements StringLogger {
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String SEVERE = "\u001B[31m";
    private static final String STARTUP = "\u001B[32m";
    private static final String WARNING = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String INFO = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private Level level = Level.INFO;

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

    private static String getColor(Level level) {
        switch (level) {
            case SEVERE:
                return SEVERE;
            case WARNING:
                return WARNING;
            case INFO:
                return INFO;
            case STARTUP:
                return STARTUP;
            case PURPLE:
                return PURPLE;
            case WHITE:
                return WHITE;
            case BLUE:
                return BLUE;
            default:
                return INFO;
        }
    }

    @Override
    public Logger level(Level level) {
        this.level = level;
        return this;
    }

    @Override
    public Logger log(JsonObject data) {
        if (enabled.get()) {
            JsonObject event = eventFromLog(data);
            write(parseJsonLog(event, consume(data, LOG_EVENT)));
        }
        return this;
    }

    private void write(String line) {
        line = replaceTags(line, LOG_HIDDEN_TAGS);
        synchronized (ConsoleLogger.class) {
            AnsiConsole.out.println(line);
        }
        AnsiConsole.out.flush();
    }

    private JsonObject eventFromLog(JsonObject data) {
        JsonObject json = data.copy();
        json.remove(ID_TOKEN);
        json.remove(LOG_EVENT);
        json.remove(LOG_APPLICATION);
        json.remove(LOG_CONTEXT);
        json.remove(LOG_HOST);
        json.remove(LOG_VERSION);
        return json;
    }

    private String parseJsonLog(JsonObject data, String event) {
        Level level = consumeLevel(data);
        String message = consume(data, LOG_MESSAGE);
        StringBuilder text = new StringBuilder()
                .append((level == null) ? formatLevel(this.level) : formatLevel(level))
                .append("\t")
                .append("[")
                .append(PURPLE)
                .append(consumeTimestamp(data))
                .append(RESET)
                .append("] ")
                .append((hasValue(event)) ? pad(event, 15) : "")
                .append(" [")
                .append(getColor(level))
                .append(pad(consume(data, LOG_SOURCE), 15))
                .append(RESET)
                .append("] ")
                .append((hasValue(message) ? message + " " : ""));

        for (String key : data.fieldNames()) {
            Object object = data.getValue(key);
            if (object != null) {
                text.append(String.format("%s%-1s%s=%s ", getColor(level), key, RESET, object.toString()));
            }
        }
        return text.toString();
    }

    private static boolean hasValue(String text) {
        return (text != null && !text.equals(""));
    }

    private Level consumeLevel(JsonObject data) {
        String level = (String) data.remove(LOG_LEVEL);
        if (level == null) {
            return this.level;
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
        return getColor(level) + level.name() + RESET;
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
