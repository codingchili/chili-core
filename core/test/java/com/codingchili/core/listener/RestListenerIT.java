package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
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
        super(RestListener::new);
    }

    @Test
    public void testListenerSupportsGet(TestContext test) {
        sendGetRequest(String.format("/?%s=%s&%s=%s", PROTOCOL_ROUTE, ID_PING, PROTOCOL_TARGET, NODE_ROUTER))
                .onComplete(test.asyncAssertSuccess(response -> test.assertEquals(ACCEPTED, status(response))));
    }

    private HttpClient client() {
        return keep(context.vertx().createHttpClient(), HttpClient::close);
    }

    private Future<JsonObject> sendGetRequest(String action) {
        return client().request(HttpMethod.GET, port, HOST, action)
                .compose(HttpClientRequest::send)
                .compose(HttpClientResponse::body)
                .map(Buffer::toJsonObject);
    }

    @Override
    public Future<JsonObject> sendRequest(JsonObject data) {
        String target = data.getString(PROTOCOL_ROUTE);

        if (target == null) {
            target = "";
        } else {
            if (!target.startsWith(CoreStrings.DIR_ROOT)) {
                target = CoreStrings.DIR_ROOT + target;
            }
            data.remove(PROTOCOL_ROUTE);
        }

        return client().request(HttpMethod.POST, port, HOST, target)
                .compose(request -> request.send(data.encode()))
                .compose(HttpClientResponse::body)
                .map(Buffer::toJsonObject);
    }
}
