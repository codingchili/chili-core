package com.codingchili.core.configuration;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.system.RemoteStorage;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.protocol.Serializer;

/**
 * The short durations of the configuration files: 7d, 10m, 1h30m, 250ms, plain numbers as milliseconds, and the
 * time settings of the framework that use them.
 */
public class DurationsTest {
    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void units() {
        Assert.assertEquals(Duration.ofMillis(250), Durations.parse("250ms"));
        Assert.assertEquals(Duration.ofSeconds(45), Durations.parse("45s"));
        Assert.assertEquals(Duration.ofMinutes(10), Durations.parse("10m"));
        Assert.assertEquals(Duration.ofHours(3), Durations.parse("3h"));
        Assert.assertEquals(Duration.ofDays(7), Durations.parse("7d"));
    }

    @Test
    public void combined() {
        Assert.assertEquals(Duration.ofMinutes(90), Durations.parse("1h30m"));
        Assert.assertEquals(Duration.ofDays(1).plusHours(2).plusSeconds(3).plusMillis(4), Durations.parse("1d2h3s4ms"));
        Assert.assertEquals(Duration.ofMinutes(5), Durations.parse(" 5M "));
    }

    @Test
    public void plainNumberIsMilliseconds() {
        Assert.assertEquals(Duration.ofMillis(1500), Durations.parse("1500"));
        Assert.assertEquals(Duration.ZERO, Durations.parse("0"));
    }

    @Test
    public void anythingElseNamesTheValue() {
        for (var bad : new String[]{"", "d", "7x", "7 d", "1h 30m", "h1", "-5m", "1.5h", "PT10M", "P7D", "7dd", "m10"}) {
            var error = Assert.assertThrows(bad, IllegalArgumentException.class, () -> Durations.parse(bad));
            Assert.assertTrue(error.getMessage(), error.getMessage().contains("'" + bad + "'"));
        }
        Assert.assertThrows(IllegalArgumentException.class, () -> Durations.parse(null));
    }

    @Test
    public void formatsShort() {
        Assert.assertEquals("7d", Durations.format(Duration.ofDays(7)));
        Assert.assertEquals("1h30m", Durations.format(Duration.ofMinutes(90)));
        Assert.assertEquals("1s500ms", Durations.format(Duration.ofMillis(1500)));
        Assert.assertEquals("0s", Durations.format(Duration.ZERO));
        for (var text : new String[]{"7d", "10m", "1d2h3m4s5ms"}) {
            Assert.assertEquals(text, Durations.format(Durations.parse(text)));
        }
    }

    @Test
    public void millisCapped() {
        Assert.assertEquals(1500, Durations.millis(Duration.ofMillis(1500)));
        Assert.assertEquals(Integer.MAX_VALUE, Durations.millis(Duration.ofDays(365)));
    }

    @Test
    public void annotatedFieldReadsAndWrites() {
        var yaml = "expiry: \"2d\"\n";
        var settings = Serializer.unpack(new JsonObject(Serializer.unyaml(yaml, Map.class)), Units.class);
        Assert.assertEquals(Duration.ofDays(2), settings.getExpiry());

        var written = Serializer.yaml(settings);
        Assert.assertTrue(written, written.contains("expiry: \"2d\"") || written.contains("expiry: 2d"));

        // a number is milliseconds.
        var millis = Serializer.unpack(new JsonObject().put("expiry", 60_000), Units.class);
        Assert.assertEquals(Duration.ofMinutes(1), millis.getExpiry());
        Assert.assertEquals("1m", Serializer.json(millis).getString("expiry"));
    }

    @Test
    public void badConfigValueNamesIt() {
        var error = Assert.assertThrows(RuntimeException.class,
                () -> Serializer.unpack(new JsonObject().put("expiry", "seven days"), Units.class));
        var message = String.valueOf(error.getMessage()) + (error.getCause() == null ? "" : error.getCause().getMessage());
        Assert.assertTrue(message, message.contains("seven days"));
    }

    @Test
    public void fieldThatHeldAnotherUnitKeepsItsPlainNumbers() {
        var plain = Serializer.unpack(new JsonObject().put("seconds", 30).put("days", "14"), Units.class);
        Assert.assertEquals(Duration.ofSeconds(30), plain.seconds);
        Assert.assertEquals(Duration.ofDays(14), plain.days);
        var units = Serializer.unpack(new JsonObject().put("seconds", "1m30s").put("days", "12h"), Units.class);
        Assert.assertEquals(Duration.ofSeconds(90), units.seconds);
        Assert.assertEquals(Duration.ofHours(12), units.days);
        Assert.assertEquals("1m30s", Serializer.json(units).getString("seconds"));
        Assert.assertThrows(RuntimeException.class,
                () -> Serializer.unpack(new JsonObject().put("seconds", "soon"), Units.class));
    }

