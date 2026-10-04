package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.RemoteLogger;

/**
 * Tests for the shutdown hook handler.
 */
@RunWith(VertxUnitRunner.class)
public class ShutdownHookTest {
    private SystemContext context;

    @Rule
    public Timeout timeout = Timeout.seconds(10);

    @Before
    public void setUp() {
        // the system settings are shared by all tests.
        Configurations.system().setShutdownHookTimeout(5000);
        context = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void stopMethodCalledInServices(TestContext test) {
        Async async = test.async();

        context.service(() -> new CoreService() {
            @Override
            public void stop(Promise<Void> stop) {
                stop.complete();
                async.complete();
            }
        }).onComplete(done -> {
            if (done.succeeded()) {
                shutdown();
            } else {
                test.fail(done.cause());
            }
        });
    }

    @Test
    public void shutdownEventEmitted(TestContext test) {
        Async async = test.async();
        ShutdownListener.subscribe(core -> {
            async.complete();
            return Future.succeededFuture();
        });
        shutdown();
    }

    @Test
    public void blockingPoolAwaited(TestContext test) {
        Configurations.system().setShutdownHookTimeout(500);
        Async async = test.async();
        context.blocking(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                test.fail("Task interrupted!");
            }
        }).onComplete(done -> async.complete());
        shutdown();
    }

    @Test
    public void vertxInstanceClosed(TestContext test) {
        Async async = test.async();
        shutdown();
        untilVertxClosed(async);
    }

    @Test
    public void onBlockingPoolTimeoutForcefulExit(TestContext test) {
        Configurations.system().setShutdownHookTimeout(25);
        Async async = test.async();
        context.blocking(() -> {
            try {
                Thread.sleep(250);
                test.fail("Blocking sleep was not forcefully interrupted.");
            } catch (InterruptedException e) {
               //
            }
        }).onComplete(done -> async.complete());
        shutdown();

    }

    @Test
    public void serviceStopOverridesTimeout(TestContext test) {
        Async async = test.async();
        Configurations.system().setShutdownHookTimeout(25);

        context.service(() -> new CoreService() {
            @Override
            public void stop(Promise<Void> stop) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    test.fail("Interrupted; should override timeout.");
                }
                stop.complete();
                // poll off the event loop, blocking it here prevents vertx from closing.
                new Thread(() -> untilVertxClosed(async)).start();
            }
        }).onComplete(done -> {
            if (done.succeeded()) {
                shutdown();
            } else {
                test.fail(done.cause());
            }
        });
    }

    @Test
    public void shutdownCompletesWithoutWaitingForTheTimeout(TestContext test) throws InterruptedException {
        Configurations.system().setShutdownHookTimeout(5000);
        ShutdownHook hook = new ShutdownHook(context);
        long start = System.nanoTime();

        hook.start();
        hook.join(4000);

        test.assertFalse(hook.isAlive());
        test.assertTrue((System.nanoTime() - start) / 1_000_000 < 3000);
    }

    @Test
    public void blockingTasksCompleteBeforeVertxIsClosed(TestContext test) throws InterruptedException {
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        Async started = test.async();

        context.blocking(() -> {
            started.complete();
            try {
                Thread.sleep(300);
                completed.set(true);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        started.awaitSuccess(2000);

        ShutdownHook hook = new ShutdownHook(context);
        hook.start();
        hook.join(4000);

        test.assertTrue(completed.get());
        test.assertFalse(interrupted.get());
    }

    @Test
    public void servicesKeepRunningDuringTheShutdownDelay(TestContext test) throws InterruptedException {
        Configurations.system().setShutdownDelay(400);
        try {
            AtomicBoolean stopped = new AtomicBoolean(false);
            Async deployed = test.async();

            context.service(() -> new CoreService() {
                @Override
                public void stop(Promise<Void> stop) {
                    stopped.set(true);
                    stop.complete();
                }
            }).onComplete(test.asyncAssertSuccess(id -> deployed.complete()));
            deployed.awaitSuccess(2000);

            ShutdownHook hook = new ShutdownHook(context);
            long start = System.nanoTime();
            hook.start();

            Thread.sleep(150);
            // not ready, but still running.
            test.assertTrue(context.isShuttingDown());
            test.assertFalse(stopped.get());

            hook.join(4000);
            test.assertTrue(stopped.get());
            test.assertTrue((System.nanoTime() - start) / 1_000_000 >= 400);
        } finally {
            Configurations.system().setShutdownDelay(0);
        }
    }

    @Test
    public void explicitCloseUnregistersTheHook(TestContext test) {
        SystemContext other = new SystemContext();
        test.assertTrue(ShutdownHook.isRegistered(other));

        other.close().onComplete(test.asyncAssertSuccess(done ->
                test.assertFalse(ShutdownHook.isRegistered(other))));
    }

    @Test
    public void closedContextIsNotShutDownAgain(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess(done -> {
            long start = System.nanoTime();
            // as if the hook was not unregistered: it should find that there is nothing left to do.
            new ShutdownHook(context).run();
            test.assertTrue((System.nanoTime() - start) / 1_000_000 < 1000);
        }));
    }

    @Test
    public void shutdownStateIsSharedWithDerivedContexts(TestContext test) throws InterruptedException {
        SystemContext derived = new SystemContext(context) {
        };
        test.assertFalse(context.isShuttingDown());
        test.assertFalse(derived.isShuttingDown());

        ShutdownHook hook = new ShutdownHook(context);
        hook.start();
        hook.join(4000);

        test.assertTrue(context.isShuttingDown());
        test.assertTrue(derived.isShuttingDown());
    }

    @Test
    public void loggingAfterVertxIsClosedDoesNotFail(TestContext test) {
        RemoteLogger logger = new RemoteLogger(context, getClass());

        context.close().onComplete(test.asyncAssertSuccess(done -> {
            logger.log("after close", Level.INFO);
            logger.onError(new RuntimeException("after close"));
        }));
    }

    /**
     * Simulate the JVM shutdown hook.
     */
    private void shutdown() {
        new ShutdownHook(context).start();
    }

    private void untilVertxClosed(Async async) {
        while (true) {
            try {
                Thread.sleep(10);
                context.timer(10, (id) -> {
                    // the timer will fail to schedule if vertx is really closed.
                });
            } catch (InterruptedException e) {
                //
            } catch (IllegalStateException | RejectedExecutionException e) {
                async.complete();
                break;
            }
        }
    }
}
