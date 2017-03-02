package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.AnsiConsole;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;

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

    public ConsoleLogger() {
    }

    public ConsoleLogger(CoreContext context) {
        super(context);
        logger = this;

        AnsiConsole.systemInstall();
    }

    private void setColor(String color) {
        AnsiConsole.out.print(color);
    }

    private void setColor(Level level) {
        switch (level) {
            case SEVERE:
                setColor(SEVERE);
                break;
            case WARNING:
                setColor(WARNING);
                break;
            case INFO:
                setColor(INFO);
                break;
            case STARTUP:
                setColor(STARTUP);
                break;
            case PURPLE:
                setColor(PURPLE);
                break;
            case WHITE:
                setColor(WHITE);
                break;
            case BLUE:
                setColor(BLUE);
                break;
            default:
                setColor(INFO);
        }
    }

    @Override
    public Logger log(String line) {
        log(line, Level.INFO);
        return this;
    }

    @Override
    public Logger level(Level level) {
        setColor(level);
        return this;
    }

    @Override
    public Logger log(String line, Level level) {
        if (enabled.get()) {
            setColor(level);
            write(line);
        }
        return this;
    }

    @Override
    public Logger log(JsonObject data) {
        if (enabled.get()) {
            JsonObject event = eventFromLog(data);
            setColor(event);

            String text = "[" + data.getString(LOG_EVENT) + " " + timestamp(Instant.now().toEpochMilli()) + "] -> ";
            write(text + parseJsonLog(event));
        }
        return this;
    }

    private void write(String text) {
        text = replaceTags(text, LOG_HIDDEN_TAGS);
        AnsiConsole.out.println(text);
        setColor(INFO);
        AnsiConsole.out.flush();
    }

    private JsonObject eventFromLog(JsonObject data) {
        JsonObject json = data.copy();
        json.remove(PROTOCOL_ROUTE);
        json.remove(ID_TOKEN);
        json.remove(LOG_TIME);
        json.remove(LOG_EVENT);
        return json;
    }

    private String parseJsonLog(JsonObject data) {
        String text = "";
        for (String key : data.fieldNames()) {
            Object object = data.getValue(key);

            if (object instanceof String) {
                text += compactPath(data.getString(key));

                if (key.equals(LOG_LEVEL)) {
                    text += "\t";
                } else {
                    text += " ~ ";
                }
            } else if (object instanceof Integer) {
                text += key + "=" + data.getInteger(key) + " ~ ";
            }
        }
        return text;
    }

    private String compactPath(String path) {
        String text = "";

        if (path != null) {
            String[] folders = path.split(DIR_SEPARATOR);

            for (int i = 0; i < folders.length; i++) {
                if (i == 0) {
                    text += folders[i];

                    if (folders.length > 1) {
                        text += DIR_SEPARATOR;
                    }
                } else if (i == folders.length - 1) {
                    text += folders[i];
                } else {
                    text += DIR_UP;
                }
            }
        }
        return text;
    }

    private void setColor(JsonObject json) {
        if (json.containsKey(LOG_LEVEL)) {
            setColor(Level.valueOf(json.getString(LOG_LEVEL)));
        }
    }

    public Logger setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    public void reset() {
        setColor(RESET);
    }
}
