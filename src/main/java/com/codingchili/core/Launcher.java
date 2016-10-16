package com.codingchili.core;

import com.codingchili.core.Authentication.Server;
import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.JsonFileStore;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.VertxSettings;
import com.codingchili.core.Logging.Model.ConsoleLogger;
import com.codingchili.core.Protocols.ClusterVerticle;
import io.vertx.core.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Launches all the components of the system on a single host.
 */
public class Launcher extends ClusterVerticle {
    private String[] nodes = {};
    private static final String[] NODE_LIST = {
            VERTICLE_LOGGING,
            VERTICLE_PATCHING,
            VERTICLE_WEBSERVER,
            VERTICLE_AUTHENTICATION,
            VERTICLE_REALM,
            VERTICLE_ROUTING
    };


    public static void main(String[] args) {
        VertxSettings settings = FileConfiguration.get(PATH_VERTX, VertxSettings.class);

        Vertx.clusteredVertx(settings.getOptions(), (clustered) -> {
            if (clustered.succeeded()) {
                Vertx vertx = clustered.result();

                vertx.deployVerticle(new Launcher(args), deployed -> {
                    if (deployed.succeeded()) {
                        addShutdownHook(vertx, deployed.result());
                    } else {
                        throw new RuntimeException(deployed.cause());
                    }
                });

            } else {
                System.out.println(ERROR_LAUNCHER_STARTUP);
            }
        });
    }

    private static void addShutdownHook(Vertx vertx, String id) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                final AtomicBoolean cleanup = new AtomicBoolean(true);

                while (cleanup.get()) {
                    vertx.undeploy(id, closed -> {

                        vertx.setTimer(3000, shutdown -> {
                            cleanup.set(false);
                        });
                    });
                }
            }
        });
    }

    public Launcher(String[] args) {
        this.nodes = args;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        try {
            super.init(vertx, context);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        if (enabled(VERTICLE_LOGGING) || enabled(VERTICLE_ALL)) {
            vertx.deployVerticle(new com.codingchili.core.Logging.Server(), deploy -> {
                if (deploy.succeeded()) {
                    startNodes();
                } else {
                    throw new RuntimeException(deploy.cause());
                }
            });
        } else {
            startNodes();
        }

        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) {
        System.out.println(Strings.ERRROR_LAUNCHER_SHUTDOWN);
        stop.complete();
    }

    private static void printHelp() {
        ConsoleLogger logger = new ConsoleLogger().setStyle(ConsoleLogger.Style.PRETTY);
        try {
            logger.log(JsonFileStore.readObject(Strings.PATH_VERTX));
        } catch (IOException e) {
            e.printStackTrace();
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
                vertx.deployVerticle(VERTICLE_LOGGING, result -> {
                    if (result.succeeded()) {
                        startAll();
                    }
                    if (result.failed()) {
                        throw new RuntimeException(result.cause());
                    }
                });
            } else if (isVerticle(node)) {
                vertx.deployVerticle(node, result -> {
                    if (result.failed()) {
                        throw new RuntimeException(result.cause());
                    }
                });
            }
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

        startServer(router, new com.codingchili.core.Routing.Server());
        startServer(patch, new com.codingchili.core.Patching.Server());
        startServer(authentication, new Server());
        startServer(web, new com.codingchili.core.Website.Server());
        startServer(realm, new com.codingchili.core.Realm.Server());
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
