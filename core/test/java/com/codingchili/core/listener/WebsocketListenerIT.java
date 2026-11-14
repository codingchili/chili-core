package com.codingchili.core.listener;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.transport.WebsocketListener;

/**
 * Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class WebsocketListenerIT extends ListenerTestCases {

    public WebsocketListenerIT() {
        super(WebsocketListener::new);
    }

    @Test
    public void testServerCloses(TestContext test) {
        Async async = test.async();
        context.vertx().createWebSocketClient().connect(port, HOST, CoreStrings.DIR_SEPARATOR).onComplete(test.asyncAssertSuccess(socket -> {
            socket.closeHandler(closed -> async.complete());
            socket.write(Buffer.buffer(new JsonObject()
                    .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                    .put(CoreStrings.PROTOCOL_ROUTE, ROUTE_CLOSE).encode()));
        }));
    }

    @Test
    public void malformedMessagesAreAnsweredAndTheConnectionStays(TestContext test) {
        // not json and json nested beyond the parser's limit: answered BAD, the next message is handled.
        Async async = test.async();
        var client = context.vertx().createWebSocketClient();
        var answers = new java.util.ArrayList<JsonObject>();
        client.connect(port, HOST, CoreStrings.DIR_SEPARATOR).onComplete(test.asyncAssertSuccess(socket -> {
            socket.handler(body -> {
                answers.add(body.toJsonObject());
                if (answers.size() == 3) {
                    test.assertEquals("BAD", answers.get(0).getString(CoreStrings.PROTOCOL_STATUS));
                    test.assertEquals("BAD", answers.get(1).getString(CoreStrings.PROTOCOL_STATUS));
                    test.assertEquals("ACCEPTED", answers.get(2).getString(CoreStrings.PROTOCOL_STATUS));
                    test.assertNotNull(client, "kept for the length of the test");
                    async.complete();
                }
            });
            socket.write(Buffer.buffer("not json"));
            socket.write(Buffer.buffer("{\"a\":".repeat(2000) + "1" + "}".repeat(2000)));
            socket.write(Buffer.buffer(new JsonObject().put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                    .put(CoreStrings.PROTOCOL_ROUTE, "hello").encode()));
        }));
    }

    @Test
    public void theLimitOfAConnectionIsCheckedBeforeDecoding(TestContext test) {
        // the handler lowers the limit of its connection to 64 bytes: a larger message is answered BAD by the listener,
        // a smaller one still reaches the handler.
        Async async = test.async();
        var client = context.vertx().createWebSocketClient();
        var answers = new java.util.ArrayList<JsonObject>();
        client.connect(port, HOST, CoreStrings.DIR_SEPARATOR).onComplete(test.asyncAssertSuccess(socket -> {
            socket.handler(body -> {
                answers.add(body.toJsonObject());
                if (answers.size() == 1) {
                    test.assertEquals("ACCEPTED", answers.get(0).getString(CoreStrings.PROTOCOL_STATUS));
                    socket.write(Buffer.buffer(new JsonObject().put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                            .put(CoreStrings.PROTOCOL_ROUTE, "hello").put("padding", "x".repeat(LIMIT_BYTES)).encode()));
                    socket.write(Buffer.buffer(new JsonObject().put(CoreStrings.PROTOCOL_ROUTE, "hi").encode()));
                } else if (answers.size() == 3) {
                    test.assertEquals("BAD", answers.get(1).getString(CoreStrings.PROTOCOL_STATUS));
                    test.assertEquals("ACCEPTED", answers.get(2).getString(CoreStrings.PROTOCOL_STATUS));
                    test.assertNotNull(client, "kept for the length of the test");
                    async.complete();
                }
            });
            socket.write(Buffer.buffer(new JsonObject().put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                    .put(CoreStrings.PROTOCOL_ROUTE, ROUTE_LIMIT).encode()));
        }));
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        context.vertx().createWebSocketClient().connect(port, HOST, CoreStrings.DIR_SEPARATOR).onComplete(handler -> {
            if (handler.succeeded()) {
                WebSocket webSocket = handler.result();
                webSocket.handler(body -> handleBody(listener, body));
                webSocket.write(Buffer.buffer(data.encode()));
            } else {
                throw new RuntimeException(handler.cause());
            }
        });
    }
}