    @Test
    public void systemSettingsReadOldNumbersAsMillis() {
        var system = Serializer.unpack(new JsonObject()
                .put("deployTimeout", 3000)
                .put("shutdownHookTimeout", 30000)
                .put("shutdownDelay", 5000)
                .put("configurationPoll", 1500)
                .put("cachedFilePoll", 2000)
                .put("clusterTimeout", 4000)
                .put("blockedThreadChecker", 2000)
                .put("maxEventLoopExecuteTime", 500)
                .put("metrics", new JsonObject().put("rate", 15000)), SystemSettings.class);
        Assert.assertEquals(Duration.ofSeconds(3), system.getDeployTimeout());
        Assert.assertEquals(Duration.ofSeconds(30), system.getShutdownHookTimeout());
        Assert.assertEquals(Duration.ofSeconds(5), system.getShutdownDelay());
        Assert.assertEquals(Duration.ofMillis(1500), system.getConfigurationPoll());
        Assert.assertEquals(Duration.ofSeconds(2), system.getCachedFilePoll());
        Assert.assertEquals(Duration.ofSeconds(4), system.getClusterTimeout());
        Assert.assertEquals(Duration.ofSeconds(2), system.getBlockedThreadChecker());
        Assert.assertEquals(Duration.ofMillis(500), system.getMaxEventLoopExecuteTime());
        Assert.assertEquals(Duration.ofSeconds(15), system.getMetrics().getRate());

        var written = Serializer.json(system);
        Assert.assertEquals("3s", written.getString("deployTimeout"));
        Assert.assertEquals("1s500ms", written.getString("configurationPoll"));
        Assert.assertEquals("15s", written.getJsonObject("metrics").getString("rate"));
    }

    @Test
    public void systemSettingsVertxOptions() {
        var system = Serializer.unpack(new JsonObject()
                .put("blockedThreadChecker", "2s")
                .put("maxEventLoopExecuteTime", "250ms"), SystemSettings.class);
        var options = system.getOptions();
        Assert.assertEquals(2000, TimeUnit.MILLISECONDS.convert(options.getBlockedThreadCheckInterval(),
                options.getBlockedThreadCheckIntervalUnit()));
        Assert.assertEquals(250, TimeUnit.MILLISECONDS.convert(options.getMaxEventLoopExecuteTime(),
                options.getMaxEventLoopExecuteTimeUnit()));
    }

    @Test
    public void systemSettingsDefaults() {
        var system = new SystemSettings();
        Assert.assertEquals(Duration.ofSeconds(3), system.getDeployTimeout());
        Assert.assertEquals(Duration.ofSeconds(1), system.getBlockedThreadChecker());
        Assert.assertEquals(Duration.ofSeconds(2), system.getMaxEventLoopExecuteTime());
        Assert.assertEquals(Duration.ZERO, system.getShutdownDelay());
        Assert.assertEquals("0s", Serializer.json(system).getString("shutdownDelay"));
    }

    @Test
    public void securityTokenLifetimesReadOldNumbersAsSeconds() {
        var security = Serializer.unpack(new JsonObject()
                .put("tokenttl", 604800)
                .put("serviceTokenttl", 0), SecuritySettings.class);
        Assert.assertEquals(Duration.ofDays(7), security.getTokenttl());
        Assert.assertEquals(Duration.ZERO, security.getServiceTokenttl());
        var written = Serializer.json(security);
        Assert.assertEquals("7d", written.getString("tokenttl"));
        Assert.assertEquals("0s", written.getString("serviceTokenttl"));

        var units = Serializer.unpack(new JsonObject().put("tokenttl", "1d"), SecuritySettings.class);
        Assert.assertEquals(Duration.ofDays(1), units.getTokenttl());
        Assert.assertEquals("the service tokens default to the tokenttl",
                Duration.ofDays(1), units.getServiceTokenttl());
    }

    @Test
    public void listenerAndStorageReadOldNumbersAsMillis() {
        var listener = Serializer.unpack(new JsonObject().put("timeout", 7000), ListenerSettings.class);
        Assert.assertEquals(Duration.ofSeconds(7), listener.getTimeout());
        Assert.assertEquals(Duration.ofSeconds(10),
                Serializer.unpack(new JsonObject().put("timeout", "10s"), ListenerSettings.class).getTimeout());
        Assert.assertEquals("7s", Serializer.json(listener).getString("timeout"));
        Assert.assertEquals(Duration.ofMillis(ListenerSettings.DEFAULT_TIMEOUT), new ListenerSettings().getTimeout());

        var storage = Serializer.unpack(new JsonObject().put("persistInterval", 500), RemoteStorage.class);
        Assert.assertEquals(Duration.ofMillis(500), storage.getPersistInterval());
        Assert.assertEquals(Duration.ofMinutes(1),
                Serializer.unpack(new JsonObject().put("persistInterval", "1m"), RemoteStorage.class).getPersistInterval());
    }

    public static class Units {
        @JsonDeserialize(using = Durations.Read.class)
        @JsonSerialize(using = Durations.Write.class)
        private Duration expiry = Duration.ofDays(7);
        @JsonDeserialize(using = Durations.ReadSeconds.class)
        @JsonSerialize(using = Durations.Write.class)
        public Duration seconds;
        @JsonDeserialize(using = Durations.ReadDays.class)
        @JsonSerialize(using = Durations.Write.class)
        public Duration days;

        public Duration getExpiry() {
            return expiry;
        }

        public Units setExpiry(Duration expiry) {
            this.expiry = expiry;
            return this;
        }
    }
}
