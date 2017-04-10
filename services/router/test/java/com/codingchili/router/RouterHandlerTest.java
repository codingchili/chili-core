package com.codingchili.router;

import com.codingchili.common.Strings;
import com.codingchili.router.configuration.*;
import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the router handler.
 */
@RunWith(VertxUnitRunner.class)
public class RouterHandlerTest {
    private static final String MISSING_NODE = "missing.node";
    private AtomicBoolean nodeTimeout = new AtomicBoolean(false);
    private AtomicBoolean nodeUnreachable = new AtomicBoolean(false);
    private AtomicBoolean recipientFailure = new AtomicBoolean(false);
    private Vertx vertx;
    private RouterHandler handler;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        system().setClusterTimeout(500);
        handler = new RouterHandler<>(new RouterContext(vertx) {

            @Override
            public RouterSettings service() {
                return new RouterSettings().addHidden(NODE_LOGGING);
            }
            @Override
            public void onNodeTimeout(String target, String route, int timeout) {
                nodeTimeout.set(true);
            }

            @Override
            public void onNodeNotReachable(String target) {
                nodeUnreachable.set(true);
            }

            @Override
            public void onRecipientFailure(String target, String route) {
                recipientFailure.set(true);
            }
        });
    }

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void testRouteWebServer(TestContext test) {
        Async async = test.async();

        mockNode(NODE_WEBSERVER);

        handle(NODE_WEBSERVER, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testTimeout(TestContext test) {
        Async async = test.async();
        mockNode(NODE_WEBSERVER, (message) -> {});

        handle(NODE_WEBSERVER, ((response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            test.assertTrue(response.getString(PROTOCOL_MESSAGE).contains(system().getClusterTimeout() + ""));
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
    public void testRouteClientAuthentication(TestContext test) {
        Async async = test.async();

        mockNode(NODE_AUTHENTICATION_CLIENTS);

        handle(NODE_AUTHENTICATION_CLIENTS, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteRealmAuthentication(TestContext test) {
        Async async = test.async();

        mockNode(NODE_AUTHENTICATION_REALMS);

        handle(NODE_AUTHENTICATION_REALMS, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteRealm(TestContext test) {
        Async async = test.async();

        mockNode("game#1" + NODE_REALM);

        handle("game#1" + NODE_REALM, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteHidden(TestContext test) {
        Async async = test.async();

        handle(NODE_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteHiddenCaseSensitive(TestContext test) {
        Async async = test.async();

        handle(NODE_LOGGING.toUpperCase(), (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteClientLogger(TestContext test) {
        Async async = test.async();

        mockNode(NODE_CLIENT_LOGGING);

        handle(NODE_CLIENT_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRoutePatchServer(TestContext test) {
        Async async = test.async();

        mockNode(NODE_PATCHING);

        handle(NODE_PATCHING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testRouteMissing(TestContext test) {
        Async async = test.async();

        handle(MISSING_NODE, (response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        });
    }

    @Test
    public void testPingRouter(TestContext test) {
        Async async = test.async();

        handle(NODE_ROUTER, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            async.complete();
        });
    }

    @Test
    public void testValidationFail(TestContext test) {
        Async async = test.async();

        handle(ID_PING, (response, status) -> {
            test.assertEquals(ResponseStatus.BAD, status);
            async.complete();
        }, new JsonObject()
                .put(ID_NAME, "invalid characters #*#&(@"));
    }

    @Test
    public void testRouteNodeTimeoutLogged(TestContext test) {
        Async async = test.async();

        String node = UUID.randomUUID().toString();
        mockNode(node, message -> {});
        handle(node, ((response, status) -> {
            test.assertTrue(nodeTimeout.get());
            async.complete();
        }));
    }

    @Test
    public void testRouteNodeNoHandlersLogged(TestContext test) {
        Async async = test.async();

        String node = UUID.randomUUID().toString();
        handle(node, ((response, status) -> {
            test.assertTrue(nodeUnreachable.get());
            async.complete();
        }));
    }

    @Test
    public void testRouteRecipientFailureLogged(TestContext test) {
        Async async = test.async();

        String node = UUID.randomUUID().toString();
        mockNode(node, message -> message.fail(0, "fail"));
        handle(node, ((response, status) -> {
            test.assertTrue(recipientFailure.get());
            async.complete();
        }));
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

    private void mockNode(String target, Handler<Message<Object>> consumer) {
        vertx.eventBus().consumer(target, consumer);
    }

    private void mockNode(String target) {
        mockNode(target, message -> message.reply(message.body()));
    }
}
