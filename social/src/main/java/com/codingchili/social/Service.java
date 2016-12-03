package com.codingchili.social;

import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;

import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.controller.SocialHandler;

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