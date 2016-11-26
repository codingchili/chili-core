package com.codingchili.services.Social;

import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;

import com.codingchili.services.Social.configuration.SocialContext;
import com.codingchili.services.Social.controller.SocialHandler;

public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        SocialContext context = new SocialContext(vertx);

        for (int i = 0; i < settings.getHandlers(); i++) {
            context.deploy(new SocialHandler<>(context));
        }

        start.complete();
    }
}