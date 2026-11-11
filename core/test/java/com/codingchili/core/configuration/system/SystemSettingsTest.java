package com.codingchili.core.configuration.system;

import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static com.codingchili.core.configuration.CoreStrings.*;

@RunWith(VertxUnitRunner.class)
public class SystemSettingsTest {
    private final Map<String, String> environment = new HashMap<>();

    @After
    public void tearDown() {
        System.clearProperty(PROPERTY_EVENTBUS_HOST);
        System.clearProperty(PROPERTY_EVENTBUS_PUBLIC_HOST);
        System.clearProperty(PROPERTY_EVENTBUS_PORT);
    }

    @Test
    public void kryoUnsafeDisabledByDefault() {
        Assert.assertFalse(new SystemSettings().isUnsafe());
        Assert.assertEquals(
                "false",
                System.getProperty("kryo.unsafe")
        );
    }

    @Test
    public void eventBusHostUnsetByDefault() {
        // unset: Vert.x takes the hosts from the cluster manager (Hazelcast) and picks a random port.
        var eventBus = new SystemSettings().getOptions().getEventBusOptions();
        Assert.assertNull(eventBus.getHost());
        Assert.assertNull(eventBus.getClusterPublicHost());
        Assert.assertEquals(EventBusOptions.DEFAULT_CLUSTER_PORT, eventBus.getPort());
    }

    @Test
    public void eventBusConfiguredValuesApplied() {
        var eventBus = apply(new SystemSettings()
                .setEventBusHost("0.0.0.0")
                .setEventBusPublicHost("203.0.113.7")
                .setEventBusPort(5702));

        Assert.assertEquals("0.0.0.0", eventBus.getHost());
        Assert.assertEquals("203.0.113.7", eventBus.getClusterPublicHost());
        Assert.assertEquals(5702, eventBus.getPort());
    }

    @Test
    public void eventBusEnvironmentOverridesConfiguration() {
        environment.put(ENV_EVENTBUS_HOST, "10.0.0.2");
        environment.put(ENV_EVENTBUS_PUBLIC_HOST, "198.51.100.2");
        environment.put(ENV_EVENTBUS_PORT, "5703");

        var eventBus = apply(new SystemSettings()
                .setEventBusHost("0.0.0.0")
                .setEventBusPublicHost("203.0.113.7")
                .setEventBusPort(5702));

        Assert.assertEquals("10.0.0.2", eventBus.getHost());
        Assert.assertEquals("198.51.100.2", eventBus.getClusterPublicHost());
        Assert.assertEquals(5703, eventBus.getPort());
    }

    @Test
    public void eventBusSystemPropertyOverridesEnvironment() {
        environment.put(ENV_EVENTBUS_HOST, "10.0.0.2");
        environment.put(ENV_EVENTBUS_PUBLIC_HOST, "198.51.100.2");
        environment.put(ENV_EVENTBUS_PORT, "5703");
        System.setProperty(PROPERTY_EVENTBUS_HOST, "0.0.0.0");
        System.setProperty(PROPERTY_EVENTBUS_PUBLIC_HOST, "192.0.2.4");
        System.setProperty(PROPERTY_EVENTBUS_PORT, "5704");

        var eventBus = apply(new SystemSettings());

        Assert.assertEquals("0.0.0.0", eventBus.getHost());
        Assert.assertEquals("192.0.2.4", eventBus.getClusterPublicHost());
        Assert.assertEquals(5704, eventBus.getPort());
    }

    @Test
    public void eventBusBlankOverridesIgnored() {
        // an empty variable, as a start script passes for an unset one, does not override.
        environment.put(ENV_EVENTBUS_PUBLIC_HOST, " ");
        System.setProperty(PROPERTY_EVENTBUS_PUBLIC_HOST, "");

        var eventBus = apply(new SystemSettings().setEventBusPublicHost("203.0.113.7"));

        Assert.assertNull(eventBus.getHost());
        Assert.assertEquals("203.0.113.7", eventBus.getClusterPublicHost());
    }

    @Test(expected = IllegalArgumentException.class)
    public void eventBusInvalidPortFails() {
        environment.put(ENV_EVENTBUS_PORT, "eventbus");
        apply(new SystemSettings());
    }

    private EventBusOptions apply(SystemSettings settings) {
        var eventBus = new EventBusOptions();
        settings.applyEventBus(eventBus, environment::get);
        return eventBus;
    }
}
