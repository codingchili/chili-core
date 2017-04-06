package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.model.WireType;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         Websocket transport listener.
 */
public class WebsocketListener extends ClusterNode {
    private final RouterHandler<RouterContext> handler;

    public WebsocketListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer().websocketHandler(socket -> {
            socket.handler(data -> handle(socket, data));
        }).listen(listener().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                listener().addListenPort(listen.result().actualPort());
                handler.start(start);
            }
            else {
                start.fail(listen.cause());
            }
        }).requestHandler(handlerino -> {

        });
    }

    private void handle(ServerWebSocket socket, Buffer buffer) {
        WebsocketRequest request = new WebsocketRequest(socket, buffer, listener());

        if (buffer.length() > listener().getMaxRequestBytes()) {
            request.error(new RequestPayloadSizeException());
        }
        else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.WEBSOCKET);
    }
}
