package Routing.Configuration;

import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Routing.Model.ClusterRequest;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class RouteProvider implements Provider {
    private Protocol<PacketHandler<ClusterRequest>> protocol;
    private RoutingSettings settings;
    private Logger logger;
    private Vertx vertx;

    public RouteProvider(Vertx vertx, RoutingSettings settings) {
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        this.protocol = new Protocol<>();
        this.vertx = vertx;
        this.settings = settings;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Protocol<PacketHandler<ClusterRequest>> getProtocol() {
        return protocol;
    }

    public RoutingSettings getSettings() {
        return settings;
    }

    public Vertx getVertx() {
        return vertx;
    }
}
