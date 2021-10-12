package com.codingchili.core.security;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.*;

import com.codingchili.core.security.exception.ThreadGuardException;

/**
 * Provides test contexts.
 */
@RunWith(VertxUnitRunner.class)
public class ThreadGuardTest {
    private static final int THREAD_POOL = 32;
    private static ExecutorService executor;

    @BeforeClass
    public static void setUp() {
        executor = Executors.newFixedThreadPool(THREAD_POOL);
    }

    @AfterClass
    public static void tearDown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                throw new RuntimeException("Executor did not shutdown, tests may fail.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void concurrencySafe(TestContext test) {
        var guard = ThreadGuard.concurrency();
        guard.error((threads) -> test.fail("Error was invoked."));
        invoke(guard);
    }

    @Test
    public void concurrencyFailure(TestContext test) {
        var guard = ThreadGuard.concurrency();
        var async = test.async();

        // when guard is executed concurrently an error is triggered.
        guard.error((threads) -> async.complete());

        concurrent(guard);
        concurrent(guard);
    }

    @Test
    public void watcherSafe() {
        var guard = ThreadGuard.watcher(1);
        invoke(guard);
    }

    @Test
    public void watcherFailure(TestContext test) throws Exception {
        var guard = ThreadGuard.watcher(1);
        var async = test.async();

        // when guard is visited using two different threads an error is triggered.
        guard.error((threads) -> async.complete());

        visit(guard);
        visit(guard);
    }

    @Test
    public void throwing() throws Exception {
        var guard = ThreadGuard.watcher(1);
        visit(guard);
        try {
            guard.protect(() -> {
            });
        } catch (ThreadGuardException e) {
            ThreadGuard.log(e.threads());
        }
    }

    @Test
    public void resetGate(TestContext test) throws Exception {
        var guard = ThreadGuard.watcher(1);
        var async = test.async(2);

        // ensure that calling reset triggers the error handlers again.
        guard.error((thread) -> {
            if (async.count() == 0) {
                async.complete();
            } else {
                async.countDown();
                guard.reset();
            }
        });

        visit(guard);
        visit(guard);
        visit(guard);
        visit(guard);
    }

    @Test
    public void reset(TestContext test) throws Exception {
        var guard = ThreadGuard.watcher(1);
        var async = test.async();

        // ensure error handler is only triggered once without calling reset.
        guard.error((thread) -> async.complete());

        visit(guard);
        visit(guard);
        visit(guard);
    }

    private void invoke(ThreadGuard guard) {
        guard.protect(() -> {});
        guard.protect(() -> {});
    }

    private void visit(ThreadGuard guard) throws Exception {
        executor.submit(() -> guard.protect(() -> {
        })).get(2, TimeUnit.SECONDS);
    }

    private void concurrent(ThreadGuard guard) {
        executor.submit(() -> guard.protect(() -> {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
