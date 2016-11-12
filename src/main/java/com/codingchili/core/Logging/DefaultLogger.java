package com.codingchili.core.Logging;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.Delay;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *
 * Default logging implementation.
 */
public abstract class DefaultLogger extends Handler implements Logger {
    private Level level = Level.INFO;
    CoreContext context;
    JsonLogger logger;

    protected DefaultLogger() {
    }

    DefaultLogger(CoreContext context) {
        this.context = context;
    }

    @Override
    public Logger log(JsonObject json) {
        logger.log(json
                .put(PROTOCOL_ROUTE, PROTOCOL_LOGGING)
                .put(LOG_TIME, Instant.now().toEpochMilli()));
        return this;
    }

    private JsonObject event(String name) {
        return event(name, Level.INFO);
    }

    @Override
    public JsonObject event(String name, Level level) {
        JsonObject event = new JsonObject()
                .put(PROTOCOL_ROUTE, PROTOCOL_LOGGING)
                .put(LOG_EVENT, name)
                .put(LOG_LEVEL, level)
                .put(LOG_TIME, Instant.now().toEpochMilli());

        if (context != null) {
            event.put(Strings.LOG_HOST, context.identity().getHost())
                    .put(Strings.LOG_NODE, context.identity().getNode())
                    .put(Strings.LOG_AGENT, context.handler());
        }
        return event;
    }

    @Override
    public void onAlreadyInitialized() {
        log(event(Strings.LOG_ERROR, Level.WARNING)
                .put(ID_MESSAGE, ERROR_ALREADY_INITIALIZED));
    }

    @Override
    public void onServerStarted(Future<Void> future) {
        log(event(LOG_SERVER_START, Level.STARTUP));
        future.complete();
    }

    @Override
    public void onServerStopped(Future<Void> future) {
        log(event(LOG_SERVER_STOP, Level.SEVERE));
        Delay.forShutdown(future);
    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {
        log(event(LOG_METRICS)
                .put(ID_DATA, metrics));
    }

    @Override
    public void onHandlerMissing(String route) {
        log(event(LOG_HANDLER_MISSING, Level.WARNING)
                .put(LOG_MESSAGE, Strings.quote(route)));
    }

    @Override
    public void onFileLoaded(String path) {
        log(event(LOG_FILE_LOADED, Level.INFO)
                .put(LOG_MESSAGE, path));
    }

    @Override
    public void onError(Throwable cause) {
        log(event(LOG_ERROR, Level.SEVERE)
                .put(LOG_MESSAGE, cause.getMessage()));
    }

    @Override
    public Logger level(Level level) {
        this.level = level;
        return this;
    }

    @Override
    public void publish(LogRecord record) {
        log(addTrace(event(LOG_VERTX, Level.valueOf(record.getLevel().getName())), record)
                .put(LOG_MESSAGE, record.getMessage()));
    }

    @Override
    public void onFileLoadError(String fileName) {
        log(event(LOG_FILE_ERROR, Level.SEVERE)
                .put(LOG_MESSAGE, fileName));
    }

    @Override
    public void onFileSaved(String saver, String path) {
        log(event(LOG_FILE_SAVED, Level.INFO)
                .put(LOG_AGENT, saver)
                .put(LOG_MESSAGE, path));
    }

    @Override
    public void onFileSaveError(String fileName) {
        log(event(LOG_FILE_SAVED, Level.SEVERE)
                .put(LOG_MESSAGE, fileName));
    }

    @Override
    public void onCacheCleared(String component) {
        log(event(LOG_CACHE_CLEARED, Level.WARNING)
                .put(LOG_AGENT, component));
    }

    @Override
    public Logger log(String line) {
        log(event(LOG_MESSAGE, level)
                .put(ID_MESSAGE, line));
        return this;
    }

    @Override
    public Logger log(String line, Level level) {
        log(event(LOG_MESSAGE, level)
                .put(ID_MESSAGE, line));
        return this;
    }

    @Override
    public void onTimerSourceChanged(String name, int initialTimeout, int newTimeout) {
        log(event(LOG_TIMER_CHANGE, Level.INFO)
                .put(ID_NAME, name)
                .put(LOG_PREVIOUS, initialTimeout)
                .put(LOG_NEW, newTimeout));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private JsonObject addTrace(JsonObject log, LogRecord record) {

        if (record.getThrown() != null) {
            StackTraceElement element = record.getThrown().getStackTrace()[0];

            if (element != null)
                log.put(LOG_TRACE,
                        element.getClassName() + "." +
                                element.getMethodName() + " (" +
                                element.getLineNumber() + ")"
                );

            record.getThrown().printStackTrace();
        }

        return log;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
