package Website;

import Logging.Model.Logger;
import Website.Configuration.WebserverProvider;
import Website.Controller.RequestHandler;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class Server implements Verticle {
    private Vertx vertx;
    private Logger logger;
    private WebserverProvider provider;

    public Server() {
    }

    public Server(WebserverProvider provider) {
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
            this.provider = new WebserverProvider(vertx);
        }

        this.logger = provider.getLogger();
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new RequestHandler(provider, i));
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
