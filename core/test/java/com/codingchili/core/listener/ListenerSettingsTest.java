package com.codingchili.core.listener;

import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.QuicServerConfig;
import io.vertx.core.net.ServerSSLOptions;
import io.vertx.core.net.TcpServerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Set;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.transport.*;
import com.codingchili.core.protocol.Serializer;

/**
 * Tests for the configuration of listeners.
 */
public class ListenerSettingsTest {

    @Test
    public void testHttp1AndHttp2WithoutTls() {
        Set<HttpVersion> versions = new ListenerSettings().setSecure(false).getHttpOptions().getVersions();

        Assert.assertTrue(versions.contains(HttpVersion.HTTP_1_1));
        Assert.assertTrue(versions.contains(HttpVersion.HTTP_2));
        Assert.assertFalse(versions.contains(HttpVersion.HTTP_3));
    }

    @Test
    public void testHttp3IsAddedWithTls() {
        Set<HttpVersion> versions = new ListenerSettings().setSecure(true).getHttpOptions().getVersions();

        // clients connect with http/1.1 or http/2 first, and are told about http/3.
        Assert.assertTrue(versions.contains(HttpVersion.HTTP_1_1));
        Assert.assertTrue(versions.contains(HttpVersion.HTTP_2));
        Assert.assertTrue(versions.contains(HttpVersion.HTTP_3));
    }

