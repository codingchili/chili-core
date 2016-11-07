package com.codingchili.services.Router.Controller.Transport;

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

import com.codingchili.core.Protocol.ResponseStatus;
import com.codingchili.core.Security.RemoteIdentity;

import com.codingchili.services.Router.Configuration.ListenerSettings;
import com.codingchili.services.Router.Configuration.RouterSettings;
import com.codingchili.services.Router.Controller.RouterHandler;
import com.codingchili.services.Router.Model.Endpoint;
import com.codingchili.services.Router.Model.WireType;
import com.codingchili.services.Shared.Strings;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class  RestListenerTest {
    private static final String HOST = "localhost";
    private static final String PATCHING_ROOT = "/patching";
    private static final int PORT = 19797;
    private static Vertx vertx;
    private static RouterSettings settings;
    private static ContextMock context;
    private static ListenerSettings listener;

    @Rule
    public Timeout timeout = new Timeout(12, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp(TestContext testContext) {
        Async async = testContext.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            vertx = cluster.result();

            settings = new RouterSettings();
            context = new ContextMock(vertx);

            ArrayList<ListenerSettings> transport = new ArrayList<>();

            settings.setIdentity(new RemoteIdentity("node", "at"));

            listener = new ListenerSettings();
            listener.setMaxRequestBytes(10);
            listener.setPort(PORT);
            listener.setType(WireType.REST);
            listener.setTimeout(105000);
            listener.setHttpOptions(new HttpServerOptions().setCompressionSupported(false));

            HashMap<String, Endpoint> api = new HashMap<>();
            api.put(PATCHING_ROOT, new Endpoint().setTarget(NODE_PATCHING));
            listener.setApi(api);

            settings.getHidden().add(Strings.NODE_LOGGING);

            transport.add(listener);

            settings.setTransport(transport);

            context.setSettings(settings);

            vertx.deployVerticle(new RestListener(new RouterHandler<>(context)), deploy -> {
                async.complete();
            });
        });
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testLargePacketRejected(TestContext context) {
        Async async = context.async();

        sendRequest(PATCHING_ROOT, (result, status) -> {
            context.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject().put("data", "request is larger than 10 bytes!!"));
    }

    @Test
    public void testUnmappedApiRoutesWebserver(TestContext context) {
        Async async = context.async();

        mockNode(NODE_WEBSERVER);

        sendRequest("/", (result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(NODE_WEBSERVER, result.getString(ID_TARGET));
            async.complete();
        });
    }

    @Test
    public void testMappedApiRoutes(TestContext context) {
        Async async = context.async();

        mockNode(NODE_PATCHING);

        sendRequest(PATCHING_ROOT, (result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(NODE_PATCHING, result.getString(ID_TARGET));
            async.complete();
        });
    }

    @Test
    public void testRouterSupportsGet(TestContext context) {
        Async async = context.async();

        sendGetRequest("/?" + ID_ACTION + "=" + ID_PING + "&" + ID_TARGET + "=" + NODE_ROUTING, (result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    private void mockNode(String target) {
        context.bus().consumer(target, message -> {
            message.reply(new JsonObject().put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED).put(ID_TARGET, target));
        });
    }

    private void sendRequest(String action, ResponseListener listener) {
        sendRequest(action, listener, new JsonObject());
    }

    private void sendRequest(String action, ResponseListener listener, JsonObject data) {
        vertx.createHttpClient().post(PORT, HOST, action, handler -> {

            handler.bodyHandler(body -> handleBody(listener, body));
        }).end(data.encode());
    }

    private void sendGetRequest(String action, ResponseListener listener) {
        vertx.createHttpClient().getNow(PORT, HOST, action, handler -> {

            handler.bodyHandler(body -> handleBody(listener, body));
        });
    }

    private void handleBody(ResponseListener listener, Buffer body) {
        ResponseStatus status = ResponseStatus.valueOf(body.toJsonObject().getString(PROTOCOL_STATUS));
        listener.handle(body.toJsonObject(), status);
    }

    @FunctionalInterface
    private interface ResponseListener {
        void handle(JsonObject result, ResponseStatus status);
    }
}
