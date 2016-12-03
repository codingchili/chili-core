package com.codingchili.router.controller.transport;

import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.controller.RouterHandler;

/**
 * @author Robin Duda
 */
public class TcpListener extends ClusterNode {
    public TcpListener(RouterHandler handler, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}
