package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class RestListenerIT extends TransportTestCases {
    private static final String SAMPLE_URL = "/files/metadata/download.json";

    public RestListenerIT() {
        super(WireType.REST);
    }

    @Test
    public void testMappedApiRoutes(TestContext context) {
        Async async = context.async();

        mockNode(NODE_PATCHING);

        sendRequest((result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(NODE_PATCHING, result.getString(PROTOCOL_TARGET));
            async.complete();
        }, new JsonObject().put(PROTOCOL_ROUTE, PATCHING_ROOT));
    }

    @Test
    public void testUnmappedApiRoutesWebserver(TestContext context) {
        Async async = context.async();

        mockNode(NODE_WEBSERVER);

        sendRequest((result, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(NODE_WEBSERVER, result.getString(PROTOCOL_TARGET));
            async.complete();
        }, new JsonObject().put(PROTOCOL_ROUTE, SAMPLE_URL));
    }

    @Test
    public void testRouterSupportsGet(TestContext context) {
        Async async = context.async();

        sendGetRequest("/?" + PROTOCOL_ROUTE + "=" + ID_PING + "&" + PROTOCOL_TARGET + "=" + NODE_ROUTING,
                (result, status) -> {
                    context.assertEquals(ResponseStatus.ACCEPTED, status);
                    async.complete();
                });
    }

    private void sendGetRequest(String action, ResponseListener listener) {
        vertx.createHttpClient().getNow(port, HOST, action, handler -> {
            handler.bodyHandler(body -> handleBody(listener, body));
        });
    }

    @Override
    void sendRequest(ResponseListener listener, JsonObject data) {
        String target = data.getString(PROTOCOL_ROUTE);

        if (target == null) {
            target = "";
        } else {
            if (!target.startsWith(DIR_ROOT)) {
                target = DIR_ROOT + target;
            }
            data.remove(PROTOCOL_ROUTE);
        }

        vertx.createHttpClient().post(port, HOST, target, handler -> {
            handler.bodyHandler(body -> handleBody(listener, body));
        }).end(data.encode());
    }
}
