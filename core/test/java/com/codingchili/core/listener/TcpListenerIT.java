package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import com.codingchili.core.listener.transport.TcpListener;

/**
 * Test cases for TCP transport.
 */
@RunWith(VertxUnitRunner.class)
public class TcpListenerIT extends ListenerTestCases {

    public TcpListenerIT() {
        super(TcpListener::new);
    }

    @Override
    public Future<JsonObject> sendRequest(JsonObject data) {
        NetClient client = keep(context.vertx().createNetClient(), NetClient::close);

        return client.connect(port, HOST).compose(socket -> {
            Promise<JsonObject> response = Promise.promise();
            Buffer received = Buffer.buffer();

            // the response is not framed and TCP may split it: read until the bytes are a whole json object.
            socket.handler(buffer -> {
                received.appendBuffer(buffer);
                try {
                    response.tryComplete(received.toJsonObject());
                } catch (DecodeException incomplete) {
                    // wait for the rest of the response.
                }
            });
            socket.closeHandler(closed -> response.tryFail(CLOSED_BEFORE_RESPONSE));
            socket.exceptionHandler(response::tryFail);
            socket.write(data.encode()).onFailure(response::tryFail);
            return response.future();
        });
    }
}
