package com.codingchili.core.context;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * A simple logger without any metadata, replacement for System.out.
 */
public class StringLogger extends ConsoleLogger {

    public StringLogger(CoreContext context, Class aClass) {
        super(context, aClass);
    }

    public StringLogger(Class aClass) {
        super(aClass);
    }

    @Override
    protected String parseJsonLog(JsonObject data, String event) {
        String message = data.getString(LOG_MESSAGE);
        String level = data.getString(LOG_LEVEL);
        return String.format("%s%s%s", Level.valueOf(level).color, message, ConsoleLogger.RESET);
    }
}