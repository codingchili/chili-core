package com.codingchili.core.Routing.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class RouteProvider implements Provider {
    private RoutingSettings settings;
    private Logger logger;
    private Vertx vertx;

    public RouteProvider(Vertx vertx) {
        this.settings = FileConfiguration.instance().getRoutingSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        this.vertx = vertx;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public RoutingSettings getSettings() {
        return settings;
    }

    public Vertx getVertx() {
        return vertx;
    }
}
