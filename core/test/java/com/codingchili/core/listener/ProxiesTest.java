package com.codingchili.core.listener;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Serializer;

/**
 * Tests the trusted proxies of a listener: the client address of X-Forwarded-For.
 */
public class ProxiesTest {
    private static final String PROXY = "10.0.0.2";
    private static final String CLIENT = "203.0.113.7";
    private static final String SPOOFED = "198.51.100.1";

    private static Proxies proxies(String... proxies) {
        return new Proxies(List.of(proxies));
    }

    @Test
    public void testTrustedPeerGivesTheForwardedAddress() {
        Assert.assertEquals(CLIENT, proxies(PROXY).client(PROXY, List.of(CLIENT)));
    }

    @Test
    public void testUntrustedPeerGivesThePeer() {
        // a client that sends the header itself, to dodge bans and rate limits.
        Assert.assertEquals(CLIENT, proxies(PROXY).client(CLIENT, List.of(SPOOFED)));
    }

    @Test
    public void testNoProxiesIgnoresTheHeader() {
        Assert.assertEquals(PROXY, proxies().client(PROXY, List.of(CLIENT)));
        Assert.assertEquals(PROXY, new Proxies(null).client(PROXY, List.of(CLIENT)));
    }

    @Test
    public void testTrustedPeerWithoutTheHeaderGivesThePeer() {
        Assert.assertEquals(PROXY, proxies(PROXY).client(PROXY, List.of()));
        Assert.assertEquals(PROXY, proxies(PROXY).client(PROXY, null));
    }

    @Test
    public void testSpoofedAddressesLeftOfTheProxyAreIgnored() {
        // the client sent 'SPOOFED', the proxy appended the address it saw.
        Assert.assertEquals(CLIENT, proxies(PROXY).client(PROXY, List.of(SPOOFED + ", " + CLIENT)));
    }

    @Test
    public void testChainOfProxies() {
        // client -> cdn (192.0.2.10) -> lb (10.0.0.3) -> peer (10.0.0.2)
        var proxies = proxies("10.0.0.0/24", "192.0.2.0/24");
        Assert.assertEquals(CLIENT, proxies.client(PROXY, List.of(SPOOFED + ", " + CLIENT + ", 192.0.2.10, 10.0.0.3")));
    }

    @Test
    public void testChainStopsAtTheFirstUntrustedHop() {
        // 192.0.2.10 is not trusted: what it says about the client is not either.
        Assert.assertEquals("192.0.2.10", proxies("10.0.0.0/24").client(PROXY, List.of(CLIENT + ", 192.0.2.10, 10.0.0.3")));
    }

    @Test
    public void testSeveralHeaderLines() {
        var proxies = proxies("10.0.0.0/24");
        Assert.assertEquals(CLIENT, proxies.client(PROXY, List.of(SPOOFED, CLIENT, "10.0.0.3")));
    }

    @Test
    public void testEveryHopTrustedGivesTheLeftmost() {
        Assert.assertEquals("10.0.0.5", proxies("10.0.0.0/24").client(PROXY, List.of("10.0.0.5, 10.0.0.3")));
    }

    @Test
    public void testNotAnAddressStopsAtTheLastTrustedHop() {
        var proxies = proxies("10.0.0.0/24");
        Assert.assertEquals("10.0.0.3", proxies.client(PROXY, List.of("unknown, 10.0.0.3")));
        Assert.assertEquals(PROXY, proxies.client(PROXY, List.of("example.com")));
        Assert.assertEquals(CLIENT, proxies.client(PROXY, List.of("example.com, " + CLIENT)));
    }

    @Test
    public void testPortsAndEmptyHops() {
        var proxies = proxies(PROXY);
        Assert.assertEquals(CLIENT, proxies.client(PROXY, List.of(CLIENT + ":4711")));
        Assert.assertEquals("2001:db8::1", proxies.client(PROXY, List.of("[2001:db8::1]:4711")));
        Assert.assertEquals(CLIENT, proxies.client(PROXY, List.of(CLIENT + ", ,")));
    }

    @Test
    public void testIpv6() {
        var proxies = proxies("fd00::/8", "::1");
        Assert.assertEquals("2001:db8::1", proxies.client("fd12::2", List.of("2001:0db8:0:0:0:0:0:1")));
        Assert.assertEquals("2001:db8::1", proxies.client("0:0:0:0:0:0:0:1", List.of("2001:db8::1")));
        Assert.assertEquals("fe80::2", proxies.client("fe80::2", List.of(CLIENT)));
    }

    @Test
    public void testIpv4MappedAddresses() {
        // a dual stack socket reports ipv4 peers as ::ffff:a.b.c.d.
        Assert.assertEquals(CLIENT, proxies("10.0.0.0/8").client("::ffff:10.0.0.2", List.of(CLIENT)));
        Assert.assertEquals(CLIENT, proxies("::ffff:10.0.0.0/104").client(PROXY, List.of("::ffff:" + CLIENT)));
    }

    @Test
    public void testRanges() {
        var proxies = proxies("172.16.0.0/12", "192.168.1.128/25", "0.0.0.0/32");
        Assert.assertTrue(proxies.trusted("172.31.255.255"));
        Assert.assertFalse(proxies.trusted("172.32.0.1"));
        Assert.assertTrue(proxies.trusted("192.168.1.200"));
        Assert.assertFalse(proxies.trusted("192.168.1.127"));
        Assert.assertFalse(proxies.trusted("0.0.0.1"));
        Assert.assertFalse(proxies.trusted("not an address"));
        Assert.assertTrue(proxies("0.0.0.0/0").trusted(CLIENT));
        Assert.assertFalse(proxies("0.0.0.0/0").trusted("::1"));
    }

    @Test
    public void testInvalidProxiesAreRejected() {
        for (String invalid : List.of("localhost", "10.0.0.0/33", "fd00::/129", "10.0.0.0/-1", "10.0.0.0/x", "10.0.0", "")) {
            Assert.assertThrows(invalid, CoreRuntimeException.class, () -> proxies(invalid));
        }
    }

    @Test
    public void testProxiesAreReadFromTheConfiguration() {
        String yaml = """
                port: 6969
                proxies: ["10.0.0.0/8", "::1"]
                """;
        var settings = Serializer.unpack(Serializer.yaml.readValue(yaml, JsonObject.class), ListenerSettings.class);

        Assert.assertEquals(List.of("10.0.0.0/8", "::1"), settings.getProxies());
        Assert.assertEquals(CLIENT, settings.proxies().client(PROXY, List.of(CLIENT)));
        Assert.assertTrue(new ListenerSettings().proxies().isEmpty());
    }
}
