package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;

/**
 * @author Robin Duda
 *
 * Test cases for UDP transport.
 */
@RunWith(VertxUnitRunner.class)
public class UdpListenerIT extends TransportTestCases {

    public UdpListenerIT() {
        super(WireType.UDP);
    }

    @Override
    void sendRequest(String route, ResponseListener listener) {
        sendRequest(route, listener, new JsonObject().put(PROTOCOL_ROUTE, route));
    }

    @Override
    void sendRequest(String route, ResponseListener listener, JsonObject data) {
        vertx.createNetClient().connect(PORT, HOST, connect -> {
            if (connect.succeeded()) {
                NetSocket socket = connect.result();

                socket.handler(buffer -> handleBody(listener, buffer));
                socket.write(data.encode());
            }
        });
    }
}
