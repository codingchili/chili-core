package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * Tests for the shutdown listener.
 */
@RunWith(VertxUnitRunner.class)
public class ShutdownListenerTest {
    private CoreContext core;

    @Before
    public void setUp(TestContext test) {
        core = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void shutdownNotified(TestContext test) {
        Async async = test.async();
        ShutdownListener.subscribe(() -> {
            async.complete();
            return Future.succeededFuture();
        });
        core.close(test.asyncAssertSuccess());
    }

}