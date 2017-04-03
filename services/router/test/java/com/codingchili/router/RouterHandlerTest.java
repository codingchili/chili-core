package com.codingchili.router;

import com.codingchili.common.Strings;
import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the router handler.
 */
@RunWith(VertxUnitRunner.class)
public class RouterHandlerTest {
    private Vertx vertx;
    private RouterHandler handler;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();

        handler = new RouterHandler<>(new RouterContext(vertx) {
            @Override
            public boolean isRouteHidden(String node) {
                return node.equals(NODE_LOGGING);
            }
        });
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testRouteWebServer(TestContext context) {
        Async async = context.async();

        mockNode(NODE_WEBSERVER);

        handle(NODE_WEBSERVER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testTimeout(TestContext test) {
        Async async = test.async();
        vertx.eventBus().consumer(NODE_WEBSERVER, handler -> {
            // trigger a timeout.
        });

        handle(NODE_WEBSERVER, ((response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            test.assertTrue(response.getString(PROTOCOL_MESSAGE).contains(new ListenerSettings().getTimeout() + ""));
            async.complete();
        }));
    }

    @Test
    public void testMissingNode(TestContext test) {
        Async async = test.async();

        handle(NODE_WEBSERVER, ((response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            test.assertEquals(response.getString(PROTOCOL_MESSAGE), Strings.getNodeNotReachable(NODE_WEBSERVER));
            async.complete();
        }));
    }

    @Test
    public void testRouteClientAuthentication(TestContext context) {
        Async async = context.async();

        mockNode(NODE_AUTHENTICATION_CLIENTS);

        handle(NODE_AUTHENTICATION_CLIENTS, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteRealmAuthentication(TestContext context) {
        Async async = context.async();

        mockNode(NODE_AUTHENTICATION_REALMS);

        handle(NODE_AUTHENTICATION_REALMS, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteRealm(TestContext context) {
        Async async = context.async();

        mockNode("game#1" + NODE_REALM);

        handle("game#1" + NODE_REALM, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteHidden(TestContext context) {
        Async async = context.async();

        handle(NODE_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteClientLogger(TestContext context) {
        Async async = context.async();

        mockNode(NODE_CLIENT_LOGGING);

        handle(NODE_CLIENT_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRoutePatchServer(TestContext context) {
        Async async = context.async();

        mockNode(NODE_PATCHING);

        handle(NODE_PATCHING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteMissing(TestContext context) {
        Async async = context.async();

        handle("missing.node", (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        });
    }

    @Test
    public void testPingRouter(TestContext context) {
        Async async = context.async();

        handle(NODE_ROUTING, (response, status) -> {
            context.assertEquals(status, ResponseStatus.ACCEPTED);
            async.complete();
        });
    }

    @Test
    public void testValidationFail(TestContext context) {
        Async async = context.async();

        handle(ID_PING, (response, status) -> {
            context.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject()
                .put(ID_NAME, "invalid characters #*#&(@"));
    }

    private void handle(String target, ResponseListener listener) {
        handle(target, listener, new JsonObject());
    }

    private void handle(String target, ResponseListener listener, JsonObject payload) {
        if (payload == null) {
            payload = new JsonObject();
        }

        payload.put(PROTOCOL_TARGET, target);
        handler.process(RequestMock.get(target, listener, payload));
    }

    /**
     * mock a cluster node with target name.
     *
     * @param target routing id of the node to mock.
     */
    private void mockNode(String target) {
        vertx.eventBus().consumer(target, message -> message.reply(message.body()));
    }
}
