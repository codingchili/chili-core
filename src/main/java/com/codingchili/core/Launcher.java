package com.codingchili.core;

import io.vertx.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Context.*;
import com.codingchili.core.Exception.*;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Logging.ConsoleLogger;
import com.codingchili.core.Logging.Level;
import com.codingchili.core.Protocol.ClusterNode;
import com.codingchili.core.Storage.StorageLoader;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Launches all the components of the system on a single host.
 */
public class Launcher extends ClusterNode {
    private static final ConsoleLogger logger = new ConsoleLogger();
    private static List<String> nodes = new ArrayList<>();
    private static CoreContext context;

    /**
     * Starts the launcher with the given arguments.
     *
     * @param args specifies which commands the launcher will execute.
     */
    public static void main(String[] args) {
        new Launcher(new LaunchContext(args));
    }

    Launcher(LaunchContext context) {
        logger.log(Strings.getStartupText(context.settings().getVersion()), Level.STARTUP);

        CommandExecutor executor = new CommandExecutor(context);
        try {
            if (executor.isHandled()) {
                exit();
            } else {
                nodes = context.block(context.args());
                nodes = nodes.stream().map(node -> node).collect(Collectors.toList());

                cluster();
            }
        } catch (CoreException e) {
            logger.log("\t\t" + e.getMessage(), Level.SEVERE);
            logger.log("\t\t" + executor.getError(), Level.SEVERE);
            exit();
        }
    }

    void exit() {
        logger.reset();
        System.exit(0);
    }

    private void cluster() {
        Vertx.clusteredVertx(new VertxOptions(), (clustered) -> {
            if (clustered.succeeded()) {
                context = new LaunchContext(clustered.result());

                context.deploy(this, deployed -> {
                    if (deployed.failed()) {
                        throw new RuntimeException(deployed.cause());
                    } else {
                        addShutdownHook(clustered.result());
                        deployServices(nodes);
                    }
                });
            } else {
                logger.log(ERROR_LAUNCHER_STARTUP, Level.SEVERE);
                exit();
            }
        });
    }

    /**
     * Deploy services in the order they are defined in the service block.
     */
    private void deployServices(List<String> nodes) {
        String node = nodes.get(0);

        if (isVerticle(node)) {
            context.deploy(nodes.get(0), deploy -> {
                if (deploy.succeeded()) {
                    nodes.remove(0);

                    if (nodes.size() > 0) {
                        deployServices(nodes);
                    }
                } else {
                    throw new RuntimeException(deploy.cause());
                }
            });
        } else {
            nodes.remove(0);
        }
    }

    private boolean isVerticle(String node) {
        try {
            Class<?> clazz = Class.forName(node);
            boolean isClusteredVerticle = clazz.getSuperclass().equals(ClusterNode.class);

            if (!isClusteredVerticle) {
                logger.log(Strings.getNodeNotVerticle(node), Level.SEVERE);
                exit();
            }

            return isClusteredVerticle;
        } catch (ClassNotFoundException e) {
            logger.log(Strings.getNodeNotFound(node), Level.SEVERE);
            exit();
            return false;
        }
    }

    private static void addShutdownHook(Vertx vertx) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                AtomicBoolean cleanup = new AtomicBoolean(true);
                Configurations.shutdown();

                vertx.deploymentIDs().forEach(vertx::undeploy);

                while (cleanup.get()) {
                    context.timer(context.system().getShutdownHookTimeout(), handler -> cleanup.set(false));
                }

                logger.log(ERRROR_LAUNCHER_SHUTDOWN, Level.SEVERE);
                logger.reset();
            }
        });
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        initialize();
        start.complete();
    }

    private void initialize() {
        Configurations.initialize(context);
        Delay.initialize(context);
        StorageLoader.initialize(context);
    }

    @Override
    public void stop(Future<Void> stop) {
        stop.complete();
    }
}
