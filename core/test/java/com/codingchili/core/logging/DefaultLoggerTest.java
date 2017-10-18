package com.codingchili.core.logging;

import com.codingchili.core.testing.LoggerMock;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Verifies the creation of logging events in DefaultLogger.
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
        LogMessage event = logger.event(LOG_MESSAGE, Level.ERROR);

        context.assertEquals(LOG_MESSAGE, event.toJson().getString(LOG_EVENT));
        context.assertEquals(Level.ERROR.toString(), event.toJson().getString(LOG_LEVEL));
        context.assertTrue(event.toJson().containsKey(LOG_TIME));
    }

}
