package com.codingchili.core.Logging.Configuration;

import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class LogProvider implements Provider {
    private Vertx vertx;
    private LogServerSettings settings;
    private Logger logger;

    public LogProvider() {}

    public LogProvider(LogServerSettings settings, Logger logger, Vertx vertx) {
        this.logger = logger;
        this.settings = settings;
        this.vertx = vertx;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public LogServerSettings getSettings() {
        return settings;
    }
}
