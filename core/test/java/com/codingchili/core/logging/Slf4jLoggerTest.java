package com.codingchili.core.logging;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Verifies that SLF4J logging from dependencies is routed to the console logger.
 */
public class Slf4jLoggerTest {

    @Test
    public void testProviderIsLoaded() {
        Assert.assertTrue(LoggerFactory.getLogger(Slf4jLoggerTest.class) instanceof Slf4jLogger);
    }

    @Test
    public void testLog() {
        var logger = LoggerFactory.getLogger(Slf4jLoggerTest.class);
        logger.info("hello {}", "world");
        logger.warn("warning {} {}", 1, 2);
        logger.error("failure", new RuntimeException("test"));
        logger.debug("not printed");
    }
}
