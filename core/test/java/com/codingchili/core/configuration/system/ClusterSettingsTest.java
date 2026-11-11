package com.codingchili.core.configuration.system;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import io.vertx.core.eventbus.EventBusOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Tests the cluster settings of the launcher: defaults, the deprecated 'clustered' alias, the precedence of the
 * overrides and the settings reaching the Hazelcast config and the event bus options.
 */
public class ClusterSettingsTest {
    private static final String[] PROPERTIES = {PROPERTY_CLUSTER_ENABLED, PROPERTY_CLUSTER_NAME,
            PROPERTY_CLUSTER_MULTICAST, PROPERTY_CLUSTER_CLOUD, PROPERTY_CLUSTER_MEMBERS, PROPERTY_CLUSTER_HOST,
            PROPERTY_CLUSTER_BIND, PROPERTY_CLUSTER_PORT};
    private final Map<String, String> environment = new HashMap<>();

    @After
    public void tearDown() {
        for (String property : PROPERTIES) {
            System.clearProperty(property);
        }
    }

    private ClusterSettings settings() {
        return new ClusterSettings().setEnvironment(environment::get);
    }

    @Test
    public void defaults() {
        ClusterSettings settings = settings();
        Assert.assertFalse(settings.isActive());
        Assert.assertTrue(settings.isMulticast());
        Assert.assertTrue(settings.isCloud());
        Assert.assertTrue(settings.resolveMembers().isEmpty());
        Assert.assertEquals("application", settings.resolveName("application"));

        // cloud (auto-detection) on: Hazelcast falls back to multicast when it detects no cloud.
        JoinConfig join = settings.apply(new Config(), "application").getNetworkConfig().getJoin();
        Assert.assertTrue(join.getAutoDetectionConfig().isEnabled());
        Assert.assertTrue(join.isAutoDetectionEnabled());
        Assert.assertFalse(join.getMulticastConfig().isEnabled());
        Assert.assertFalse(join.getTcpIpConfig().isEnabled());

        EventBusOptions eventBus = settings.apply(new EventBusOptions());
        Assert.assertNull(eventBus.getHost());
        Assert.assertNull(eventBus.getClusterPublicHost());
        Assert.assertEquals(EventBusOptions.DEFAULT_CLUSTER_PORT, eventBus.getPort());
    }

    @Test
    public void defaultsInLauncher() {
        LauncherSettings launcher = new LauncherSettings();
        Assert.assertFalse(launcher.isClustered());
        Assert.assertFalse(launcher.getCluster().isEnabled());
    }

    @Test
    public void clusteredAliasEnablesCluster() {
        LauncherSettings launcher = Serializer.unyaml("clustered: true\n", LauncherSettings.class);
        Assert.assertTrue(launcher.getCluster().isEnabled());
        Assert.assertTrue(launcher.isClustered());
        // the alias is read but never written.
        Assert.assertFalse(Serializer.yaml(launcher).contains("clustered"));
    }

    @Test
    public void clusterObjectRead() {
        LauncherSettings launcher = Serializer.unyaml(String.join("\n",
                "cluster:",
                "  enabled: true",
                "  name: bunnies",
                "  multicast: false",
                "  cloud: false",
                "  members: [\"10.0.0.1,10.0.0.2\", \"10.0.0.3:5702\"]",
                "  host: 203.0.113.7",
                "  bind: 10.0.0.9",
                "  port: 5801",
                ""), LauncherSettings.class);
        ClusterSettings cluster = launcher.getCluster().setEnvironment(environment::get);

        Assert.assertTrue(cluster.isActive());
        Assert.assertEquals("bunnies", cluster.resolveName("application"));
        Assert.assertEquals(List.of("10.0.0.1", "10.0.0.2", "10.0.0.3:5702"), cluster.resolveMembers());

        EventBusOptions eventBus = cluster.apply(new EventBusOptions());
        Assert.assertEquals("10.0.0.9", eventBus.getHost());
        Assert.assertEquals("203.0.113.7", eventBus.getClusterPublicHost());
        Assert.assertEquals(5801, eventBus.getPort());
    }

    @Test
    public void membersJoinByTcpIp() {
        Config config = settings().setMembers(List.of("hub, realms", "arena")).apply(new Config(), "application");
        JoinConfig join = config.getNetworkConfig().getJoin();

        Assert.assertTrue(join.getTcpIpConfig().isEnabled());
        Assert.assertEquals(List.of("hub", "realms", "arena"), join.getTcpIpConfig().getMembers());
        Assert.assertFalse(join.getMulticastConfig().isEnabled());
        Assert.assertFalse(join.isAutoDetectionEnabled());
        join.verify();
    }

    @Test
    public void multicastWithoutCloud() {
        JoinConfig join = settings().setCloud(false).apply(new Config(), "application")
                .getNetworkConfig().getJoin();

        Assert.assertTrue(join.getMulticastConfig().isEnabled());
        Assert.assertFalse(join.getAutoDetectionConfig().isEnabled());
        Assert.assertFalse(join.getTcpIpConfig().isEnabled());
        join.verify();
    }

