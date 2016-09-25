import Configuration.JsonFileStore;
import Configuration.Strings;
import Logging.Model.ConsoleLogger;
import Protocols.ClusterVerticle;
import io.vertx.core.*;

import java.io.IOException;
import java.util.List;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Launches all the components of the system on a single host.
 */
public class Launcher extends ClusterVerticle {
    private List<String> nodes;
    private static final String[] NODE_LIST = {
            VERTICLE_LOGGING,
            VERTICLE_PATCHING,
            VERTICLE_WEBSERVER,
            VERTICLE_AUTHENTICATION,
            VERTICLE_REALM};

    @Override
    public void init(Vertx vertx, Context context) {
        try {
            super.init(vertx, context);
            this.nodes = context.processArgs();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        if (nodes.size() <= 3) {
            printHelp();
        } else {
            deploy();
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

    private void deploy() {
        if (enabled(VERTICLE_LOGGING) || enabled(VERTICLE_ALL)) {
            vertx.deployVerticle(new Logging.Server(), deploy -> {
                if (deploy.succeeded()) {
                    startNodes();
                } else {
                    throw new RuntimeException(deploy.cause());
                }
            });
        } else {
            startNodes();
        }
    }


    private boolean enabled(String server) {
        for (String node : nodes) {
            if (node.equals(server)) {
                return true;
            }
        }
        return false;
    }

    private void startNodes() {
        for (String node : nodes) {
            if (node.equals(VERTICLE_ALL)) {
                startAll();
            } else if (!node.equals(VERTICLE_LOGGING) && isVerticle(node))
                vertx.deployVerticle(node, result -> {
                    if (result.failed()) {
                        throw new RuntimeException(result.cause());
                    }
                });
        }
    }

    private boolean isVerticle(String node) {
        for (String name : NODE_LIST) {
            if (name.equals(node)) {
                return true;
            }
        }
        return false;
    }

    private void startAll() {
        Future<Void> patch = Future.future();
        Future<Void> authentication = Future.future();
        Future<Void> realm = Future.future();
        Future<Void> web = Future.future();
        Future<Void> router = Future.future();

        CompositeFuture.all(patch, authentication, realm, router, web).setHandler(result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });

        startServer(router, new Routing.Server());
        startServer(patch, new Patching.Server());
        startServer(authentication, new Authentication.Server());
        startServer(web, new Website.Server());
        startServer(realm, new Realm.Server());
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
