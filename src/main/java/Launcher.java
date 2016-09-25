import Configuration.JsonFileStore;
import Configuration.Strings;
import Configuration.VertxSettings;
import Logging.Model.ConsoleLogger;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import java.io.IOException;

/**
 * @author Robin Duda
 *         Launches all the components of the system on a single host.
 */
public class Launcher {
    private Vertx vertx;

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Future<Launcher> future = Future.future();

        if (args.length == 0) {
            printHelp();
        } else {
            future.setHandler(startup -> {
                if (startup.succeeded()) {
                    Launcher launcher = startup.result();

                    for (String arg : args) {
                        if (arg.equals("*")) {
                            launcher.startAll();
                        } else {
                            launcher.start(arg);
                        }
                    }
                } else {
                    throw new RuntimeException(startup.cause());
                }
            });
            new Launcher(future);
        }
    }

    private static void printHelp() {
        ConsoleLogger logger = new ConsoleLogger().setStyle(ConsoleLogger.Style.PRETTY);
        try {
            logger.log(JsonFileStore.readObject(Strings.PATH_VERTX));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Launcher(Future<Launcher> future) {
        Vertx.clusteredVertx(VertxSettings.Configuration(), cluster -> {
            this.vertx = cluster.result();

            if (cluster.succeeded()) {
                future.complete(this);
            } else {
                future.fail(cluster.cause());
            }
        });
    }

    public void start(String arg) {
        vertx.deployVerticle(arg, result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });
    }

    private void startAll() {
        Future<Void> patch = Future.future();
        Future<Void> authentication = Future.future();
        Future<Void> game = Future.future();
        Future<Void> web = Future.future();
        Future<Void> router = Future.future();

        CompositeFuture.all(patch, authentication, game, router).setHandler(result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
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
}
