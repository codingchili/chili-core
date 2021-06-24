package com.codingchili.core.listener;

import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import com.codingchili.core.listener.transport.UdpListener;

/**
 * Test cases for UDP transport.
 */
@RunWith(VertxUnitRunner.class)
public class UdpListenerIT extends ListenerTestCases {

    public UdpListenerIT() {
        super(WireType.UDP, UdpListener::new);
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        DatagramSocket socket = context.vertx().createDatagramSocket();

        socket.handler(packet -> handleBody(listener, packet.data()));
        socket.send(data.encode(), port, HOST);
    }
}
