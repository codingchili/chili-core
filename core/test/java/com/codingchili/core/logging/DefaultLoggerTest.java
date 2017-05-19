package com.codingchili.core.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.LoggerMock;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Verifies the creation of logging events in DefaultLogger.
 */
@RunWith(VertxUnitRunner.class)
public class DefaultLoggerTest {
    private LoggerMock logger;

    @Before
    public void setUp() {
        this.logger = new LoggerMock((line) -> {
        });
    }

    @Test
    public void testGenerateLogEvent(TestContext context) {
        JsonObject event = logger.event(LOG_MESSAGE, Level.SEVERE);

        context.assertEquals(PROTOCOL_LOGGING, event.getString(PROTOCOL_ROUTE));
        context.assertEquals(LOG_MESSAGE, event.getString(LOG_EVENT));
        context.assertEquals(Level.SEVERE.toString(), event.getString(LOG_LEVEL));
        context.assertTrue(event.containsKey(LOG_TIME));
    }

}
