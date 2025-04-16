package com.codingchili.core;

import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.*;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Launches all the components of the system on a single host. An application can be started using the
 * `Launcher.main(args)` or `Launcher.start(context)`. The context provides more configuration options
 * while the main can be used directly by passing the args from the main method.
 * <p>
 * Deploying a {@link CoreService} can also be done using the {@link CoreContext#service(Supplier)} methods.
 * To avoid the Launcher entirely, create a new {@link SystemContext} and use the deployments method
 * directly on that object.
 */
public class Launcher implements CoreService {
    private static Launcher instance;
    private static final ConsoleLogger logger = new ConsoleLogger(Launcher.class);
    private LaunchContext context;
    private CoreContext core;
    private Future<CommandResult> status;


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
    public static Future<CommandResult> start(LaunchContext context) {
        var launcher = new Launcher(context);
        return launcher.status;
    }

    /**
     * Creates a new launcher with the given launcher context.
     *
     * @param context the launcher context to use.
     */
    public Launcher(LaunchContext context) {
        this.context = context;

        logger.log(CoreStrings.getStartupText());

        status = context.execute();

        status.onSuccess(result -> {
            if (LauncherCommandResult.CONTINUE.equals(result)) {
                clusterIfEnabled(context);
            }
            if (LauncherCommandResult.SHUTDOWN.equals(result)) {
                exit();
            }
        }).onFailure(e -> {
            logger.log(throwableToString(e), Level.ERROR);
            logger.log(getCommandError(context.getCommand().orElse("")), Level.INFO);
            exit();
        });
        instance = this;
    }

    /**
     * @return the launcher that started the application, null if not started
     * using the default Launcher.
     */
    public static Launcher instance() {
        return instance;
    }

    /**
     * @return the launch context used to start the launcher, null if not started
     * using the default launcher.
     */
    public LaunchContext context() {
        return context;
    }

    void exit() {
        // no vertx context is initialized yet - but some thread pools
        // might need to be shut down after running the command.
        if (this.core == null) {
            ShutdownListener.publish(null);
        } else {
            this.core.close();
        }
        logger.close();
    }

    private void clusterIfEnabled(LaunchContext launcher) {
        try {
            var nodes = new ArrayList<>(context.services());

            if (launcher.settings().isClustered()) {
                SystemContext.clustered(clustered -> {
                    if (clustered.succeeded()) {
                        start(clustered.result(), nodes);
                    } else {
                        logger.log(ERROR_LAUNCHER_STARTUP, Level.ERROR);
                        exit();
                    }
                });
            } else {
                start(new SystemContext(), nodes);
            }
        } catch (Exception e) {
            logger.log(throwableToString(e), Level.ERROR);
            logger.log(getCommandError(context.getCommand().orElse("")), Level.INFO);
            exit();
        }
    }

    private void start(CoreContext core, List<String> nodes) {
        this.core = core;

        // the Launcher is a good example of a service.
        core.service(() -> this).onComplete(deployed -> {
            if (deployed.failed()) {
                throw new RuntimeException(deployed.cause());
            } else {
                deployServices(nodes);
            }
        });
    }

    /**
     * Deploy services in the order they are defined in the service block.
     */
    private void deployServices(List<String> nodes) {
        List<Future> deployments = new ArrayList<>();

        for (String node : nodes) {
            if (isDeployable(node)) {
                Promise<String> promise = Promise.promise();
                core.deploy(node).onComplete(promise);
                deployments.add(promise.future());
            }
        }
        CompositeFuture.all(deployments).onComplete(deployed -> {
            if (deployed.failed()) {
                for (var i = 0; i < deployments.size(); i++) {
                    var future = deployments.get(i);

                    if (future.failed()) {
                        logger.event(LOG_SERVICE_FAIL, Level.SEVERE)
                                .put(ID_SERVICE, nodes.get(i))
                                .put(LOG_ERROR, throwableToString(future.cause()))
                                .send();
                    }
                }
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
}
