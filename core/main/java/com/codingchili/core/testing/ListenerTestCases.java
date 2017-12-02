package com.codingchili.core.testing;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.*;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.ContextMock;
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

/**
 * @author Robin Duda
 * <p>
 * Contains test cases for transport implementations.
 */
@Ignore("Extend this class to run the tests.")
@RunWith(VertxUnitRunner.class)
public abstract class ListenerTestCases {

    protected int port;
    protected static ContextMock context;

    protected static final String NODE_ROUTER = "router.node";
    protected static final String HOST = CoreStrings.getLoopbackAddress();
    private static final String NODE_PATCHING = "patching.node";
    private static final String NODE_WEBSERVER = "webserver.node";
    private static final String PATCHING_ROOT = "/patching";
    private static final int MAX_REQUEST_BYTES = 256;
    private static final String ONE_CHAR = "x";
    private static final String DATA = "data";
    private Supplier<CoreListener> listener;
    private WireType wireType;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    protected ListenerTestCases(WireType wireType, Supplier<CoreListener> listener) {
        this.wireType = wireType;
        this.listener = listener;
    }

    @BeforeClass
    public static void setUpClass() {
        context = new ContextMock();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        ListenerSettings settings = new ListenerSettings()
                .setMaxRequestBytes(MAX_REQUEST_BYTES)
                .setPort(0)
                .setSecure(false)
                .setType(wireType)
                .setTimeout(7000)
                .setDefaultTarget(NODE_WEBSERVER)
                .setHttpOptions(new HttpServerOptions().setCompressionSupported(false))
                .addMapping(PATCHING_ROOT, new Endpoint(NODE_PATCHING));

        context.listener(() -> listener.get().settings(() -> settings).handler(new TestHandler())).setHandler(deploy -> {
            if (deploy.failed()) {
                deploy.cause().printStackTrace();
                test.fail(deploy.cause());
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

    /**
     * Implementing class must provide transport specific implementation.
     *
     * @param listener invoked with the request response
     * @param data     the request data with route and target.
     */
    public abstract void sendRequest(ResponseListener listener, JsonObject data);

    protected void handleBody(ResponseListener listener, Buffer body) {
        ResponseStatus status = ResponseStatus.valueOf(body.toJsonObject().getString(CoreStrings.PROTOCOL_STATUS));
        listener.handle(body.toJsonObject(), status);
    }

    @FunctionalInterface
    public interface ResponseListener {
        void handle(JsonObject result, ResponseStatus status);
    }

    public class TestHandler implements CoreHandler<Request> {
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
}
