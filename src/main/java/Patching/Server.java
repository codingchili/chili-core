package Patching;

import Patching.Configuration.PatchProvider;
import Patching.Controller.PatchHandler;
import Protocols.ClusterListener;
import Protocols.ClusterVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Server extends ClusterVerticle {
    private PatchProvider provider;

    public Server() {
    }

    public Server(PatchProvider provider) {
        this.provider = provider;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (provider == null) {
            provider = new PatchProvider(vertx);
        }

        this.logger = provider.getLogger();
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClusterListener(new PatchHandler(provider)));
        }

        logger.onServerStarted(start);
    }
}
