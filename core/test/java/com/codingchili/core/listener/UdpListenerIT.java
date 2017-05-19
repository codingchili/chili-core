package com.codingchili.core.listener;

import org.junit.runner.RunWith;

import com.codingchili.core.listener.transport.UdpListener;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;


/**
 * @author Robin Duda
 *         <p>
 *         Test cases for UDP transport.
 */
@RunWith(VertxUnitRunner.class)
public class UdpListenerIT extends TransportTestCases {

    public UdpListenerIT() {
        super(WireType.UDP, UdpListener::new);
    }

    @Override
    void sendRequest(ResponseListener listener, JsonObject data) {
        vertx.createDatagramSocket().send(data.encode(), port, HOST, handler -> {
            if (handler.succeeded()) {
                handler.result().handler(response -> handleBody(listener, response.data()));
            }
        });
    }
}
