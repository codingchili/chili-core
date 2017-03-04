package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Ignore;
import org.junit.runner.RunWith;


/**
 * @author Robin Duda
 *
 * Test cases for UDP transport.
 */
@Ignore("Disable for travis.")
@RunWith(VertxUnitRunner.class)
public class UdpListenerIT extends TransportTestCases {

    public UdpListenerIT() {
        super(WireType.UDP);
    }

    @Override
    void sendRequest(String route, ResponseListener listener, JsonObject data) {
        vertx.createDatagramSocket().send(data.encode(), PORT, HOST, handler -> {
            if (handler.succeeded()) {
                handler.result().handler(response -> handleBody(listener, response.data()));
            }
        });
    }
}
