package com.codingchili.services.Social;

import io.vertx.core.Future;

import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Social.Configuration.SocialContext;
import com.codingchili.services.Social.Controller.SocialHandler;

public class Server extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        SocialContext context = new SocialContext(vertx);

        for (int i = 0; i < settings.getHandlers(); i++) {
            context.deploy(new SocialHandler<>(context));
        }

        start.complete();
    }
}