package com.codingchili.core.context;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the startup listener.
 */
@RunWith(VertxUnitRunner.class)
public class StartupListenerTest {
    private CoreContext core;

    @After
    public void tearDown(TestContext test) {
        if (core != null) {
            core.close(test.asyncAssertSuccess());
        }
    }

    @Test
    public void startupNotified(TestContext test) {
        Async async = test.async();

        StartupListener.subscibe(core -> {
            test.assertNotNull(core);
            async.complete();
        });
        core = new SystemContext();
    }
}