package Patching;

import Logging.Model.Logger;
import Patching.Configuration.PatchProvider;
import Patching.Controller.ClientPatchHandler;
import Protocols.ClusterListener;
import Protocols.ClusterServer;
import Routing.Controller.Transport.ClientServerPatch;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Server implements Verticle {
    private PatchProvider provider;
    private Logger logger;
    private Vertx vertx;

    public Server() {
    }

    public Server(PatchProvider provider) {
        this.provider = provider;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (provider == null) {
            provider = new PatchProvider(vertx);
        }

        this.logger = provider.getLogger();
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClusterListener(new ClientPatchHandler(provider)));
        }

        logger.onServerStarted(start);
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
