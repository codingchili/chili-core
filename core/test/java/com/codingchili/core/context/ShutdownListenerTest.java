package com.codingchili.core.context;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
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

    @Test
    public void shutdownNotified(TestContext test) {
        Async async = test.async();
        ShutdownListener.subscribe(async::complete);
        core.close(test.asyncAssertSuccess());
    }

}