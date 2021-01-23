package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.codingchili.core.configuration.Environment;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.core.logging.Level.INFO;

/**
 * Default logging implementation.
 */
public abstract class AbstractLogger extends Handler implements Logger {
    protected Map<String, Consumer<JsonObject>> metadata = new HashMap<>();
    protected CoreContext context;
    protected JsonLogger logger;
    protected Class aClass;

    public AbstractLogger(CoreContext context, Class aClass) {
        this.context = context;
        this.aClass = aClass;
    }

    public AbstractLogger(Class aClass) {
        this.aClass = aClass;
    }

    @Override
    public Logger log(JsonObject json) {
        logger.log(json
                .put(PROTOCOL_ROUTE, PROTOCOL_LOGGING)
                .put(LOG_TIME, Instant.now().toEpochMilli()));
        return this;
    }

    @Override
    public Logger setMetadata(String name, Supplier<JsonObject> value) {
        metadata.put(name, (json) -> json.mergeIn(value.get()));
        return this;
    }

    @Override
    public Logger setMetadataValue(String key, Supplier<String> value) {
        metadata.put(key, (json) -> json.put(key, value.get()));
        return this;
    }

    @Override
    public Logger removeMetadata(String nameOrKey) {
        metadata.remove(nameOrKey);
        return this;
    }

    @Override
    public LogMessage event(String name) {
        return event(name, INFO);
    }

    @Override
    public LogMessage event(String name, LogLevel level) {
        JsonObject event = new JsonObject()
                .put(LOG_EVENT, name)
                .put(LOG_LEVEL, level.getName())
                .put(LOG_TIME, Instant.now().toEpochMilli());
        addMetadata(event);
        return new LogMessage(this, event);
    }

    /**
     * Adds metadata to logging events generated by the logger.
     *
     * @param event the log event to add metadata to.
     */
    private void addMetadata(JsonObject event) {
        event.put(LOG_HOST, Environment.hostname().orElse(ID_UNDEFINED))
                .put(LOG_APPLICATION, launcher().getApplication())
                .put(LOG_SOURCE, aClass.getSimpleName())
                .put(LOG_VERSION, launcher().getVersion());

        metadata.values().forEach(consumer -> consumer.accept(event));
    }

    @Override
    public void onAlreadyInitialized() {
        event(LOG_ERROR, Level.WARNING)
                .put(PROTOCOL_MESSAGE, ERROR_ALREADY_INITIALIZED).send();
    }

    @Override
    public void onServiceStarted(CoreService service) {
        event(LOG_SERVICE_START)
                .put(ID_NAME, service.name()).send();
    }

    @Override
    public void onServiceStopped(CoreService service) {
        event(LOG_SERVICE_STOP)
                .put(ID_NAME, service.name()).send();
    }

    @Override
    public void onListenerStarted(CoreListener listener) {
        event(LOG_LISTENER_START)
                .put(ID_HANDLER, listener.toString()).send();
    }

    @Override
    public void onListenerStopped(CoreListener listener) {
        event(LOG_LISTENER_STOP)
                .put(ID_HANDLER, listener.toString()).send();
    }

    @Override
    public void onServiceFailed(Throwable cause) {
        event(LOG_SERVICE_FAIL, Level.ERROR)
                .put(ID_MESSAGE, cause.getMessage()).send();
    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {
        event(LOG_METRICS).put(ID_DATA, metrics).send();
    }

    @Override
    public void onHandlerMissing(String target, String route) {
        event(LOG_HANDLER_MISSING, Level.WARNING)
                .put(PROTOCOL_TARGET, target)
                .put(PROTOCOL_ROUTE, route)
                .put(LOG_MESSAGE, getHandlerMissing()).send();
    }

    @Override
    public void onFileLoaded(String path) {
        event(LOG_FILE_LOADED, INFO).put(LOG_MESSAGE, path).send();
    }

    @Override
    public void onError(Throwable throwable) {
        LogMessage event = event(LOG_ERROR, Level.ERROR);

        if (throwable.getStackTrace() == null) {
            event.send(throwable.getMessage());
        } else {
            event.put(LOG_STACKTRACE, throwableToString(throwable)).send();
        }
    }

    @Override
    public void publish(LogRecord record) {
        LogMessage message = event(LOG_VERTX, Level.valueOf(record.getLevel().getName()));

        if (record.getThrown() != null) {
            message.put(LOG_STACKTRACE, throwableToString(record.getThrown()));
        }
        message.send(record.getMessage());
    }

    @Override
    public void onFileLoadError(String fileName) {
        event(LOG_FILE_ERROR, Level.ERROR).put(LOG_MESSAGE, fileName).send();
    }

    @Override
    public void onFileSaved(String saver, String path) {
        event(LOG_FILE_SAVED, INFO)
                .put(LOG_AGENT, saver)
                .put(LOG_MESSAGE, path).send();
    }

    @Override
    public void onFileSaveError(String fileName) {
        event(LOG_FILE_SAVED, Level.ERROR)
                .put(LOG_MESSAGE, fileName).send();
    }

    @Override
    public void onConfigurationDefaultsLoaded(String path, Class<?> clazz) {
        event(LOG_CONFIG_DEFAULTED, Level.WARNING)
                .put(LOG_MESSAGE, getFileLoadDefaults(path, clazz)).send();
    }

    @Override
    public void onInvalidConfigurable(Class<?> clazz) {
        event(LOG_CONFIGURATION_INVALID, Level.ERROR)
                .put(LOG_MESSAGE, getErrorInvalidConfigurable(clazz)).send();
    }

    @Override
    public void onCacheCleared(String component) {
        event(LOG_CACHE_CLEARED, Level.WARNING)
                .put(LOG_AGENT, component).send();
    }

    @Override
    public void onSecurityDependencyMissing(String target, String identifier) {
        event(LOG_SECURITY, Level.ERROR)
                .put(LOG_MESSAGE, getSecurityDependencyMissing(target, identifier)).send();
    }

    @Override
    public Logger log(String line) {
        event(LOG_MESSAGE, INFO).put(LOG_MESSAGE, line).send();
        return this;
    }

    @Override
    public Logger log(String line, LogLevel level) {
        event(LOG_MESSAGE, level).put(LOG_MESSAGE, line).send();
        return this;
    }

    @Override
    public void onTimerSourceChanged(String name, int initialTimeout, int newTimeout) {
        event(LOG_TIMER_CHANGE, INFO)
                .put(ID_NAME, name)
                .put(LOG_PREVIOUS, initialTimeout)
                .put(LOG_NEW, newTimeout)
                .send();
    }

    @Override
    public void flush() {
    }
}