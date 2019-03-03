package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Helper class to construct logging messages.
 */
public class LogMessage {
    private Logger logger;
    private JsonObject event;

    /**
     * Creates a new logging message. Constructor typically invoked indirectly
     * from the #{@link DefaultLogger} implementation using the #{@link Logger#event} method.
     *
     * @param logger the logger on which to send the final message.
     * @param event  the type of event in the log message.
     */
    LogMessage(DefaultLogger logger, JsonObject event) {
        this.event = event;
        this.logger = logger;
    }

    /**
     * @param level the logging level to set for the message.
     * @return fluent
     */
    public LogMessage level(Level level) {
        event.put(LOG_LEVEL, level);
        return this;
    }

    /**
     * Adds a new value to the log message as a key-value pair.
     *
     * @param key   the key of the value to add
     * @param value the value to add for the given key
     * @return fluent
     */
    public LogMessage put(String key, Object value) {
        event.put(key, value);
        return this;
    }

    /**
     * Commits the logging message.
     *
     * @return the logger that was used to send the message.
     */
    public Logger send() {
        logger.log(event);
        return logger;
    }

    /**
     * Commits the logging message with a message.
     *
     * @param message the message to include.
     * @return the logger that was used to send the message.
     */
    public Logger send(String message) {
        event.put(LOG_MESSAGE, message);
        logger.log(event);
        return logger;
    }

    /**
     * Converts the logging event into a json object.
     *
     * @return a json object representing the logging event.
     */
    public JsonObject toJson() {
        return event;
    }
}
