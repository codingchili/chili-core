package com.codingchili.core.context;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.exception.SystemNotInitializedException;
import com.codingchili.core.testing.ContextMock;

/**
 * Verifies that the STARTUP_DELAY system is working, is required for some tests.
 */
@RunWith(VertxUnitRunner.class)
public class DelayTest {
    private static CoreContext context;

    @BeforeClass
    public static void setUp() {
        context = new ContextMock();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }
    @Test
    public void testDelayFuture(TestContext test) {
        Delay.forMS(1).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testDelayNotInitialized(TestContext test) {
        try {
            Delay.forMS(1);
        } catch (SystemNotInitializedException e) {
            test.assertTrue(e.getMessage().contains(Delay.class.getSimpleName()));
        }
    }
}
