package com.codingchili.core.listener;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.WebsocketListener;

/**
 * Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class WebsocketListenerIT extends ListenerTestCases {

    public WebsocketListenerIT() {
        super(WireType.WEBSOCKET, WebsocketListener::new);
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        context.vertx().createHttpClient().webSocket(port, HOST, CoreStrings.DIR_SEPARATOR, handler -> {
            if (handler.succeeded()) {
                WebSocket webSocket = handler.result();
                webSocket.handler(body -> handleBody(listener, body));
                webSocket.write(Buffer.buffer(data.encode()));
            } else {
                throw new RuntimeException(handler.cause());
            }
        });
    }
}
