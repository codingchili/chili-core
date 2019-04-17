package com.codingchili.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * Registered as a shutdown hook for the JVM and is used to clean up the context.
 */
public class ShutdownHookHandler extends Thread {
    private static final Map<SystemContext, ShutdownHookHandler> contexts = new HashMap<>();
    private SystemContext context;
    private Logger logger;

    /**
     * Registers a context for graceful shutdown as a JVM hook so that the context
     * will be closed gracefully on JVM exit.
     *
     * @param context the context to register the hook on, if a hook is already registered
     *                for the given context then an {@link IllegalStateException} is thrown.
     */
    static synchronized void register(SystemContext context) {
        if (contexts.containsKey(context)) {
            throw new IllegalStateException("Failed to register shutdown hook for context, hook already set.");
        } else {
            ShutdownHookHandler hook = new ShutdownHookHandler(context);
            contexts.put(context, hook);
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    /**
     * Unregister a context from the JVM shutdown hooks, this must be done if the context
     * is closed before the JVM exits.
     *
     * @param context the context to unregister shutdown hooks for.
     */
    static synchronized void unregister(SystemContext context) {
        Runtime.getRuntime().removeShutdownHook(contexts.remove(context));
    }

    /**
     * Removes all shutdown hooks.
     */
    private static synchronized void removeAll() {
        contexts.values().forEach(context -> {
            Runtime.getRuntime().removeShutdownHook(context);
        });
        contexts.clear();
    }

    /**
     * @param context the context that is to be shut down on JVM exit.
     */
    public ShutdownHookHandler(SystemContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
    }

    @Override
    public void run() {
        AtomicInteger timeout = new AtomicInteger(system().getShutdownHookTimeout());
        logger.log(LAUNCHER_SHUTDOWN_STARTED, Level.WARNING);
        try {
            // emit the shutdown event before closing.
            ShutdownListener.publish(context).setHandler(listeners -> {
                if (listeners.failed()) {
                    logger.onError(listeners.cause());
                }

                // stop all deployed services - prevent scheduling of new blocked tasks.
                context.stop().setHandler(stop -> {
                    try {
                        // wait for all blocking tasks that are scheduled to complete.
                        ExecutorService executor = context.getBlockingExecutor();
                        executor.shutdown();
                        executor.awaitTermination(timeout.get(), TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.onError(e);
                    }

                    // close the vertx instance.
                    context.vertx().close((close) -> {
                        if (close.failed()) {
                            logger.onError(close.cause());
                        }
                        timeout.set(0);
                    });

                });
            });
            while (timeout.decrementAndGet() > 0) {
                Thread.sleep(1L);
            }
            logger.close(); // flush pending tasks and enter sync mode.
            logger.log(LAUNCHER_SHUTDOWN_COMPLETED, Level.WARNING);
        } catch (InterruptedException e) {
            logger.onError(e);
        }
    }

    public static void main(String[] args) {
        new SystemContext().close();
    }
}
