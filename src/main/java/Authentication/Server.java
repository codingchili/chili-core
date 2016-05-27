package Authentication;

import Authentication.Controller.ClientHandler;
import Authentication.Model.DefaultProvider;
import Authentication.Controller.RealmHandler;
import Authentication.Model.Provider;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realm handler.
 */
public class Server implements Verticle {
    private Provider store;
    private Vertx vertx;
    private Logger logger;

    public Server() {
    }

    public Server(Provider store) {
        this.store = store;
        this.logger = store.getLogger();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (store == null) {
            store = new DefaultProvider(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (int i = 0; i < 4; i++) {
            new ClientHandler(store);
            new RealmHandler(store);
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
