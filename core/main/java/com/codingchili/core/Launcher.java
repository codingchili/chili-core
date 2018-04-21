package com.codingchili.core;

import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;

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
 * <p>
 * Launches all the components of the system on a single host.
 */
public class Launcher implements CoreService {
    private static final ConsoleLogger logger = new ConsoleLogger(Launcher.class);
    private static List<String> nodes = new ArrayList<>();
    private CoreContext core;

    /**
     * Creates a new launcher with the given launcher context.
     *
     * @param context the launcher context to use.
     */
    public Launcher(LaunchContext context) {
        Future<CommandResult> future = Future.future();

        logger.log(CoreStrings.getStartupText(), Level.STARTUP);

        context.getExecutor().execute(future, context.args());
        future.setHandler(done -> {
            CommandResult result = done.result();
            try {
                if (done.succeeded()) {
                    if (CommandResult.CONTINUE.equals(result)) {
                        nodes = context.block(context.args());
                        nodes = new ArrayList<>(nodes);
                        clusterIfEnabled(context);
                    }
                    if (CommandResult.SHUTDOWN.equals(result)) {
                        exit();
                    }
                } else {
                    if (done.cause() != null) {
                        throw done.cause();
                    } else {
                        throw new CoreRuntimeException("Unknown cause: ");
                    }
                }
                // else: the future succeeded with "true" - no action.
            } catch (Throwable e) {
                logger.log(throwableToString(e), Level.ERROR);
                logger.log(getCommandError(context.getCommand()), Level.INFO);
                exit();
            }
        });
    }

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
     *
     * @param context contains the launcher args and settings.
     */
    public static void start(LaunchContext context) {
        new Launcher(context);
    }

    void exit() {
        // no vertx context is initialized yet - but some thread pools
        // might need to be shut down after running the command.
        if (this.core == null) {
            ShutdownListener.publish();
        } else {
            this.core.close();
        }
    }

    private void clusterIfEnabled(LaunchContext launcher) {
        if (launcher.settings().isClustered()) {
            SystemContext.clustered(clustered -> {
                if (clustered.succeeded()) {
                    start(clustered.result());
                } else {
                    logger.log(ERROR_LAUNCHER_STARTUP, Level.ERROR);
                    exit();
                }
            });
        } else {
            start(new SystemContext());
        }
    }

    private void start(CoreContext core) {
        this.core = core;

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
        List<Future> deployments = new ArrayList<>();
        //String node = nodes.get(0);

        for (String node : nodes) {
            if (isDeployable(node)) {
                Future<String> future = Future.future();
                core.deploy(node).setHandler(future);
                deployments.add(future);
            }
        }
        CompositeFuture.all(deployments).setHandler(deployed -> {
           if (deployed.failed()) {
               logger.onError(deployed.cause());
               exit();
           }
        });
    }

    private boolean isDeployable(String node) {
        try {
            Class<?> clazz = Class.forName(node);
            boolean isDeployable = Verticle.class.isAssignableFrom(clazz) ||
                    CoreHandler.class.isAssignableFrom(clazz) ||
                    CoreService.class.isAssignableFrom(clazz) ||
                    CoreListener.class.isAssignableFrom(clazz);

            if (!isDeployable) {
                logger.log(getUnsupportedDeployment(node), Level.ERROR);
                exit();
            }
            return isDeployable;
        } catch (ClassNotFoundException e) {
            logger.log(getNodeNotFound(node), Level.ERROR);
            exit();
            return false;
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(LAUNCHER_SHUTDOWN_STARTED, Level.ERROR);
            try {
                ShutdownListener.publish();
                Thread.sleep(system().getShutdownHookTimeout());
                logger.log(LAUNCHER_SHUTDOWN_COMPLETED, Level.ERROR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
