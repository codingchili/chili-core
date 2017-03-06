package com.codingchili.router.controller.transport;

import com.codingchili.common.Strings;
import com.codingchili.router.Service;
import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterSettings;
import com.codingchili.router.model.Endpoint;
import com.codingchili.router.model.WireType;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.security.RemoteIdentity;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Contains test cases for transport implementations.
 */
@Ignore("Extend this class to run the tests.")
@RunWith(VertxUnitRunner.class)
public abstract class TransportTestCases {
    static final String PATCHING_ROOT = "/patching";
    static final String HOST = getLoopbackAddress();
    private static final AtomicInteger PORT = new AtomicInteger(39885);
    private static final int MAX_REQUEST_BYTES = 256;
    private static final String ONE_CHAR = "x";
    private static final String DATA = "data";
    private ContextMock context;
    private WireType wireType;
    int port = PORT.getAndDecrement();
    Vertx vertx;

    TransportTestCases(WireType wireType) {
        this.wireType = wireType;
    }

    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

            vertx = Vertx.vertx();
            context = new ContextMock(vertx);

        ListenerSettings listener = new ListenerSettings()
                .setMaxRequestBytes(MAX_REQUEST_BYTES)
                .setPort(port)
                .setType(wireType)
                .setTimeout(105000)
                .setHttpOptions(new HttpServerOptions().setCompressionSupported(false))
                .addMapping(PATCHING_ROOT, new Endpoint(NODE_PATCHING));

        RouterSettings settings = new RouterSettings(new RemoteIdentity("node", "host"))
                .setHidden(NODE_LOGGING)
                .addTransport(listener);

            settings.setHidden(Strings.NODE_LOGGING);
            settings.addTransport(listener);
            context.setSettings(settings);

            vertx.deployVerticle(new Service(context), deploy -> {
                test.assertTrue(deploy.succeeded(), deploy.cause().getMessage());
                async.complete();
            });
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testLargePacketRejected(TestContext test) {
        Async async = test.async();

        mockNode(PATCHING_ROOT);

        sendRequest(PATCHING_ROOT, (result, status) -> {
            test.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject()
                .put(DATA, new String(new byte[MAX_REQUEST_BYTES]).replace("\0", ONE_CHAR)));
    }

    @Test
    public void testAccepted(TestContext test) {
        Async async = test.async();

        sendRequest(DIR_ROOT, (result, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(PROTOCOL_TARGET, NODE_ROUTING)
                .put(PROTOCOL_ROUTE, ID_PING));
    }

    void mockNode(String node) {
        context.bus().consumer(node, message -> {
            message.reply(new JsonObject()
                    .put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED)
                    .put(PROTOCOL_TARGET, node));
        });
    }

    /**
     * Implementing class must provide transport specific implementation.
     *
     * @param route    the request route
     * @param listener invoked with the request response
     * @param data     the request data.
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
