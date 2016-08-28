package Patching;

import Logging.Model.Logger;
import Patching.Controller.ClientHandler;
import Patching.Configuration.PatchProvider;
import Patching.Controller.Transport.ClientServer;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private PatchProvider provider;
    private Logger logger;

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
    public void start(Future<Void> start) throws Exception {
        new ClientHandler(provider);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClientServer(provider));
        }

        logger.onServerStarted(start);
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
