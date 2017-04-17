package com.codingchili.core;

import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Launches all the components of the system on a single host.
 */
public class Launcher implements CoreService {
    private static final ConsoleLogger logger = new ConsoleLogger();
    private static List<String> nodes = new ArrayList<>();
    private LaunchContext context;

    /**
     * Starts the launcher with the given arguments.
     *
     * @param args specifies which commands the launcher will execute.
     */
    public static void main(String[] args) {
        new Launcher(new LaunchContext(args));
    }

    public Launcher(LaunchContext context) {
        Future<Void> future = Future.future();
        this.context = context;

        logger.log(CoreStrings.getStartupText(context.settings().getVersion()), Level.STARTUP);

        new LauncherCommandExecutor().execute(future, context.args());
        future.setHandler(done -> {
            try {
                if (done.failed()) {
                    nodes = context.block(context.args());
                    nodes = new ArrayList<>(nodes);
                    cluster();
                } else {
                    exit();
                }
            } catch (Throwable e) {
                logger.log(e.getMessage(), Level.SEVERE);
                logger.log(getCommandError(context.command()), Level.INFO);
                exit();
            }
        });
    }

    void exit() {
        logger.reset();
        context.vertx().close();
    }

    private void cluster() {
        Vertx.clusteredVertx(system().getOptions(), (clustered) -> {
            if (clustered.succeeded()) {
                context.setVertx(clustered.result());
                init(context);

                context.service(this, deployed -> {
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
            boolean isDeployable = Verticle.class.isAssignableFrom(clazz) ||
                    CoreHandler.class.isAssignableFrom(clazz) ||
                    CoreService.class.isAssignableFrom(clazz) ||
                    CoreListener.class.isAssignableFrom(clazz);

            if (!isDeployable) {
                logger.log(getUnsupportedDeployment(node), Level.SEVERE);
                exit();
            }
            return isDeployable;
        } catch (ClassNotFoundException e) {
            logger.log(getNodeNotFound(node), Level.SEVERE);
            exit();
            return false;
        }
    }

    private void addShutdownHook(Vertx vertx) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            vertx.deploymentIDs().forEach(vertx::undeploy);
            try {
                Thread.sleep(Configurations.system().getShutdownHookTimeout());
            } catch (InterruptedException ignored) {
            }
            logger.log(ERRROR_LAUNCHER_SHUTDOWN, Level.SEVERE);
            logger.reset();
        }));
    }

    @Override
    public void init(CoreContext core) {
        Configurations.initialize(core);
        Delay.initialize(core);
    }
}