    @Test
    public void discoveryOffKeepsTheConfigJoin() {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("192.0.2.1");

        JoinConfig join = settings().setCloud(false).setMulticast(false).apply(config, "application")
                .getNetworkConfig().getJoin();

        Assert.assertTrue(join.getTcpIpConfig().isEnabled());
        Assert.assertEquals(List.of("192.0.2.1"), join.getTcpIpConfig().getMembers());
    }

    @Test
    public void nameAndHostReachTheHazelcastConfig() {
        Config config = settings().setName("bunnies").setHost("203.0.113.7").apply(new Config(), "application");

        Assert.assertEquals("bunnies", config.getClusterName());
        Assert.assertEquals("203.0.113.7", config.getNetworkConfig().getPublicAddress());
    }

    @Test
    public void hostBindsTheEventBusToEveryInterface() {
        EventBusOptions eventBus = settings().setHost("203.0.113.7").apply(new EventBusOptions());

        Assert.assertEquals(ClusterSettings.ANY_ADDRESS, eventBus.getHost());
        Assert.assertEquals("203.0.113.7", eventBus.getClusterPublicHost());
    }

    @Test
    public void environmentOverridesConfiguration() {
        environment.put(ENV_CLUSTER_NAME, "from-env");
        environment.put(ENV_CLUSTER_MEMBERS, "10.0.0.1,10.0.0.2");
        environment.put(ENV_CLUSTER_HOST, "198.51.100.2");
        environment.put(ENV_CLUSTER_PORT, "5803");

        ClusterSettings settings = settings().setName("bunnies").setHost("203.0.113.7").setPort(5801);
        Config config = settings.apply(new Config(), "application");
        EventBusOptions eventBus = settings.apply(new EventBusOptions());

        Assert.assertEquals("from-env", config.getClusterName());
        Assert.assertEquals(List.of("10.0.0.1", "10.0.0.2"),
                config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        Assert.assertEquals("198.51.100.2", config.getNetworkConfig().getPublicAddress());
        Assert.assertEquals("198.51.100.2", eventBus.getClusterPublicHost());
        Assert.assertEquals(5803, eventBus.getPort());
    }

    @Test
    public void systemPropertyOverridesEnvironment() {
        environment.put(ENV_CLUSTER_NAME, "from-env");
        environment.put(ENV_CLUSTER_PORT, "5803");
        System.setProperty(PROPERTY_CLUSTER_NAME, "from-property");
        System.setProperty(PROPERTY_CLUSTER_PORT, "5804");

        ClusterSettings settings = settings().setName("bunnies");

        Assert.assertEquals("from-property", settings.resolveName("application"));
        Assert.assertEquals(5804, settings.apply(new EventBusOptions()).getPort());
    }

    @Test
    public void blankOverridesIgnored() {
        // an empty variable, as a start script passes for an unset one, does not override.
        environment.put(ENV_CLUSTER_NAME, " ");
        System.setProperty(PROPERTY_CLUSTER_HOST, "");
        System.setProperty(PROPERTY_CLUSTER_ENABLED, "");

        ClusterSettings settings = settings().setName("bunnies").setHost("203.0.113.7");

        Assert.assertFalse(settings.isActive());
        Assert.assertEquals("bunnies", settings.resolveName("application"));
        Assert.assertEquals("203.0.113.7", settings.apply(new EventBusOptions()).getClusterPublicHost());
    }

    @Test
    public void enabledOverride() {
        System.setProperty(PROPERTY_CLUSTER_ENABLED, "on");
        Assert.assertTrue(settings().isActive());

        System.setProperty(PROPERTY_CLUSTER_ENABLED, "off");
        Assert.assertFalse(settings().setEnabled(true).isActive());
    }

    @Test
    public void membersOverrideEnables() {
        environment.put(ENV_CLUSTER_MEMBERS, "hub,realms");
        Assert.assertTrue(settings().isActive());

        // unless clustering is switched off explicitly.
        environment.put(ENV_CLUSTER_ENABLED, "false");
        Assert.assertFalse(settings().isActive());
    }

    @Test
    public void discoveryOverrides() {
        System.setProperty(PROPERTY_CLUSTER_CLOUD, "off");

        JoinConfig join = settings().apply(new Config(), "application").getNetworkConfig().getJoin();
        Assert.assertTrue(join.getMulticastConfig().isEnabled());
        Assert.assertFalse(join.getAutoDetectionConfig().isEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSwitchFails() {
        System.setProperty(PROPERTY_CLUSTER_ENABLED, "maybe");
        settings().isActive();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPortFails() {
        environment.put(ENV_CLUSTER_PORT, "eventbus");
        settings().apply(new EventBusOptions());
    }
}
