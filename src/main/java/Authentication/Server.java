package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientAuthenticationHandler;
import Authentication.Controller.RealmAuthenticationHandler;
import Protocols.ClusterListener;
import Protocols.ClusterVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Server extends ClusterVerticle {
    private AuthProvider provider;

    public Server() {
    }

    public Server(AuthProvider store) {
        this.provider = store;
        this.logger = store.getLogger();
    }

    @Override
    public void start(Future<Void> start) {
        if (provider == null) {
            Future<AuthProvider> providerFuture = Future.future();

            providerFuture.setHandler(future -> {
                if (future.succeeded()) {
                    this.provider = future.result();
                    this.logger = provider.getLogger();

                    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                        vertx.deployVerticle(new ClusterListener(new RealmAuthenticationHandler(provider)));
                        vertx.deployVerticle(new ClusterListener(new ClientAuthenticationHandler(provider)));
                    }

                    logger.onServerStarted(start);
                } else {
                    start.fail(future.cause());
                }
            });

            AuthProvider.create(providerFuture, vertx);
        }
    }
}
