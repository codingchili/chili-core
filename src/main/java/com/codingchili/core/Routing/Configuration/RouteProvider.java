package com.codingchili.core.Routing.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

import static com.codingchili.core.Configuration.Strings.PATH_ROUTING;

/**
 * @author Robin Duda
 */
public class RouteProvider implements Provider {
    private final RoutingSettings settings;
    private final Logger logger;
    private final Vertx vertx;

    public RouteProvider(Vertx vertx) {
        this.settings = FileConfiguration.get(PATH_ROUTING, RoutingSettings.class);
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
