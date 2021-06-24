package com.codingchili.core.listener;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.RestListener;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class RestListenerIT extends ListenerTestCases {

    public RestListenerIT() {
        super(WireType.REST, RestListener::new);
    }

    @Test
    public void testListenerSupportsGet(TestContext test) {
        Async async = test.async();

        sendGetRequest(String.format("/?%s=%s&%s=%s", PROTOCOL_ROUTE, ID_PING, PROTOCOL_TARGET, NODE_ROUTER),
                (result, status) -> {
                    test.assertEquals(ACCEPTED, status);
                    async.complete();
                });
    }

    private void sendGetRequest(String action, ResponseListener listener) {
        context.vertx().createHttpClient().request(HttpMethod.GET, port, HOST, action, handler -> {

            handler.result().send().onComplete(response -> {
                response.result().bodyHandler(body -> handleBody(listener, body));
            });
        });
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        String target = data.getString(PROTOCOL_ROUTE);

        if (target == null) {
            target = "";
        } else {
            if (!target.startsWith(CoreStrings.DIR_ROOT)) {
                target = CoreStrings.DIR_ROOT + target;
            }
            data.remove(PROTOCOL_ROUTE);
        }

        context.vertx().createHttpClient().request(HttpMethod.POST, port, HOST, target, handler -> {
            HttpClientRequest request = handler.result();

            request.end(data.encode());

            request.response().onComplete(response -> {
                response.result().bodyHandler(body -> handleBody(listener, body));
            });
        });
    }
}
