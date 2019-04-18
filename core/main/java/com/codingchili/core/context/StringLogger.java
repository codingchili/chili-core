package com.codingchili.core.context;

import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.Ansi;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.LogLevel;

import static com.codingchili.core.configuration.CoreStrings.LOG_MESSAGE;

/**
 * A simple logger without any metadata, replacement for System.out.
 */
public class StringLogger extends ConsoleLogger {

    /**
     * @param aClass the class that uses this logger.
     */
    public StringLogger(Class aClass) {
        super(aClass);
    }

    /**
     * @param context the context the logger is used on.
     * @param aClass  the class that uses this logger.
     */
    public StringLogger(CoreContext context, Class aClass) {
        super(context, aClass);
    }

    @Override
    protected String parseJsonLog(JsonObject data, String event) {
        String message = data.getString(LOG_MESSAGE);
        LogLevel level = consumeLevel(data);
        return Ansi.ansi().fg(level.getColor())
                .a(message)
                .reset()
                .toString();
    }
}