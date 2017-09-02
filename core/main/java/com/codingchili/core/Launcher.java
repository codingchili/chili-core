package com.codingchili.core;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.Delay;
import com.codingchili.core.context.LaunchContext;
import com.codingchili.core.context.LauncherCommandExecutor;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;

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
    private SystemContext core;

    /**
     * Starts the launcher with the given arguments.
     *
     * @param args specifies which commands the launcher will execute.
     */
    public static void main(String[] args) {
        new Launcher(new LaunchContext(args));
    }

    /**
     * Starts the launcher with the given context.
     * @param context contains the launcher args and settings.
     */
    public static void start(LaunchContext context) {
        new Launcher(context);
    }

    private Launcher(LaunchContext context) {
        Future<Void> future = Future.future();

        logger.log(CoreStrings.getStartupText(context.settings().getVersion()), Level.STARTUP);

        new LauncherCommandExecutor().execute(future, context.args());
        future.setHandler(done -> {
            try {
                // the CommandExecutor has failed to execute the command.
                if (done.failed()) {
                    nodes = context.block(context.args());
                    nodes = new ArrayList<>(nodes);
                    clusterIfEnabled(context.settings());
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
    }

    private void clusterIfEnabled(LauncherSettings settings) {
        if (settings.isClustered()) {
            Vertx.clusteredVertx(system().getOptions(), (clustered) -> {
                if (clustered.succeeded()) {
                    start(clustered.result());
                } else {
                    logger.log(ERROR_LAUNCHER_STARTUP, Level.SEVERE);
                    exit();
                }
            });
        } else {
            start(Vertx.vertx());
        }
    }

    private void start(Vertx vertx) {
        core = new SystemContext(vertx);
        Configurations.initialize(core);
        Delay.initialize(core);

        // the Launcher is a good example of a service.
        core.service(() -> this).setHandler(deployed -> {
            if (deployed.failed()) {
                throw new RuntimeException(deployed.cause());
            } else {
                addShutdownHook();
                deployServices(nodes);
            }
        });
    }

    /**
     * Deploy services in the order they are defined in the service block.
     */
    private void deployServices(List<String> nodes) {
        String node = nodes.get(0);

        if (isDeployable(node)) {
            core.deploy(nodes.get(0)).setHandler(deploy -> {
                if (deploy.succeeded()) {
                    nodes.remove(0);

                    if (nodes.size() > 0) {
                        deployServices(nodes);
                    }
                } else {
                    throw new RuntimeException(deploy.cause());
                }
            });
        }
    }

    private boolean isDeployable(String node) {
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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(system().getShutdownHookTimeout());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.log(ERRROR_LAUNCHER_SHUTDOWN, Level.SEVERE);
            logger.reset();
        }));
    }
}
