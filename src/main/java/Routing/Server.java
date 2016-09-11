package Routing;

import Configuration.FileConfiguration;
import Logging.Model.Logger;
import Routing.Configuration.RouteProvider;
import Routing.Configuration.RoutingSettings;
import Routing.Controller.RoutingHandler;
import Routing.Controller.Transport.RestListener;
import Routing.Controller.Transport.TcpListener;
import Routing.Controller.Transport.UdpListener;
import Routing.Controller.Transport.WebsocketListener;
import Routing.Model.WireListener;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Server implements Verticle {
    private RoutingSettings settings;
    private RouteProvider provider;
    private Logger logger;
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.settings = FileConfiguration.instance().getRoutingSettings();
        this.provider = new RouteProvider(vertx, settings);
        this.logger = provider.getLogger();
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        new RoutingHandler(provider);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {

            for (WireListener listener : settings.getTransport()) {
                switch (listener.getType()) {
                    case UDP:
                        vertx.deployVerticle(new UdpListener(provider, listener));
                        break;
                    case TCP:
                        vertx.deployVerticle(new TcpListener(provider, listener));
                        break;
                    case WEBSOCKET:
                        vertx.deployVerticle(new WebsocketListener(provider, listener));
                        break;
                    case REST:
                        vertx.deployVerticle(new RestListener(provider, listener));
                        break;
                }
            }
        }

        logger.onServerStarted(start);
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
