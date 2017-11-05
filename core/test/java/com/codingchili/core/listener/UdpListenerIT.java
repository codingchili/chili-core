package com.codingchili.core.listener;

import com.codingchili.core.listener.transport.UdpListener;
import com.codingchili.core.testing.ListenerTestCases;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Ignore;
import org.junit.runner.RunWith;


/**
 * @author Robin Duda
 * <p>
 * Test cases for UDP transport.
 */
@RunWith(VertxUnitRunner.class)
public class UdpListenerIT extends ListenerTestCases {

    public UdpListenerIT() {
        super(WireType.UDP, UdpListener::new);
    }

    @Ignore("DTLS not enabled yet.")
    public void testTLSEnabled(TestContext test) {
        //
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        context.vertx().createDatagramSocket().send(data.encode(), port, HOST, handler -> {
            if (handler.succeeded()) {
                handler.result().handler(response -> handleBody(listener, response.data()));
            }
        });
    }
}
