package com.codingchili.core.Logging;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Testing.LoggerMock;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *
 * Verifies the creation of logging events in DefaultLogger.
 */
@RunWith(VertxUnitRunner.class)
public class DefaultLoggerTest {
    private LoggerMock logger;

    @Before
    public void setUp() {
        this.logger = new LoggerMock((line) -> {});
    }

    @Test
    public void testGenerateLogEvent(TestContext context) {
        JsonObject event = logger.event(LOG_MESSAGE, Level.SEVERE);

        context.assertEquals(PROTOCOL_LOGGING, event.getString(ID_ROUTE));
        context.assertEquals(LOG_MESSAGE, event.getString(LOG_EVENT));
        context.assertEquals(Level.SEVERE.toString(), event.getString(LOG_LEVEL));
        context.assertTrue(event.containsKey(LOG_TIME));
    }

}
