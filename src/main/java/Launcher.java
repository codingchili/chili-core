import Authentication.Controller.AuthenticationServer;
import Configuration.Configuration;
import Game.GameServer;
import Logging.LogServer;
import Utilities.DefaultLogger;
import Utilities.Logger;
import Website.WebServer;
import io.vertx.core.*;

/**
 * Created by Robin on 2016-04-07.
 */
public class Launcher extends AbstractVerticle {
    private Vertx vertx;
    private Logger logger;

    public void init(Vertx vertx, Context context) {
        Configuration.Load(context.config());
        this.vertx = vertx;
    }

    public void start(final Future<Void> future) {
        Future<Void> logging = Future.future();
        startServer(logging, new LogServer());

        logging.setHandler(result -> {
            if (result.succeeded()) {
                logger = new DefaultLogger(vertx, this.getClass().getSimpleName());
                logger.configuration(Configuration.getSource());
                startAll(future);
            } else
                future.fail(result.cause());
        });
    }

    private void startAll(Future<Void> future) {
        Future<Void> web = Future.future();
        Future<Void> authentication = Future.future();
        Future<Void> game = Future.future();

        CompositeFuture.all(web, authentication, game).setHandler(result -> {
            if (result.succeeded()) {
                future.complete();
            } else
                future.fail(result.cause());
        });

        startServer(web, new WebServer());
        startServer(authentication, new AuthenticationServer());
        startServer(game, new GameServer());
    }

    private void startServer(Future<Void> future, Verticle verticle) {
        vertx.deployVerticle(verticle, result -> {
            if (result.succeeded())
                future.complete();
            else
                future.fail(result.cause());
        });
    }

    public void stop(Future<Void> future) {
        future.complete();
    }

}
