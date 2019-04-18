package com.codingchili.core.logging;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.fusesource.jansi.Ansi;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.testing.ContextMock;

/**
 * Verifies the console logger can write.
 */
@RunWith(VertxUnitRunner.class)
public class ConsoleLoggerTest {
    private static ConsoleLogger logger;
    private static SystemContext context;

    @BeforeClass
    public static void setUp() {
        context = new ContextMock();
        logger = new ConsoleLogger(context, ConsoleLoggerTest.class);
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
        logger.close();
    }

    @Test
    public void testLogMessage() {
        logger.log("line");
        logger.log("line 2", Level.INFO);
        logger.event("wowza", Level.INFO).send("line 3");
    }

    @Test
    public void testLogCustomUnregistered() {
        logger.log("unregistered", create("unregistered"));
    }

    @Test
    public void testLogCustomRegistered() {
        LogLevel registered = create("registered");
        LogLevel.register(registered);
        logger.log("registered", registered);
    }

    @Test
    public void testLogNotInitialized() {
        new ConsoleLogger(getClass()).log("");
    }

    private static LogLevel create(String name) {
        return new LogLevel() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Ansi.Color getColor() {
                return Ansi.Color.BLUE;
            }
        };
    }
}
