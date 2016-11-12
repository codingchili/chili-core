package com.codingchili.core.Context;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Configuration.System.SystemSettings;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Verifies that the delay system is working, is required for some tests.
 */
@RunWith(VertxUnitRunner.class)
public class DelayTest {
    private CoreContext context;

    @Before
    public void setUp() {
        this.context = new ContextMock(Vertx.vertx()) {
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
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testDelayAsync(TestContext test) {
        Async async = test.async();
        Delay.forMS(async, 1);
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
}
