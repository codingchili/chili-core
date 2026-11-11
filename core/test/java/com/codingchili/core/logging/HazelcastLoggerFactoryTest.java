package com.codingchili.core.logging;

import com.hazelcast.config.Config;
import com.hazelcast.logging.ILogger;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.logging.HazelcastLoggerFactory.PROPERTY_LOGGING_CLASS;
import static com.codingchili.core.logging.HazelcastLoggerFactory.PROPERTY_LOGGING_TYPE;

/**
 * Tests that Hazelcast's logging is forwarded to chili-core's logger and installed only when not configured.
 */
public class HazelcastLoggerFactoryTest {
    private final List<JsonObject> events = new CopyOnWriteArrayList<>();
    private final HazelcastLoggerFactory factory = new HazelcastLoggerFactory(new ConsoleLogger(getClass()) {
        @Override
        public Logger log(JsonObject data) {
            events.add(data);
            return this;
        }
    });

    @After
    public void tearDown() {
        System.clearProperty(PROPERTY_LOGGING_CLASS);
        System.clearProperty(PROPERTY_LOGGING_TYPE);
    }

    @Test
    public void forwardsToLogger() {
        ILogger logger = factory.getLogger("com.hazelcast.internal.cluster.ClusterService");
        logger.info("[172.18.0.2]:5701 [bunnies] [5.6.0] \n\nMembers {size:2, ver:2} [\n" +
                "\tMember [172.18.0.2]:5701 - a this\n\tMember [172.18.0.3]:5701 - b\n]\n");

        Assert.assertEquals(1, events.size());
        JsonObject event = events.get(0);
        Assert.assertEquals("hazelcast", event.getString(LOG_EVENT));
        Assert.assertEquals("ClusterService", event.getString(LOG_SOURCE));
        Assert.assertEquals(Level.INFO.name(), event.getString(LOG_LEVEL));
        Assert.assertEquals("Members {size:2, ver:2} [Member [172.18.0.2]:5701 - a this, " +
                "Member [172.18.0.3]:5701 - b]", event.getString(LOG_MESSAGE));
    }

    @Test
    public void prefixWithoutAddressRemoved() {
        Assert.assertEquals("Resolving domain name 'hub'",
                HazelcastLoggerFactory.format("[bunnies] [5.6.0] Resolving domain name 'hub'"));
        Assert.assertEquals("[hub]:5701 is STARTED",
                HazelcastLoggerFactory.format("[hub]:5701 [bunnies] [5.6.0] [hub]:5701 is STARTED"));
    }

    @Test
    public void mapsLevels() {
        ILogger logger = factory.getLogger("com.hazelcast.test.Levels");
        logger.severe("severe", new IllegalStateException("cause"));
        logger.warning("warning");
        logger.info("info");

        Assert.assertEquals(List.of(Level.ERROR.name(), Level.WARNING.name(), Level.INFO.name()),
                events.stream().map(event -> event.getString(LOG_LEVEL)).toList());
        Assert.assertTrue(events.get(0).getString(LOG_STACKTRACE).contains("cause"));
        Assert.assertEquals(Level.DEBUG, DependencyLogs.level(java.util.logging.Level.FINEST));
    }

    @Test
    public void fineHiddenByDefault() {
        ILogger logger = factory.getLogger("com.hazelcast.test.Fine");
        logger.fine("fine");
        logger.finest("finest");

        Assert.assertTrue(events.isEmpty());
        Assert.assertFalse(logger.isFineEnabled());
        Assert.assertTrue(logger.isInfoEnabled());
    }

    @Test
    public void fineShownWhenTheCategoryLogsFine() {
        java.util.logging.Logger category = java.util.logging.Logger.getLogger("com.hazelcast.test.Verbose");
        category.setLevel(java.util.logging.Level.FINE);
        try {
            factory.getLogger("com.hazelcast.test.Verbose.Child").fine("fine");
            Assert.assertEquals(Level.DEBUG.name(), events.get(0).getString(LOG_LEVEL));
        } finally {
            category.setLevel(null);
        }
    }

    @Test
    public void quietCategoriesOnlyShowWarnings() {
        ILogger logo = factory.getLogger("com.hazelcast.system.logo");
        logo.info("ascii art");
        logo.warning("a warning");
        factory.getLogger("com.hazelcast.system").info("[bunnies] [5.6.0] The Jet engine is disabled.");

        Assert.assertEquals(1, events.size());
        Assert.assertEquals("a warning", events.get(0).getString(LOG_MESSAGE));
    }

    @Test
    public void installedWhenNotConfigured() {
        Assert.assertTrue(HazelcastLoggerFactory.install(new Config()));
        Assert.assertEquals(HazelcastLoggerFactory.class.getName(), System.getProperty(PROPERTY_LOGGING_CLASS));
    }

    @Test
    public void userLoggingClassKept() {
        System.setProperty(PROPERTY_LOGGING_CLASS, "com.example.Factory");
        Assert.assertFalse(HazelcastLoggerFactory.install(new Config()));
        Assert.assertEquals("com.example.Factory", System.getProperty(PROPERTY_LOGGING_CLASS));
    }

    @Test
    public void userLoggingTypeKept() {
        System.setProperty(PROPERTY_LOGGING_TYPE, "jdk");
        Assert.assertFalse(HazelcastLoggerFactory.install(new Config()));
        Assert.assertNull(System.getProperty(PROPERTY_LOGGING_CLASS));
    }

    @Test
    public void configLoggingTypeKept() {
        Config config = new Config().setProperty(PROPERTY_LOGGING_TYPE, "slf4j");
        Assert.assertFalse(HazelcastLoggerFactory.install(config));
        Assert.assertNull(System.getProperty(PROPERTY_LOGGING_CLASS));
    }
}
