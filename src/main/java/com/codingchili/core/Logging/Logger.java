package com.codingchili.core.Logging;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * @author Robin Duda
 *         simplifies the generation of logging messages.
 */
public interface Logger extends JsonLogger, StringLogger {

    /**
     * Create a logging event.
     */
    JsonObject event(String event, Level level);

    /**
     * Emit when multiple attempts to initialize singleton.
     */
    void onAlreadyInitialized();

    /**
     * Emit when a new server has started.
     */
    void onServerStarted(Future<Void> future);

    /**
     * Emit when a server has stopped.
     */
    void onServerStopped(Future<Void> future);

    /**
     * Emit when failing to load specified fileName;
     */
    void onFileLoadError(String fileName);

    /**
     * Send METRICS_ENABLED to the logger.
     */
    void onMetricsSnapshot(JsonObject metrics);

    /**
     * Throw when a requested handler was not found.
     *
     * @param action the name of the missing handler.
     */
    void onHandlerMissing(String action);

    /**
     * Emit when a file has been loaded from the file-system.
     * @param path a path to the loaded file.
     */
    void onFileLoaded(String path);

    /**
     * Emit when the cache of a component has been cleared.
     * @param component the name of the component.
     */
    void onCacheCleared(String component);

    /**
     * Emit when a file has been saved to file.
     * @param component the component that saved the file.
     * @param path to the saved file.
     */
    void onFileSaved(String component, String path);

    /**
     * Emit when failed to save file.
     */
    void onFileSaveError(String key);

    /**
     * Log general errors.
     * @param cause the cause for throw.
     */
    void onError(Throwable cause);

    /**
     * Set logging level for raw logging calls.
     */
    Logger level(Level startup);

    /**
     * Emit when the interval of a periodic timer has changed.
     */
    void onTimerSourceChanged(String name, int initial, int timeout);
}
