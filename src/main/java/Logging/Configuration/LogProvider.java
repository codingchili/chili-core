package Logging.Configuration;

import Configuration.Provider;
import Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class LogProvider implements Provider {
    private final Vertx vertx;
    private LogServerSettings settings;
    private Logger logger;

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
