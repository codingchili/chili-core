package com.codingchili.core.logging;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.testing.ContextMock;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Verifies the console logger can write.
 */
@RunWith(VertxUnitRunner.class)
public class ConsoleLoggerTest {
    private ConsoleLogger logger;
    private SystemContext context;

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
        logger = new ConsoleLogger(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testLogMessage() {
        logger.log("line");
        logger.log("line", Level.INFO);
        logger.log(new JsonObject());
    }

    @Test
    public void testLogNotInitialized() {
        new ConsoleLogger().log("");
    }
}
