package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;
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
        super(UdpListener::new);
    }

    @Override
    public Future<JsonObject> sendRequest(JsonObject data) {
        DatagramSocket socket = keep(context.vertx().createDatagramSocket(), DatagramSocket::close);
        Promise<JsonObject> response = Promise.promise();

        // a datagram is a whole response.
        socket.handler(packet -> complete(response, packet.data()));
        socket.exceptionHandler(response::tryFail);
        return socket.send(data.encode(), port, HOST).compose(sent -> response.future());
    }
}
