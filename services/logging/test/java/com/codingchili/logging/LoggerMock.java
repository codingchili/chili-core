package com.codingchili.logging;

import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.DefaultLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * implementation of a mock logger to disable logging.
 */
public class LoggerMock extends DefaultLogger {

    public LoggerMock() {
        super(LoggerMock.class);
    }

    @Override
    public void onAlreadyInitialized() {
    }

    @Override
    public void onServiceStarted(CoreService service) {
    }

    @Override
    public void onServiceStopped(Future<Void> future, CoreService service) {
        future.complete();
    }

    @Override
    public void onFileLoadError(String fileName) {
    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {
    }

    @Override
    public void onHandlerMissing(String target, String route) {
    }

    @Override
    public void onFileLoaded(String path) {
    }

    @Override
    public void onCacheCleared(String component) {
    }

    @Override
    public void onFileSaved(String component, String path) {
    }

    @Override
    public void onError(Throwable cause) {
    }

    @Override
    public Logger log(JsonObject data) {
        return this;
    }

    @Override
    public DefaultLogger log(String line) {
        return this;
    }

    @Override
    public DefaultLogger log(String line, Level level) {
        return this;
    }
}
