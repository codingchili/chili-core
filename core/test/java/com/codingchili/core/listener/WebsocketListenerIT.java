package com.codingchili.core.listener;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.WebsocketListener;
import com.codingchili.core.testing.ListenerTestCases;
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
public class WebsocketListenerIT extends ListenerTestCases {

    public WebsocketListenerIT() {
        super(WireType.WEBSOCKET, WebsocketListener::new);
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        context.vertx().createHttpClient().websocket(port, HOST, CoreStrings.DIR_SEPARATOR, handler -> {
            handler.handler(body -> handleBody(listener, body));
            handler.write(Buffer.buffer(data.encode()));
        });
    }
}
