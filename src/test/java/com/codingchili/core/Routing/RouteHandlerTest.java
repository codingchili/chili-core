package com.codingchili.core.Routing;

import com.codingchili.core.Routing.Configuration.RouteProvider;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Protocols.ResponseStatus;
import io.vertx.core.Vertx;
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

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class RouteHandlerTest {
    private Vertx vertx;
    private RouteHandler handler;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        handler = new RouteHandler(new RouteProvider(vertx));
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testRouteWebServer(TestContext context) {
        Async async = context.async();

        mockNode(NODE_WEBSERVER);

        handle("/test", ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRouteClientAuthentication(TestContext context) {
        Async async = context.async();

        handle(NODE_AUTHENTICATION_CLIENTS, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRouteRealmAuthentication(TestContext context) {
        Async async = context.async();

        handle(NODE_AUTHENTICATION_REALMS, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRouteRealm(TestContext context) {
        Async async = context.async();

        handle("game#1" + NODE_REALM, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRouteLogger(TestContext context) {
        Async async = context.async();

        handle(NODE_LOGGING, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRoutePatchServer(TestContext context) {
        Async async = context.async();

        handle(NODE_PATCHING, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }));
    }

    @Test
    public void testRouteMissing(TestContext context) {
        Async async = context.async();

        handle("missing.node", ((response, status) -> {
            context.assertEquals(ResponseStatus.MISSING, status);
            async.complete();
        }));
    }

    private void handle(String target, ResponseListener listener) {
        mockNode(target);
        handler.process(new RouteRequestMock(target, listener, new JsonObject()));
    }

    /**
     * mock a cluster node with target name.
     * @param target routing id of the node to mock.
     */
    private void mockNode(String target) {
        vertx.eventBus().consumer(target, message -> message.reply(message.body()));
    }
}
