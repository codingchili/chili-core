package com.codingchili.router.controller.transport;

import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.security.RemoteIdentity;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterSettings;
import com.codingchili.router.model.Endpoint;
import com.codingchili.router.model.WireType;
import com.codingchili.common.Strings;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *
 * Contains test cases for transport implementations.
 */
@Ignore
@RunWith(VertxUnitRunner.class)
public abstract class TransportTestCases {
    static final String PATCHING_ROOT = "/patching";
    private static RouterSettings settings;
    private static ListenerSettings listener;
    static final String HOST = "localhost";
    static final int PORT = 19797;
    Vertx vertx;
    private ContextMock context;
    private WireType wireType;

    TransportTestCases(WireType wireType) {
        this.wireType = wireType;
    }

    @Rule
    public Timeout timeout = new Timeout(12, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext testContext) {
        Async async = testContext.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            vertx = cluster.result();

            settings = new RouterSettings();
            context = new ContextMock(vertx);

            ArrayList<ListenerSettings> transport = new ArrayList<>();

            settings.setIdentity(new RemoteIdentity("node", "at"));

            listener = new ListenerSettings();
            listener.setMaxRequestBytes(10)
                    .setPort(PORT)
                    .setType(wireType)
                    .setTimeout(105000)
                    .setHttpOptions(new HttpServerOptions().setCompressionSupported(false));

            HashMap<String, Endpoint> api = new HashMap<>();
            api.put(PATCHING_ROOT, new Endpoint().setTarget(NODE_PATCHING));
            listener.setApi(api);

            settings.getHidden().add(Strings.NODE_LOGGING);
            transport.add(listener);
            settings.setTransport(transport);
            context.setSettings(settings);

            vertx.deployVerticle(fromWireType(wireType), deploy -> {
                async.complete();
            });
        });
    }

    private ClusterNode fromWireType(WireType type) {
        switch (type) {
            case REST:
                return new RestListener(new RouterHandler<>(context));
            case UDP:
                return new UdpListener(new RouterHandler<>(context));
            default:
                throw new RuntimeException("Transport verticle undefined for given transport type");
        }
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testLargePacketRejected(TestContext context) {
        Async async = context.async();

        mockNode(PATCHING_ROOT);

        sendRequest(PATCHING_ROOT, (result, status) -> {
            context.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject().put("data", "request is larger than 10 bytes!!"));
    }

    void mockNode(String target) {
        context.bus().consumer(target, message -> {
            message.reply(new JsonObject().put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED).put(ID_TARGET, target));
        });
    }

    /**
     * Implementing class should provide transport specific implementation.
     *
     * @param route    the request route
     * @param listener invoked with the request response
     */
    abstract void sendRequest(String route, ResponseListener listener);

    /**
     * Implementing class must provide transport specific implementation.
     *
     * @param route    the request route
     * @param listener invoked with the request response
     * @param data     the response data.
     */
    abstract void sendRequest(String route, ResponseListener listener, JsonObject data);

    void handleBody(ResponseListener listener, Buffer body) {
        ResponseStatus status = ResponseStatus.valueOf(body.toJsonObject().getString(PROTOCOL_STATUS));
        listener.handle(body.toJsonObject(), status);
    }

    @FunctionalInterface
    interface ResponseListener {
        void handle(JsonObject result, ResponseStatus status);
    }
}
