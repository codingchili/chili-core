package com.codingchili.core.context;

import com.hazelcast.config.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.HazelcastLoggerFactory;

import static com.codingchili.core.configuration.CoreStrings.PROPERTY_CLUSTER_NAME;

/**
 * Tests that the cluster settings of the launcher reach the Hazelcast config of a clustered context, and that
 * Hazelcast logs through chili-core unless its logging is configured.
 */
public class ClusterManagerTest {

    @After
    public void tearDown() {
        System.clearProperty(PROPERTY_CLUSTER_NAME);
        System.clearProperty(HazelcastLoggerFactory.PROPERTY_LOGGING_CLASS);
        System.clearProperty(HazelcastLoggerFactory.PROPERTY_LOGGING_TYPE);
        Configurations.reset();
    }

    @Test
    public void launcherSettingsReachHazelcast() {
        Configurations.launcher().setApplication("bunny-app");
        Configurations.launcher().getCluster().setCloud(false).setHost("203.0.113.7");

        Config config = SystemContext.clusterManager().getConfig();

        // no name configured: the application name, cluster.xml's name is replaced.
        Assert.assertEquals("bunny-app", config.getClusterName());
        Assert.assertTrue(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        Assert.assertEquals("203.0.113.7", config.getNetworkConfig().getPublicAddress());
        Assert.assertEquals(HazelcastLoggerFactory.class.getName(),
                System.getProperty(HazelcastLoggerFactory.PROPERTY_LOGGING_CLASS));
    }

    @Test
    public void nameOverridePrecedence() {
        Configurations.launcher().getCluster().setName("configured");
        Assert.assertEquals("configured", SystemContext.clusterManager().getConfig().getClusterName());

        System.setProperty(PROPERTY_CLUSTER_NAME, "override");
        Assert.assertEquals("override", SystemContext.clusterManager().getConfig().getClusterName());
    }

    @Test
    public void userLoggingKept() {
        System.setProperty(HazelcastLoggerFactory.PROPERTY_LOGGING_TYPE, "jdk");
        SystemContext.clusterManager();
        Assert.assertNull(System.getProperty(HazelcastLoggerFactory.PROPERTY_LOGGING_CLASS));
    }
}
