import Configuration.FileConfiguration;
import Configuration.VertxSettings;
import io.vertx.core.*;

/**
 * @author Robin Duda
 *         Launches all the components of the system on a single host.
 */
public class Launcher implements Verticle {
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        FileConfiguration.instance();
        this.vertx = vertx;
    }

    @Override
    public void start(final Future<Void> start) {
        Future<Void> logging = Future.future();
        startServer(logging, new Logging.Server());

        Vertx.clusteredVertx(VertxSettings.Configuration(), cluster -> {
            this.vertx = cluster.result();

            if (cluster.succeeded()) {
                logging.setHandler(logger -> {
                    if (logger.succeeded()) {
                        startAll(start);
                    } else
                        start.fail(logger.cause());
                });
            } else {
                start.fail(cluster.cause());
            }
        });
    }

    private void startAll(Future<Void> future) {
        Future<Void> patch = Future.future();
        Future<Void> authentication = Future.future();
        Future<Void> game = Future.future();
        Future<Void> web = Future.future();
        Future<Void> router = Future.future();

        CompositeFuture.all(patch, authentication, game, router).setHandler(result -> {
            if (result.succeeded()) {
                future.complete();
            } else
                future.fail(result.cause());
        });

        startServer(router, new Routing.Server());
        startServer(patch, new Patching.Server());
        startServer(authentication, new Authentication.Server());
        startServer(web, new Website.Server());
        startServer(game, new Realm.Server());
    }

    private void startServer(Future<Void> future, Verticle verticle) {
        vertx.deployVerticle(verticle, result -> {
            if (result.succeeded())
                future.complete();
            else
                future.fail(result.cause());
        });
    }

    @Override
    public void stop(Future<Void> stop) {
        stop.complete();
    }
}
