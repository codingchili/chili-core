package com.codingchili.services.Router.Controller.Transport;

import io.vertx.core.Future;

import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Router.Configuration.ListenerSettings;
import com.codingchili.services.Router.Controller.RouterHandler;

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
