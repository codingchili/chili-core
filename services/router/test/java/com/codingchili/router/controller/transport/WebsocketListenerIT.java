package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import static com.codingchili.common.Strings.DIR_SEPARATOR;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;

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

    @Override
    void sendRequest(String route, ResponseListener listener, JsonObject data) {
        if (!data.containsKey(PROTOCOL_ROUTE)) {
            data.put(PROTOCOL_ROUTE, route);
        }

        vertx.createHttpClient().websocket(port, HOST, DIR_SEPARATOR, handler -> {
            handler.handler(body -> handleBody(listener, body));
            handler.write(Buffer.buffer(data.encode()));
        });
    }
}
