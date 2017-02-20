package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.model.WireType;
import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

/**
 * @author Robin Duda
 *
 * TCP listener implementation.
 */
public class TcpListener extends ClusterNode {
    private RouterHandler<RouterContext> handler;

    public TcpListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {

        vertx.createNetServer().connectHandler(handler -> {

            handler.handler(data -> {
               packet(handler, data);
            });

        }).listen(listener().getPort());

        handler.start(start);
    }

    private void packet(NetSocket socket, Buffer data) {
        TcpRequest request = new TcpRequest(socket, data, listener());

        if (data.length() > listener().getMaxRequestBytes()) {
            request.error(new RequestPayloadSizeException());
        } else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.TCP);
    }
}
