package com.codingchili.router.controller;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.configuration.RouterSettings;
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

import java.util.concurrent.TimeUnit;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 * <p>
 * Tests for the router handler.
 */
@RunWith(VertxUnitRunner.class)
public class RouterHandlerTest {
    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private RouterHandler handler;
    private CoreContext core;

    @Before
    public void setUp() {
        core = new SystemContext();
        system().setClusterTimeout(500);
        handler = new RouterHandler(new RouterContext(core) {
            @Override
            public RouterSettings service() {
                return new RouterSettings().addExternal(NODE_WEBSERVER, ".*");
            }
        });
        handler.init(core);
    }

    @After
    public void tearDown(TestContext test) {
        core.vertx().close(test.asyncAssertSuccess());
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

    private void handle(String target, ResponseListener listener) {
        handle(target, listener, new JsonObject());
    }

    private void handle(String target, ResponseListener listener, JsonObject payload) {
        if (payload == null) {
            payload = new JsonObject();
        }

        payload.put(PROTOCOL_TARGET, target);
        handler.handle(RequestMock.get(target, listener, payload));
    }

    private void mockNode(String target, Handler<Message<Object>> consumer) {
        core.bus().consumer(target, consumer);
    }

    private void mockNode(String target) {
        mockNode(target, message -> message.reply(message.body()));
    }
}
