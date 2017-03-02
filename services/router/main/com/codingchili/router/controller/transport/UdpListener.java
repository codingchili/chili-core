package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.model.WireType;
import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramPacket;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

/**
 * @author Robin Duda
 *
 * UDP transport listener.
 */
public class UdpListener extends ClusterNode {
    private static final String INTERFACE_IPv4 = "0.0.0.0";
    private final RouterHandler<RouterContext> handler;

    public UdpListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createDatagramSocket().listen(listener().getPort(), INTERFACE_IPv4, listen -> {
            if (listen.succeeded()) {
                listen.result().handler(this::handle);
            }
        });

        handler.start(start);
    }

    private void handle(DatagramPacket connection) {
        UdpRequest request = new UdpRequest(handler.context(), connection);

        if (connection.data().length() > listener().getMaxRequestBytes()) {
            request.error(new RequestPayloadSizeException());
        } else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.UDP);
    }
}
