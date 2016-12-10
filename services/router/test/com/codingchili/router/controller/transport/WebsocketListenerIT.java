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
public class WebsocketListenerIT extends TransportTestCases {

    public WebsocketListenerIT() {
        super(WireType.WEBSOCKET);
    }

    void sendRequest(String route, ResponseListener listener) {
        sendRequest(route, listener, new JsonObject());
    }

    void sendRequest(String route, ResponseListener listener, JsonObject data) {
        vertx.createHttpClient().websocket(PORT, HOST, route, handler -> {
            handler.handler(body -> {
                handleBody(listener, body);
            });
        });
    }
}
