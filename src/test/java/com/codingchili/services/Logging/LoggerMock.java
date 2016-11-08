package com.codingchili.services.Logging;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Logging.*;


/**
 * Authentication.Mock implementation of a logger to disable logging.
 */
public class LoggerMock extends DefaultLogger {

    @Override
    public void onAlreadyInitialized() {

    }

    @Override
    public void onServerStarted(Future<Void> future) {
        future.complete();
    }

    @Override
    public void onServerStopped(Future<Void> future) {
        //future.complete();
    }

    @Override
    public void onFileLoadError(String fileName) {

    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {

    }

    @Override
    public void onHandlerMissing(String action) {

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
    public Logger level(Level startup) {
        return null;
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
