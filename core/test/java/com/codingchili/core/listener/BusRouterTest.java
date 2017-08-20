package com.codingchili.core.listener;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.system;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the router handler.
 */
@RunWith(VertxUnitRunner.class)
public class BusRouterTest {
    private static final String SAMPLE_URL = "/files/metadata/download.json";
    private static final String MISSING_NODE = "missing.node";
    private static final String NODE_1 = "first.node";
    private static final String NODE_2 = "second.node";
    private static final String API_ROOT = "/web";
    private AtomicBoolean nodeTimeout = new AtomicBoolean(false);
    private AtomicBoolean nodeUnreachable = new AtomicBoolean(false);
    private AtomicBoolean recipientFailure = new AtomicBoolean(false);
    private BusRouter router;
    private CoreContext core;

    @Rule
    public Timeout timeout = new Timeout(500, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        core = new SystemContext();
        system().setClusterTimeout(500);
        router = new BusRouter()
        {
            @Override
            public void onNodeTimeout(Request request) {
                nodeTimeout.set(true);
                super.onNodeTimeout(request);
            }

            @Override
            public void onNodeNotReachable(Request request) {
                nodeUnreachable.set(true);
                super.onNodeNotReachable(request);
            }

            @Override
            public void onRecipientFailure(Request request) {
                recipientFailure.set(true);
                super.onRecipientFailure(request);
            }
        };
        router.init(core);
    }

    @After
    public void tearDown(TestContext test) {
        core.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testRouteWebServer(TestContext test) {
        Async async = test.async();

        mockNode(NODE_1);

        handle(NODE_1, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    @Test
    public void testMappedApiRoutes(TestContext context) {
        Async async = context.async();

        mockNode(NODE_2);

        handle(NODE_2, (result, status) -> {
            context.assertEquals(ACCEPTED, status);
            context.assertEquals(NODE_2, result.getString(CoreStrings.PROTOCOL_TARGET));
            async.complete();
        }, new JsonObject().put(CoreStrings.PROTOCOL_ROUTE, API_ROOT));
    }

    @Test
    public void testUnmappedApiRoutesWebserver(TestContext context) {
        Async async = context.async();

        mockNode(NODE_1);

        handle(NODE_1, (result, status) -> {
            context.assertEquals(ACCEPTED, status);
            context.assertEquals(NODE_1, result.getString(CoreStrings.PROTOCOL_TARGET));
            async.complete();
        }, new JsonObject().put(CoreStrings.PROTOCOL_ROUTE, SAMPLE_URL));
    }

    @Test
    public void testTimeout(TestContext test) {
        Async async = test.async();
        mockNode(NODE_1, (message) -> {
        });

        handle(NODE_1, ((response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            test.assertTrue(response.getString(PROTOCOL_MESSAGE).contains(system().getClusterTimeout() + ""));
            async.complete();
        }));
    }

    @Test
    public void testMissingNode(TestContext test) {
        Async async = test.async();

        handle(NODE_1, ((response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            test.assertEquals(response.getString(PROTOCOL_MESSAGE), getNodeNotReachable(NODE_1));
            async.complete();
        }));
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

        handle(ID_PING, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            async.complete();
        });
    }

    @Test
    public void testRouteNodeTimeoutLogged(TestContext test) {
        Async async = test.async();

        String node = UUID.randomUUID().toString();
        mockNode(node, message -> {
        });
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
        router.handle(RequestMock.get(target, listener, payload));
    }

    private void mockNode(String target, Handler<Message<Object>> consumer) {
        core.bus().consumer(target, consumer);
    }

    private void mockNode(String target) {
        mockNode(target, message -> message.reply(message.body()));
    }
}
