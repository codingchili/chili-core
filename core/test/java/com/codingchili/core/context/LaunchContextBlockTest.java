package com.codingchili.core.context;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingchili.core.configuration.Environment;
import com.codingchili.core.configuration.exception.BlockNotConfiguredException;
import com.codingchili.core.configuration.exception.HostNotConfiguredException;
import com.codingchili.core.configuration.system.LauncherSettings;

/**
 * Tests how the launcher picks the block of a host: a hosts entry, a block named as the hostname, the default
 * block only without hosts, and no fallback to the default block for a block or host that is not configured.
 */
public class LaunchContextBlockTest {
    private static final String DEFAULT = "default";
    private final Map<String, List<String>> blocks = new HashMap<>();
    private final Map<String, String> hosts = new HashMap<>();
    private String hostname;

    @Before
    public void setUp() {
        Assume.assumeTrue("the hostname of this machine is needed", Environment.hostname().isPresent());
        hostname = Environment.hostname().get();
        blocks.put(DEFAULT, List.of("default-service"));
    }

    private LaunchContext context(String... args) {
        LaunchContext context = create(args);
        if (args.length > 0) {
            // parses the deploy command and its blocks.
            context.execute();
        }
        return context;
    }

    private LaunchContext create(String... args) {
        LauncherSettings settings = new LauncherSettings()
                .setBlocks(new HashMap<>(blocks))
                .setHosts(new HashMap<>(hosts));

        return new LaunchContext(args) {
            @Override
            public LauncherSettings settings() {
                return settings;
            }
        };
    }

    @Test
    public void blockNamedAsTheHostname() throws CoreException {
        blocks.put(hostname, List.of("host-service"));
        Assert.assertEquals(List.of("host-service"), context().services());
    }

    @Test
    public void hostsMappingWinsOverABlockOfTheSameName() throws CoreException {
        blocks.put(hostname, List.of("host-service"));
        blocks.put("edge", List.of("edge-service"));
        hosts.put(hostname, "edge");
        Assert.assertEquals(List.of("edge-service"), context().services());
    }

    @Test
    public void unknownBlockFails() {
        try {
            context("deploy", "hubb").services();
            Assert.fail("a block that is not configured must fail");
        } catch (CoreException e) {
            Assert.assertTrue(e instanceof BlockNotConfiguredException);
            Assert.assertTrue(e.getMessage().contains("hubb"));
        }
    }

    @Test
    public void unmatchedHostWithHostsConfiguredFails() {
        blocks.put("edge", List.of("edge-service"));
        hosts.put("another-machine.invalid", "edge");
        try {
            context().services();
            Assert.fail("a host that is not in the configured hosts must fail");
        } catch (CoreException e) {
            Assert.assertTrue(e instanceof HostNotConfiguredException);
            Assert.assertTrue(e.getMessage().contains(hostname));
            Assert.assertTrue(e.getMessage().contains("another-machine.invalid"));
        }
    }

    @Test
    public void localhostEntryStillMatches() throws CoreException {
        hosts.put("localhost", DEFAULT);
        Assert.assertEquals(List.of("default-service"), context().services());
    }

    @Test
    public void noHostsDeploysDefault() throws CoreException {
        blocks.put("edge", List.of("edge-service"));
        Assert.assertEquals(List.of("default-service"), context().services());
    }

    @Test
    public void explicitBlockDeployed() throws CoreException {
        blocks.put("edge", List.of("edge-service"));
        Assert.assertEquals(List.of("edge-service"), context("deploy", "edge").services());
    }
}
