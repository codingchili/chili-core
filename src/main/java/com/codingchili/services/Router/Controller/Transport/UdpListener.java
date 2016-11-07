package com.codingchili.services.Router.Controller.Transport;

import io.vertx.core.Future;

import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Router.Configuration.ListenerSettings;
import com.codingchili.services.Router.Controller.RouterHandler;

/**
 * @author Robin Duda
 */
public class UdpListener extends ClusterNode {

    public UdpListener(RouterHandler handler, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}
