package Authentication;

import Authentication.Controller.ClientHandler;
import Authentication.Controller.Transport.ClientServer;
import Authentication.Controller.Transport.RealmServer;
import Authentication.Model.DefaultProvider;
import Authentication.Controller.RealmHandler;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Server implements Verticle {
    private AuthServerSettings settings;
    private Provider provider;
    private Vertx vertx;
    private Logger logger;

    public Server(Provider store) {
        this.provider = store;
        this.logger = store.getLogger();
        this.settings = store.getAuthserverSettings();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (provider == null) {
            provider = new DefaultProvider(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        new ClientHandler(provider);
        new RealmHandler(provider);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClientServer(provider, settings));
            vertx.deployVerticle(new RealmServer(provider, settings));
        }

        logger.onServerStarted();
        start.complete();
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