    @Test
    public void testHttp3RequiresTls() {
        ListenerSettings settings = new ListenerSettings().setSecure(false)
                .setHttpOptions(new HttpServerConfig().setVersions(HttpVersion.HTTP_3));

        try {
            settings.getHttpOptions();
            Assert.fail("expected http/3 without tls to be rejected.");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("HTTP/3 requires TLS"));
        }
    }

    @Test
    public void testConfiguredVersionsAreNotChanged() {
        ListenerSettings settings = new ListenerSettings().setSecure(true)
                .setConfig(new JsonObject().put("versions", java.util.List.of("HTTP_1_1")));

        Assert.assertEquals(Set.of(HttpVersion.HTTP_1_1), settings.getHttpOptions().getVersions());
    }

    @Test
    public void testConfiguredHttp3WithoutTlsIsRejected() {
        ListenerSettings settings = new ListenerSettings().setSecure(false)
                .setConfig(new JsonObject().put("versions", java.util.List.of("HTTP_1_1", "HTTP_3")));

        try {
            settings.getHttpOptions();
            Assert.fail("expected http/3 without tls to be rejected.");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("HTTP/3 requires TLS"));
        }
    }

    @Test
    public void testConfigurationIsReadFromTheConfigurationFile() {
        String yaml = """
                port: 4433
                secure: false
                config:
                  idleTimeout: PT45S
                  acceptBacklog: 64
                """;

        ListenerSettings settings = Serializer.unpack(Serializer.yaml.readValue(yaml, JsonObject.class), ListenerSettings.class);

        Assert.assertEquals(4433, settings.getPort());
        TcpServerConfig tcp = settings.getTcp();
        Assert.assertEquals(Duration.ofSeconds(45), tcp.getIdleTimeout());
        Assert.assertEquals(64, tcp.getAcceptBacklog());
    }

    @Test
    public void testTheConfigurationIsReadAsTheClassOfTheListener() {
        ListenerSettings settings = new ListenerSettings().setSecure(false)
                .setConfig(new JsonObject().put("idleTimeout", "PT10S"));

        // each listener reads the same key as the configuration class that it declares: the key must match the class.
        Assert.assertEquals(Duration.ofSeconds(10), settings.getTcp().getIdleTimeout());
        Assert.assertEquals(Duration.ofSeconds(10), settings.getQuic().getIdleTimeout());

        settings = new ListenerSettings().setSecure(false)
                .setConfig(new JsonObject().put("handle100ContinueAutomatically", true));
        Assert.assertTrue(settings.getHttpOptions().isHandle100ContinueAutomatically());
    }

    @Test
    public void testQuicAndUdpConfiguration() {
        ListenerSettings settings = new ListenerSettings()
                .setConfig(new JsonObject().put("loadBalanced", true));
        Assert.assertTrue(settings.getQuic().isLoadBalanced());

        settings = new ListenerSettings().setConfig(new JsonObject().put("broadcast", true).put("receiveBufferSize", 4096));
        Assert.assertTrue(settings.getUdp().isBroadcast());
        Assert.assertEquals(4096, settings.getUdp().getReceiveBufferSize());
    }

    @Test
    public void testNestedConfigurationIsRead() {
        ListenerSettings settings = new ListenerSettings().setSecure(false).setConfig(new JsonObject()
                .put("compressionConfig", new JsonObject().put("compressionEnabled", false))
                .put("http1Config", new JsonObject().put("maxHeaderSize", 4096)));

        HttpServerConfig http = settings.getHttpOptions();
        Assert.assertFalse(http.getCompressionConfig().isCompressionEnabled());
        Assert.assertEquals(4096, http.getHttp1Config().getMaxHeaderSize());
    }

    @Test
    public void testMisspelledPropertyIsRejectedWithAClearMessage() {
        ListenerSettings settings = new ListenerSettings()
                .setConfig(new JsonObject().put("idleTimout", "PT10S"));

        try {
            settings.getTcp();
            Assert.fail("expected a misspelled property to be rejected.");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("TcpServerConfig"));
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("idleTimout"));
        }
    }

    @Test
    public void testInvalidValueIsRejected() {
        ListenerSettings settings = new ListenerSettings()
                .setConfig(new JsonObject().put("idleTimeout", "soon"));

        try {
            settings.getTcp();
            Assert.fail("expected an invalid value to be rejected.");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("TcpServerConfig"));
        }
    }

    @Test
    public void testObjectsThatAreSetOverrideTheConfiguration() {
        TcpServerConfig tcp = new TcpServerConfig().setIdleTimeout(Duration.ofSeconds(99));
        ListenerSettings settings = new ListenerSettings()
                .setConfig(new JsonObject().put("idleTimeout", "PT10S"))
                .setTcp(tcp);

        Assert.assertSame(tcp, settings.getTcp());

        HttpServerConfig http = new HttpServerConfig().setIdleTimeout(Duration.ofSeconds(99));
        settings.setHttpOptions(http);
        Assert.assertSame(http, settings.getHttpOptions());
    }

    @Test
    public void testNoConfigurationIsTheDefaultConfiguration() {
        ListenerSettings settings = new ListenerSettings();

        Assert.assertNull(settings.getConfig());
        Assert.assertEquals(new TcpServerConfig().getIdleTimeout(), settings.getTcp().getIdleTimeout());
        Assert.assertEquals(new DatagramSocketOptions().getReceiveBufferSize(), settings.getUdp().getReceiveBufferSize());
        Assert.assertEquals(new QuicServerConfig().getPort(), settings.getQuic().getPort());
    }

    @Test
    public void testConfigurationSurvivesBeingSavedAndLoaded() {
        ListenerSettings settings = new ListenerSettings().setPort(1234)
                .setConfig(new JsonObject().put("idleTimeout", "PT10S"));

        ListenerSettings loaded = Serializer.unpack(Serializer.json(settings), ListenerSettings.class);

        Assert.assertEquals(1234, loaded.getPort());
        Assert.assertEquals(settings.getConfig(), loaded.getConfig());
    }

    @Test
    public void testTlsOptionsThatAreSetAreUsed() {
        ServerSSLOptions options = new ServerSSLOptions();
        ListenerSettings settings = new ListenerSettings().setSecure(true).setSecurity(options);

        Assert.assertSame(options, settings.getSecurity());
        // an insecure listener must not use tls, whatever the options.
        Assert.assertNull(settings.setSecure(false).getSecurity());
    }

    @Test
    public void testAlpnIsEnabledByDefault() {
        // the certificate is generated when the default keystore is missing.
        ListenerSettings settings = new ListenerSettings().setSecure(true);
        Assert.assertTrue(settings.getSecurity().isUseAlpn());

        settings.setAlpn(false);
        Assert.assertFalse(settings.getSecurity().isUseAlpn());
    }

    @Test
    public void testListenersDeclareTheConfigurationTheyRead() {
        Assert.assertEquals(HttpServerConfig.class, new RestListener().configType());
        Assert.assertEquals(HttpServerConfig.class, new WebsocketListener().configType());
        Assert.assertEquals(TcpServerConfig.class, new TcpListener().configType());
        Assert.assertEquals(QuicServerConfig.class, new QuicListener().configType());
        Assert.assertEquals(DatagramSocketOptions.class, new UdpListener().configType());
        // the cluster listener uses the event bus.
        Assert.assertNull(new ClusterListener().configType());
    }
}
