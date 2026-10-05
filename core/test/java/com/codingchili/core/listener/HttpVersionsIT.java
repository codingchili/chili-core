package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.listener.transport.RestListener;
import com.codingchili.core.listener.transport.TcpListener;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.CoreStrings.getLoopbackAddress;

/**
 * Verifies the HTTP versions that the listeners serve, with clients that speak each version: HTTP/1.1 and HTTP/2
 * without TLS, and HTTP/1.1, HTTP/2 (negotiated with ALPN) and HTTP/3 (QUIC) with TLS.
 */
@RunWith(VertxUnitRunner.class)
public class HttpVersionsIT {
    private static final String HOST = getLoopbackAddress();
    private static final String PATH = "/node/ping";
    private final java.util.List<HttpClientAgent> clients = new java.util.ArrayList<>();
    private ContextMock context;

    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        context = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testHttp1WithoutTls(TestContext test) {
        ListenerSettings settings = plain();

        listen(settings).compose(port -> get(client(false, HttpVersion.HTTP_1_1), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_1_1, response.version);
                }));
    }

    @Test
    public void testHttp2WithoutTls(TestContext test) {
        ListenerSettings settings = plain();

        // prior knowledge: the client starts with HTTP/2 without upgrading from HTTP/1.1.
        listen(settings).compose(port -> get(client(false, HttpVersion.HTTP_2), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_2, response.version);
                }));
    }

    @Test
    public void testHttp1WithTls(TestContext test) {
        ListenerSettings settings = secure();

        listen(settings).compose(port -> get(client(true, HttpVersion.HTTP_1_1), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_1_1, response.version);
                }));
    }

    @Test
    public void testHttp2WithTlsIsNegotiatedWithAlpn(TestContext test) {
        ListenerSettings settings = secure();

        listen(settings).compose(port -> get(client(true, HttpVersion.HTTP_2, HttpVersion.HTTP_1_1), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_2, response.version);
                }));
    }

    @Test
    public void testHttp3WithTls(TestContext test) throws java.io.IOException {
        // with port 0 the TCP and UDP sockets of the server get different ports: use the port that a client would.
        ListenerSettings settings = secure().setPort(freePort());

        // the server listens for http/3 on the same port number as for http/1.1 and http/2 (UDP and TCP).
        listen(settings).compose(port -> get(client(true, HttpVersion.HTTP_3), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_3, response.version);
                }));
    }

    @Test
    public void testHttp3WithoutTlsFailsWithAClearMessage(TestContext test) {
        ListenerSettings settings = plain()
                .setConfig(new JsonObject().put("versions", java.util.List.of("HTTP_1_1", "HTTP_3")));

        listen(settings).onComplete(test.asyncAssertFailure(e ->
                test.assertTrue(e.getMessage().contains("HTTP/3 requires TLS"))));
    }

    @Test
    public void testConfiguredVersionsAreServed(TestContext test) {
        // the key in the configuration changes what is served: only http/1.1.
        ListenerSettings settings = plain()
                .setConfig(new JsonObject().put("versions", java.util.List.of("HTTP_1_1")));

        listen(settings).compose(port -> get(client(false, HttpVersion.HTTP_1_1), port))
                .onComplete(test.asyncAssertSuccess(response -> {
                    test.assertEquals(200, response.status);
                    test.assertEquals(HttpVersion.HTTP_1_1, response.version);
                }));
    }

    @Test
    public void testHttp2IsNotServedWhenOnlyHttp1IsConfigured(TestContext test) {
        ListenerSettings settings = plain()
                .setConfig(new JsonObject().put("versions", java.util.List.of("HTTP_1_1")));

        listen(settings).compose(port -> get(client(false, HttpVersion.HTTP_2), port))
                .onComplete(test.asyncAssertFailure());
    }

    @Test
    public void testInvalidTransportConfigurationFailsTheDeployment(TestContext test) {
        ListenerSettings settings = plain().setConfig(new JsonObject().put("idleTimout", "PT10S"));

        context.listener(() -> new TcpListener().settings(settings).handler(new AcceptingHandler()))
                .onComplete(test.asyncAssertFailure(e -> {
                    test.assertTrue(e.getMessage().contains("TcpServerConfig"));
                    test.assertTrue(e.getMessage().contains("idleTimout"));
                }));
    }

    private static int freePort() throws java.io.IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private ListenerSettings plain() {
        return new ListenerSettings().setPort(0).setSecure(false);
    }

    /**
     * Secure with the default keystore, which does not exist: a certificate is generated.
     */
    private ListenerSettings secure() {
        return new ListenerSettings().setPort(0).setSecure(true);
    }

    private Future<Integer> listen(ListenerSettings settings) {
        return context.listener(() -> new RestListener().settings(settings).handler(new AcceptingHandler()))
                .map(id -> settings.getListenPorts().iterator().next());
    }

    private HttpClientAgent client(boolean tls, HttpVersion... versions) {
        HttpClientConfig config = new HttpClientConfig().setVersions(versions).setSsl(tls);
        // do not upgrade from http/1.1 to http/2: the version that the test asks for is the one that is used.
        config.setHttp2Config(new Http2ClientConfig().setClearTextUpgrade(false));

        HttpClientAgent client = (tls) ?
                // the version is negotiated with ALPN, otherwise the client speaks http/2 without asking the server.
                context.vertx().createHttpClient(config, new ClientSSLOptions().setTrustAll(true)
                        .setHostnameVerificationAlgorithm("").setUseAlpn(true)) :
                context.vertx().createHttpClient(config);

        // a client that is garbage collected closes its connections: the reference is kept until the test is done.
        clients.add(client);
        return client;
    }

    private Future<Response> get(HttpClientAgent client, int port) {
        return client.request(HttpMethod.GET, port, HOST, PATH)
                .compose(request -> request.send())
                .compose(response -> response.body().map(body ->
                        new Response(response.statusCode(), response.version())));
    }

    private record Response(int status, HttpVersion version) {
    }

    private static class AcceptingHandler implements CoreHandler<Request> {
        @Override
        public void handle(Request request) {
            request.accept();
        }

        @Override
        public String address() {
            return "accepting.handler";
        }
    }
}
