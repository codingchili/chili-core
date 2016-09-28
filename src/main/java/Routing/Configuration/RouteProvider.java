package Routing.Configuration;

import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Routing.Model.ListenerSettings;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class RouteProvider implements Provider {
    private RoutingSettings settings;
    private Logger logger;
    private Vertx vertx;

    public RouteProvider(Vertx vertx, RoutingSettings settings) {
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        this.vertx = vertx;
        this.settings = settings;
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
