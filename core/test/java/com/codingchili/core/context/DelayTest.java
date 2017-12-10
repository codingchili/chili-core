package com.codingchili.core.context;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.context.exception.SystemNotInitializedException;
import com.codingchili.core.testing.ContextMock;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Verifies that the STARTUP_DELAY system is working, is required for some tests.
 */
@RunWith(VertxUnitRunner.class)
public class DelayTest {
    private CoreContext context;

    @Before
    public void setUp() {
        this.context = new ContextMock() {
            @Override
            public SystemSettings system() {
                SystemSettings settings = new SystemSettings();
                settings.setShutdownLogTimeout(1);
                return settings;
            }
        };
        Delay.initialize(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }
    @Test
    public void testDelayFuture(TestContext test) {
        Async async = test.async();
        Future<Void> future = Future.future();
        future.setHandler(result -> async.complete());

        Delay.forMS(future, 1);
    }

    @Test
    public void testDelayForShutdown(TestContext test) {
        Async async = test.async();
        Future<Void> future = Future.future();
        future.setHandler(result -> async.complete());

        Delay.forShutdown(future);
    }

    @Test
    public void testDelayNotInitialized(TestContext test) {
        try {
            Delay.initialize(null);
            Delay.forMS(Future.future(), 1);
        } catch (SystemNotInitializedException e) {
            test.assertTrue(e.getMessage().contains(Delay.class.getSimpleName()));
        }
    }
}
