package com.codingchili.core.Logging.Model;

import com.codingchili.core.Configuration.Strings;
import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.*;

/**
 * @author Robin Duda
 */
public class ConsoleLogger {
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private Style style = Style.COMPRESSED;
    private boolean enabled;


    public ConsoleLogger() {
        this(true);
    }

    public ConsoleLogger(Boolean enabled) {
        this.enabled = enabled;

        AnsiConsole.systemInstall();
    }

    private void setColor(String color) {
        AnsiConsole.out.print(color);
    }

    private void setColor(JsonObject data) {
        if (data.containsKey(Strings.LOG_LEVEL)) {

            switch (data.getString(Strings.LOG_LEVEL)) {
                case Strings.LOG_LEVEL_SEVERE:
                    setColor(RED);
                    break;
                case Strings.LOG_LEVEL_WARNING:
                    setColor(YELLOW);
                    break;
                case Strings.LOG_LEVEL_INFO:
                    setColor(CYAN);
                    break;
                case Strings.LOG_LEVEL_STARTUP:
                    setColor(GREEN);
                    break;
            }
        } else {
            setColor(CYAN);
        }
    }

    public ConsoleLogger setStyle(Style style) {
        this.style = style;
        return this;
    }

    public void log(JsonObject data) {
        if (enabled) {
            setColor(data);

            String text = ((style == Style.PRETTY) ? data.encodePrettily() : data.encode())
                    .replaceAll("(\":\")", "=")
                    .replaceAll("[{}\"]", "")
                    .replaceAll(",", " ");

            AnsiConsole.out.println(text);
        }
    }

    public enum Style {PRETTY, COMPRESSED}
}
