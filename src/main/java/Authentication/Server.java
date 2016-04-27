package Authentication;

import Authentication.Controller.ClientHandler;
import Authentication.Controller.RealmHandler;
import Configuration.Config;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by Robin on 2016-04-07.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        Config.Load();
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, "Authserver");

        new ClientHandler(vertx, logger, new RealmHandler(vertx, logger));
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        logger.onServerStarted();
        start.complete();
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
