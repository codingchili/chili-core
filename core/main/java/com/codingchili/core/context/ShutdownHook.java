package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.logging.*;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * Registered as a shutdown hook for the JVM and is used to clean up the context.
 * <p>
 * The shutdown is performed in order: the context is marked as shutting down (a readiness check fails), after the
 * shutdown delay the shutdown event is published to subscribers, deployed services are stopped,
 * blocking tasks that are running are awaited and then vertx is closed, which interrupts anything still running.
 * Each step starts when the previous one has completed, and the hook returns as soon as the last step completes.
 * The whole shutdown is bounded by the shutdown hook timeout in the system settings: when the time is up the blocking
 * tasks that are still running are interrupted, and the JVM exits without waiting for the rest.
 */
public class ShutdownHook extends Thread {
    private static final Map<Vertx, ShutdownHook> contexts = new HashMap<>();
    private final SystemContext context;
    private final Logger logger;

    /**
     * Registers a context for graceful shutdown as a JVM hook so that the context
     * will be closed gracefully on JVM exit.
     *
     * @param context the context to register the hook on, if a hook is already registered
     *                it has no effect.
     */
    static synchronized void register(SystemContext context) {
        if (contexts.containsKey(context.vertx)) {
            // context already registered - no action.
        } else {
            ShutdownHook hook = new ShutdownHook(context);
            contexts.put(context.vertx, hook);
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
        ShutdownHook handler = contexts.remove(context.vertx);

        if (handler != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(handler);
            } catch (IllegalStateException e) {
                // the JVM is already shutting down: the hook is running, or will run and find nothing to do.
            }
        }
    }

    /**
     * @param context the context to check.
     * @return true if the context has a hook that will shut it down when the JVM exits.
     */
    static synchronized boolean isRegistered(SystemContext context) {
        return contexts.containsKey(context.vertx);
    }

    private static synchronized void forget(ShutdownHook hook) {
        contexts.values().remove(hook);
    }

    /**
     * @param context the context that is to be shut down on JVM exit.
     */
    public ShutdownHook(SystemContext context) {
        this.context = context;
        this.logger = new RemoteLogger(context, getClass());
    }

    @Override
    public void run() {
        if (!context.beginShutdown()) {
            // closed before the JVM exited, or already being shut down by another hook.
            forget(this);
            return;
        }
        long timeout = system().getShutdownHookTimeout();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        CountDownLatch closed = new CountDownLatch(1);

        logger.log(LAUNCHER_SHUTDOWN_STARTED, Level.WARNING);

        // services keep running while the context reports that it's shutting down, to let traffic move away.
        delay(Math.min(system().getShutdownDelay(), remainingMS(deadline)))
                // emit the shutdown event before closing, a failing subscriber or service does not prevent the shutdown.
                .compose(done -> ShutdownListener.publish(context))
                .otherwise(this::failed)
                // stop all deployed services: listeners stop accepting requests and in-flight requests complete.
                .compose(done -> context.stop())
                .otherwise(this::failed)
                // blocking tasks that are running are allowed to complete: closing vertx interrupts them.
                .compose(done -> context.drain(remainingMS(deadline)))
                .compose(done -> {
                    int interrupted = context.blockingTasks();
                    if (interrupted > 0) {
                        logger.log(getShutdownInterrupted(interrupted, timeout), Level.WARNING);
                    }
                    return context.vertx().close();
                })
                .onComplete(done -> {
                    if (done.failed()) {
                        logger.onError(done.cause());
                    }
                    closed.countDown();
                });

        boolean completed = await(closed, remainingMS(deadline));

        // cleanup of listeners.
        ShutdownListener.clear();
        forget(this);

        logger.close(); // flush pending tasks and enter sync mode.
        logger.log((completed) ? LAUNCHER_SHUTDOWN_COMPLETED : getShutdownTimedOut(timeout), Level.WARNING);
    }

    private Future<Void> delay(long ms) {
        return (ms > 0) ? context.vertx().timer(ms, TimeUnit.MILLISECONDS).mapEmpty() : Future.succeededFuture();
    }

    private Void failed(Throwable e) {
        logger.onError(e);
        return null;
    }

    private static long remainingMS(long deadline) {
        return Math.max(0, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
    }

    private static boolean await(CountDownLatch latch, long timeoutMS) {
        try {
            return latch.await(timeoutMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void main(String[] args) {
        new SystemContext().close();
    }
}
