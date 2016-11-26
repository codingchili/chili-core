package com.codingchili.core.logging;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Verifies the console logger can write.
 */
@RunWith(VertxUnitRunner.class)
public class ConsoleLoggerTest {
    private ConsoleLogger logger;

    @Before
    public void setUp() {
        SystemContext context = new ContextMock(Vertx.vertx());
        logger = new ConsoleLogger(context);
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
