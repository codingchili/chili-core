package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.RealmHandler;
import Configuration.VertxSettings;
import Logging.Model.Logger;
import Protocols.ClusterListener;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Server implements Verticle {
    private AuthProvider provider;
    private Vertx vertx;
    private Logger logger;

    public Server() {
    }

    public Server(AuthProvider store) {
        this.provider = store;
        this.logger = store.getLogger();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }


    @Override
    public void start(Future<Void> start) throws Exception {
        Vertx.clusteredVertx(VertxSettings.Configuration(), cluster -> {
            this.vertx = cluster.result();

            if (cluster.succeeded()) {
                initialize(start);
            } else {
                start.fail(cluster.cause());
            }
        });
    }

    private void initialize(Future<Void> start) {
        if (provider == null) {
            Future<AuthProvider> providerFuture = Future.future();

            providerFuture.setHandler(future -> {
                if (future.succeeded()) {
                    this.provider = future.result();
                    this.logger = provider.getLogger();

                    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                        vertx.deployVerticle(new ClusterListener(new RealmHandler(provider)));
                        vertx.deployVerticle(new ClusterListener(new ClientHandler(provider)));
                    }

                    logger.onServerStarted(start);
                } else {
                    start.fail(future.cause());
                }
            });

            AuthProvider.create(providerFuture, vertx);
        }
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
