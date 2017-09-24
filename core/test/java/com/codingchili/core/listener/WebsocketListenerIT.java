package com.codingchili.core.listener;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.WebsocketListener;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class WebsocketListenerIT extends TransportTestCases {

    public WebsocketListenerIT() {
        super(WireType.WEBSOCKET, WebsocketListener::new);
    }

    @Override
    void sendRequest(ResponseListener listener, JsonObject data) {
        vertx.createHttpClient().websocket(port, HOST, CoreStrings.DIR_SEPARATOR, handler -> {
            handler.handler(body -> handleBody(listener, body));
            handler.write(Buffer.buffer(data.encode()));
        });
    }
}
