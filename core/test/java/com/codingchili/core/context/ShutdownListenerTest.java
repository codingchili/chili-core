package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Tests for the shutdown listener.
 */
@RunWith(VertxUnitRunner.class)
public class ShutdownListenerTest {
    private CoreContext core;

    @Rule
    public Timeout timeout = new Timeout(4, TimeUnit.SECONDS);

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
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void shutdownNotifiedJVMBreaker(TestContext test) {
        Async async = test.async();
        ShutdownListener.subscribe((core) -> {
            test.assertFalse(core.isPresent());
            async.complete();
            return Future.succeededFuture();
        });
        ShutdownListener.publish(null);
    }
}