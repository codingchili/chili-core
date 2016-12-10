package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.model.WireType;
import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

/**
 * @author Robin Duda
 *
 * Websocket transport listener.
 */
public class WebsocketListener extends ClusterNode {
    private final RouterHandler<RouterContext> handler;

    public WebsocketListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(data -> {
                handle(socket, data);
            });

        }).listen(listener().getPort());

        handler.start(start);
    }

    private void handle(ServerWebSocket socket, Buffer buffer) {
        WebsocketRequest request = new WebsocketRequest(socket, buffer, listener());

        if (buffer.length() > listener().getMaxRequestBytes()) {
            request.bad(new RequestPayloadSizeException());
        } else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.WEBSOCKET);
    }
}
