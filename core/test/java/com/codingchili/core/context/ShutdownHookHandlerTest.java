package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.RejectedExecutionException;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;

/**
 * Tests for the shutdown hook handler.
 */
@RunWith(VertxUnitRunner.class)
public class ShutdownHookHandlerTest {
    private SystemContext context;

    @Before
    public void setUp() {
        context = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void stopMethodCalledInServices(TestContext test) {
        Async async = test.async();

        context.service(() -> new CoreService() {
            @Override
            public void stop(Future<Void> stop) {
                stop.complete();
                async.complete();
            }
        }).setHandler(done -> {
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
        context.blocking((blocking) -> {
            try {
                Thread.sleep(200);
                blocking.complete();
            } catch (InterruptedException e) {
                test.fail("Task interrupted!");
            }
        }, done -> async.complete());
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
        context.blocking((blocking) -> {
            try {
                Thread.sleep(250);
                test.fail("Blocking sleep was not forcefully interrupted.");
            } catch (InterruptedException e) {
                blocking.complete();
            }
        }, done -> async.complete());
        shutdown();

    }

    @Test
    public void serviceStopOverridesTimeout(TestContext test) {
        Async async = test.async();
        Configurations.system().setShutdownHookTimeout(25);

        context.service(() -> new CoreService() {
            @Override
            public void stop(Future<Void> stop) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    test.fail("Interrupted; should override timeout.");
                }
                stop.complete();
                untilVertxClosed(async);
            }
        }).setHandler(done -> {
            if (done.succeeded()) {
                shutdown();
            } else {
                test.fail(done.cause());
            }
        });
    }

    /**
     * Simulate the JVM shutdown hook.
     */
    private void shutdown() {
        new ShutdownHookHandler(context).start();
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
            } catch (RejectedExecutionException e) {
                async.complete();
                break;
            }
        }
    }
}
