package com.codingchili.router.controller.transport;

import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.model.WireType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.common.Strings.*;
import static com.codingchili.common.Strings.NODE_WEBSERVER;

/**
 * @author Robin Duda
 *
 * Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class RestListenerIT extends TransportTestCases {

    public RestListenerIT() {
        super(WireType.REST);
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
    public void testRouterSupportsGet(TestContext context) {
        Async async = context.async();

        sendGetRequest("/?" + ID_ROUTE + "=" + ID_PING + "&" + ID_TARGET + "=" + NODE_ROUTING, (result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        });
    }

    private void sendGetRequest(String action, ResponseListener listener) {
        vertx.createHttpClient().getNow(PORT, HOST, action, handler -> {

            handler.bodyHandler(body -> handleBody(listener, body));
        });
    }

    void sendRequest(String route, ResponseListener listener) {
        sendRequest(route, listener, new JsonObject());
    }

    void sendRequest(String route, ResponseListener listener, JsonObject data) {
        vertx.createHttpClient().post(PORT, HOST, route, handler -> {

            handler.bodyHandler(body -> handleBody(listener, body));
        }).end(data.encode());
    }
}
