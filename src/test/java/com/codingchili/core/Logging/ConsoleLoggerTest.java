package com.codingchili.core.Logging;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Context.SystemContext;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
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
