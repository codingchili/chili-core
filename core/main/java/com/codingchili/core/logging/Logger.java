package com.codingchili.core.logging;

import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * @author Robin Duda
 *         <p>
 *         Interface to generate logging messages.
 */
public interface Logger extends JsonLogger, StringLogger {

    /**
     * Create a logging event.
     *
     * @param event handler of the event to generate
     * @param level the severity level of the event
     * @return a JsonObject with a generated event, level, timestamp and context.
     */
    JsonObject event(String event, Level level);

    /**
     * Emit when multiple attempts to initialize singleton.
     */
    void onAlreadyInitialized();

    /**
     * Emit when failing to load specified fileName;
     *
     * @param fileName handler of the file that failed to load
     */
    void onFileLoadError(String fileName);

    /**
     * Send METRICS_ENABLED to the logger.
     *
     * @param metrics a json object containing metrics data to be published
     */
    void onMetricsSnapshot(JsonObject metrics);

    /**
     * Throw when a requested handler was not found.
     *
     * @param route the handler of the missing handler.
     */
    void onHandlerMissing(String route);

    /**
     * Emit when a file has been loaded from the file-system.
     *
     * @param path a path to the loaded file.
     */
    void onFileLoaded(String path);

    /**
     * Emit when a configuration was not found on disk and its defaults was
     * loaded from memory.
     *
     * @param path  the path to the configurable that was not found.
     * @param clazz the class of the configurable.
     */
    void onConfigurationDefaultsLoaded(String path, Class<?> clazz);

    /**
     * Throw when a configuration was not found and the attempt to instantiate
     * default configuration from the given class has error.
     *
     * @param clazz a class that is not of Configurable type.
     */
    void onInvalidConfigurable(Class<?> clazz);

    /**
     * Emit when the cache of a component has been cleared.
     *
     * @param component the handler of the component.
     */
    void onCacheCleared(String component);

    /**
     * Emit when a file has been saved to file.
     *
     * @param component the component that saved the file.
     * @param path      to the saved file.
     */
    void onFileSaved(String component, String path);

    /**
     * Emit when error to save file.
     *
     * @param fileName handler of the entity that failed saving.
     */
    void onFileSaveError(String fileName);

    /**
     * Log general errors.
     *
     * @param cause the cause for throw.
     */
    void onError(Throwable cause);

    /**
     * Set logging level for raw logging calls.
     *
     * @param level sets the logging level of the logger
     * @return fluent
     */
    Logger level(Level level);

    /**
     * Emit when the interval of a periodic timer has changed.
     *
     * @param name     handler of the timer that changed interval
     * @param initial  the previous value of the interval
     * @param interval the new interval of the timer
     */
    void onTimerSourceChanged(String name, int initial, int interval);

    /**
     * Emit when a property in the security configuration is misconfigured.
     *
     * @param target     the handler of the target that is missing the requested property identifier.
     * @param identifier the secret that was requested
     */
    void onSecurityDependencyMissing(String target, String identifier);

    /**
     * Emit when a new server has started.
     */
    void onServiceStarted(CoreService service);

    /**
     * Emit when a server has stopped.
     *
     * @param future callback is called when the event is written.
     */
    void onServiceStopped(Future<Void> future, CoreService service);

    /**
     * @param cause the reason why the name has failed.
     */
    void onServiceFailed(Throwable cause);

    /**
     * called when a listener is started.
     *
     * @param listener the listener that was started
     */
    void onListenerStarted(CoreListener listener);

    /**
     * called when a listener is stopped.
     *
     * @param listener the listener that was stopped
     */
    void onListenerStopped(CoreListener listener);
}
