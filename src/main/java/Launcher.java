import Authentication.Configuration.AuthProvider;
import Configuration.FileConfiguration;
import Configuration.VertxSettings;
import Patching.Configuration.PatchProvider;
import Website.Configuration.WebserverProvider;
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
        this.vertx = Vertx.vertx(VertxSettings.Configuration());
    }

    @Override
    public void start(final Future<Void> future) {
        Future<Void> logging = Future.future();
        startServer(logging, new Logging.Server());

        logging.setHandler(result -> {
            if (result.succeeded()) {
                startAll(future);
            } else
                future.fail(result.cause());
        });
    }

    private void startAll(Future<Void> future) {
        Future<Void> patch = Future.future();
        Future<Void> authentication = Future.future();
        Future<Void> game = Future.future();
        Future<Void> web = Future.future();

        CompositeFuture.all(patch, authentication, game).setHandler(result -> {
            if (result.succeeded()) {
                future.complete();
            } else
                future.fail(result.cause());
        });

        startServer(patch, new Patching.Server(new PatchProvider(vertx)));
        startServer(authentication, new Authentication.Server(new AuthProvider(vertx)));
        startServer(web, new Website.Server(new WebserverProvider(vertx)));
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
    public void stop(Future<Void> future) {
        future.complete();
    }
}
