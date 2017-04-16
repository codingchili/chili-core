package com.codingchili.core.listener;

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
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.*;
import com.codingchili.core.protocol.*;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *         <p>
 *         Contains test cases for transport implementations.
 */
@Ignore("Extend this class to run the tests.")
@RunWith(VertxUnitRunner.class)
public abstract class TransportTestCases {
    private static final String NODE_PATCHING = "patching.node";
    private static final String NODE_WEBSERVER = "webserver.node";
    private static final String PATCHING_ROOT = "/patching";
    static final String NODE_ROUTER = "router.node";
    static final String HOST = CoreStrings.getLoopbackAddress();
    private static final int MAX_REQUEST_BYTES = 256;
    private static final String ONE_CHAR = "x";
    private static final String DATA = "data";
    private Supplier<CoreListener> listener;
    private ContextMock context;
    private WireType wireType;
    protected int port;
    protected static Vertx vertx;

    TransportTestCases(WireType wireType, Supplier<CoreListener> listener) {
        this.wireType = wireType;
        this.listener = listener;
    }

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUpClass() {
        vertx = Vertx.vertx();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        context = new ContextMock(vertx);

        ListenerSettings settings = new ListenerSettings()
                .setMaxRequestBytes(MAX_REQUEST_BYTES)
                .setPort(0)
                .setType(wireType)
                .setTimeout(7000)
                .setDefaultTarget(NODE_WEBSERVER)
                .setHttpOptions(new HttpServerOptions().setCompressionSupported(false))
                .addMapping(PATCHING_ROOT, new Endpoint(NODE_PATCHING));

        context.listener(listener.get().settings(() -> settings).handler(new TestHandler()), deploy -> {
            if (deploy.failed()) {
                deploy.cause().printStackTrace();
            }

            this.port = settings.getListenPorts().iterator().next();
            test.assertTrue(deploy.succeeded());
            async.complete();
        });
    }

    @Test
    public void testLargePacketRejected(TestContext test) {
        Async async = test.async();

        sendRequest((result, status) -> {
            test.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject()
                .put(DATA, new String(new byte[MAX_REQUEST_BYTES]).replace("\0", ONE_CHAR))
                .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER));
    }

    @Test
    public void testAccepted(TestContext test) {
        Async async = test.async();

        sendRequest((result, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                .put(CoreStrings.PROTOCOL_ROUTE, CoreStrings.ID_PING));
    }

    private class TestHandler implements CoreHandler {
        private String address = "test.address.node";

        @Override
        public void handle(Request request) {
            request.accept();
        }

        @Override
        public String address() {
            return address;
        }
    }

    /**
     * Implementing class must provide transport specific implementation.
     *
     * @param listener invoked with the request response
     * @param data     the request data with route and target.
     */
    abstract void sendRequest(ResponseListener listener, JsonObject data);

    void handleBody(ResponseListener listener, Buffer body) {
        ResponseStatus status = ResponseStatus.valueOf(body.toJsonObject().getString(CoreStrings.PROTOCOL_STATUS));
        listener.handle(body.toJsonObject(), status);
    }

    @FunctionalInterface
    interface ResponseListener {
        void handle(JsonObject result, ResponseStatus status);
    }
}
