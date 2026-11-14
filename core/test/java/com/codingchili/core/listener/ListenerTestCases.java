package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CompressionConfig;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.ContextMock;

/**
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
    protected static final String ROUTE_CLOSE = "close";
    protected static final String ROUTE_LIMIT = "limit";
    protected static final int LIMIT_BYTES = 64;
    protected static final String CLOSED_BEFORE_RESPONSE = "the connection closed before the response";
    private final List<Supplier<Future<?>>> clients = new CopyOnWriteArrayList<>();
    private final List<String> deployments = new CopyOnWriteArrayList<>();
    private Supplier<CoreListener> listener;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    protected ListenerTestCases(Supplier<CoreListener> listener) {
        this.listener = listener;
    }

    @BeforeClass
    public static void setUpClass() {
        context = new ContextMock();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Before
    public void setUp(TestContext test) {
        ListenerSettings settings = new ListenerSettings()
                .setMaxRequestBytes(MAX_REQUEST_BYTES)
                .setPort(0)
                .setSecure(false)
                .setTimeout(Duration.ofSeconds(7))
                .setDefaultTarget(NODE_WEBSERVER)
                .setHttpOptions(new HttpServerConfig().setCompressionConfig(new CompressionConfig().setCompressionEnabled(false)))
                .addMapping(PATCHING_ROOT, new Endpoint(NODE_PATCHING));
        configure(settings);

        // a failed deploy fails the test with its cause, the port is only read from a listener that listens.
        deploy(() -> listener.get().settings(settings).handler(new TestHandler()))
                .onComplete(test.asyncAssertSuccess(deployment ->
                        this.port = settings.getListenPorts().iterator().next()));
    }

    @After
    public void tearDownTest(TestContext test) {
        // the clients of the test first, then the listeners it deployed: the next test starts without them.
        List<Future<?>> closing = clients.stream().<Future<?>>map(Supplier::get).toList();
        List<String> deployed = List.copyOf(deployments);
        clients.clear();
        deployments.clear();

        Future.join(closing)
                .transform(closed -> Future.join(deployed.stream().<Future<?>>map(context::stop).toList()))
                .onComplete(test.asyncAssertSuccess());
    }

    /**
     * Deploys a listener that is undeployed after the test.
     *
     * @param listener creates the listener to deploy.
     * @return the deployment id.
     */
    protected Future<String> deploy(Supplier<CoreListener> listener) {
        return context.listener(listener).onSuccess(deployments::add);
    }

    /**
     * Keeps a client for the length of the test and closes it after. Vert.x closes a client that is garbage
     * collected, with its connections: a client only referenced from a callback loses the response in flight.
     *
     * @param client the client to keep.
     * @param close  closes the client.
     * @param <T>    the type of client.
     * @return the client.
     */
    protected <T> T keep(T client, Function<T, Future<?>> close) {
        clients.add(() -> close.apply(client));
        return client;
    }

    @Test
    @Ignore("Needs to be reconsidered/redesigned.")
    public void testLargePacketRejected(TestContext test) {
        sendRequest(new JsonObject()
                .put(DATA, new String(new byte[MAX_REQUEST_BYTES]).replace("\0", ONE_CHAR))
                .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER))
                .onComplete(test.asyncAssertSuccess(response ->
                        test.assertEquals(ResponseStatus.BAD, status(response))));
    }

    @Test
    public void testAccepted(TestContext test) {
        sendRequest(new JsonObject()
                .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                .put(CoreStrings.PROTOCOL_ROUTE, CoreStrings.ID_PING))
                .onComplete(test.asyncAssertSuccess(response ->
                        test.assertEquals(ResponseStatus.ACCEPTED, status(response))));
    }

    /**
     * Override to change the listener settings before the listener is deployed.
     *
     * @param settings the settings used by the listener under test.
     */
    protected void configure(ListenerSettings settings) {
    }

    /**
     * Implementing class must provide transport specific implementation. Keep the client with {@link #keep}.
     *
     * @param data the request data with route and target.
     * @return the response, failed when the request fails: the connection fails or closes before the response.
     */
    public abstract Future<JsonObject> sendRequest(JsonObject data);

    /**
     * Completes the promise with a message that is a whole response, fails it if the message is not json.
     *
     * @param response the promise to complete.
     * @param message  a whole response.
     */
    protected static void complete(Promise<JsonObject> response, Buffer message) {
        try {
            response.tryComplete(message.toJsonObject());
        } catch (RuntimeException e) {
            response.tryFail(e);
        }
    }

    protected static ResponseStatus status(JsonObject response) {
        return ResponseStatus.valueOf(response.getString(CoreStrings.PROTOCOL_STATUS));
    }

    public class TestHandler implements CoreHandler<Request> {
        private String address = "test.address.node";

        @Override
        public void handle(Request request) {
            if (ROUTE_CLOSE.equals(request.route())) {
                // the server closes the connection: the client sees it closed.
                request.connection().close();
            } else if (ROUTE_LIMIT.equals(request.route())) {
                // lowers the limit of this connection: a larger message is refused before it is decoded.
                request.connection().maxRequestBytes(LIMIT_BYTES);
                request.accept();
            } else {
                request.accept();
            }
        }

        @Override
        public String address() {
            return address;
        }
    }
}
