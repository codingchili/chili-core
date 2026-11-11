package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.RestListener;
import com.codingchili.core.listener.transport.WebsocketListener;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * Tests that the REST and websocket listeners take the address of the client from X-Forwarded-For only
 * when the peer is a trusted proxy.
 */
@RunWith(VertxUnitRunner.class)
public class ProxiesIT {
    private static final String HOST = CoreStrings.getLoopbackAddress();
    private static final String REMOTE = "remote";
    private static final String CLIENT = "203.0.113.7";
    private static final String SPOOFED = "198.51.100.1";
    private static final List<String> LOOPBACK = List.of("127.0.0.0/8", "::1");
    private ContextMock context;
    private HttpClient http;
    private WebSocketClient websocket;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        context = new ContextMock();
        http = context.vertx().createHttpClient();
        websocket = context.vertx().createWebSocketClient();
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testRestTrustedPeer(TestContext test) {
        rest(test, LOOPBACK, CLIENT, CLIENT);
    }

    @Test
    public void testRestUntrustedPeer(TestContext test) {
        rest(test, List.of("10.0.0.0/8"), CLIENT, HOST);
    }

    @Test
    public void testRestNoProxies(TestContext test) {
        rest(test, List.of(), CLIENT, HOST);
    }

    @Test
    public void testRestChainOfProxies(TestContext test) {
        rest(test, List.of("127.0.0.0/8", "::1", "10.0.0.0/8"), SPOOFED + ", " + CLIENT + ", 10.0.0.3, 10.0.0.2", CLIENT);
    }

    @Test
    public void testWebsocketTrustedPeer(TestContext test) {
        websocket(test, LOOPBACK, CLIENT, CLIENT);
    }

    @Test
    public void testWebsocketUntrustedPeer(TestContext test) {
        websocket(test, List.of("10.0.0.0/8"), CLIENT, HOST);
    }

    @Test
    public void testWebsocketChainOfProxies(TestContext test) {
        websocket(test, List.of("127.0.0.0/8", "::1", "10.0.0.0/8"), SPOOFED + ", " + CLIENT + ", 10.0.0.3", CLIENT);
    }

    @Test
    public void testInvalidProxyFailsTheDeployment(TestContext test) {
        var settings = settings(List.of("localhost"));
        context.listener(() -> new RestListener().settings(settings).handler(new RemoteHandler()))
                .onComplete(test.asyncAssertFailure(e -> test.assertTrue(e.getMessage().contains("localhost"))));
    }

    private void rest(TestContext test, List<String> proxies, String forwarded, String expected) {
        Async async = test.async();
        deploy(RestListener::new, proxies).compose(port -> http.request(new RequestOptions()
                        .setMethod(HttpMethod.POST)
                        .setHost(HOST)
                        .setPort(port)
                        .setURI(DIR_ROOT + ID_PING)
                        .putHeader(PROTOCOL_FORWARDED_FOR, forwarded)))
                .compose(request -> request.send(new JsonObject().put(PROTOCOL_TARGET, ID_PING).encode()))
                .compose(HttpClientResponse::body)
                .onComplete(test.asyncAssertSuccess(body -> {
                    test.assertEquals(expected, body.toJsonObject().getString(REMOTE));
                    async.complete();
                }));
    }

    private void websocket(TestContext test, List<String> proxies, String forwarded, String expected) {
        Async async = test.async();
        deploy(WebsocketListener::new, proxies).compose(port -> websocket.connect(new WebSocketConnectOptions()
                        .setHost(HOST)
                        .setPort(port)
                        .setURI(DIR_SEPARATOR)
                        .addHeader(PROTOCOL_FORWARDED_FOR, forwarded)))
                .onComplete(test.asyncAssertSuccess(socket -> {
                    socket.handler(body -> {
                        test.assertEquals(expected, body.toJsonObject().getString(REMOTE));
                        async.complete();
                    });
                    socket.write(Buffer.buffer(new JsonObject()
                            .put(PROTOCOL_TARGET, ID_PING)
                            .put(PROTOCOL_ROUTE, ID_PING).encode()));
                }));
    }

    private Future<Integer> deploy(Supplier<CoreListener> listener, List<String> proxies) {
        var settings = settings(proxies);
        return context.listener(() -> listener.get().settings(settings).handler(new RemoteHandler()))
                .map(id -> settings.getListenPorts().iterator().next());
    }

    private static ListenerSettings settings(List<String> proxies) {
        return new ListenerSettings()
                .setPort(0)
                .setSecure(false)
                .setProxies(proxies);
    }

    /**
     * Answers each request with the address of its client.
     */
    private static class RemoteHandler implements CoreHandler<Request> {
        @Override
        public void handle(Request request) {
            request.write(Response.status(request.target(), request.route(), ACCEPTED)
                    .put(REMOTE, request.connection().remote()));
        }

        @Override
        public String address() {
            return ID_PING;
        }
    }
}
