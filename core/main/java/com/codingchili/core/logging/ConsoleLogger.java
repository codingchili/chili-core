package com.codingchili.core.logging;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fusesource.jansi.AnsiConsole;

import com.codingchili.core.context.CoreContext;

import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Implementation of a console logger.
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
    }

    public ConsoleLogger(CoreContext context) {
        super(context);
        logger = this;
        AnsiConsole.systemInstall();
    }

    private String getColor(Level level) {
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
    public Logger log(String line) {
        log(line, Level.INFO);
        return this;
    }

    @Override
    public Logger level(Level level) {
        this.level = level;
        return this;
    }

    @Override
    public Logger log(String line, Level level) {
        if (enabled.get()) {
            write(getColor(level), line);
        }
        return this;
    }

    @Override
    public Logger log(JsonObject data) {
        if (enabled.get()) {
            JsonObject event = eventFromLog(data);
            write(getColor(event), parseJsonLog(event, consumeEvent(data)));
        }
        return this;
    }

    private void write(String color, String text) {
        text = replaceTags(text, LOG_HIDDEN_TAGS);
        AnsiConsole.out.print(String.format("%s%s\n", color, text));
        AnsiConsole.out.flush();
    }

    private JsonObject eventFromLog(JsonObject data) {
        JsonObject json = data.copy();
        json.remove(PROTOCOL_ROUTE);
        json.remove(ID_TOKEN);
        json.remove(LOG_TIME);
        json.remove(LOG_EVENT);
        json.remove(LOG_APPLICATION);
        json.remove(LOG_CONTEXT);
        return json;
    }

    private String time() {
        return timestamp(Instant.now().toEpochMilli());
    }

    private String consumeLevel(JsonObject data) {
        return (String) data.remove(LOG_LEVEL);
    }

    private String consumeEvent(JsonObject data) {
        return (String) data.remove(LOG_EVENT);
    }

    private String parseJsonLog(JsonObject data, String event) {
        String level = consumeLevel(data);
        StringBuilder text = new StringBuilder(String.format("%-12s %-7s [%s]\t", time(),
                (level == null) ? "" : level,
                (event == null) ? "" : event.toUpperCase()));

        for (String key : data.fieldNames()) {
            Object object = data.getValue(key);
            if (object != null) {
                text.append(String.format("%-1s=%s ", key, object.toString()));
            }
        }
        return text.toString();
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

    private String getColor(JsonObject json) {
        if (json.containsKey(LOG_LEVEL)) {
            return getColor(Level.valueOf(json.getString(LOG_LEVEL)));
        } else {
            return getColor(level);
        }
    }

    public Logger setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    public void reset() {
        AnsiConsole.out.print(RESET);
    }
}
