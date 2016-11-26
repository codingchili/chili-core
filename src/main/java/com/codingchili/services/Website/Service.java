package com.codingchili.services.website;

import io.vertx.core.*;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.context.Deploy;

import com.codingchili.services.website.configuration.WebserverContext;
import com.codingchili.services.website.controller.WebHandler;

/**
 * @author Robin Duda
 */
public class Service extends ClusterNode {
    private WebserverContext context;

    public Service() {}

    public Service(WebserverContext context) {
        this.context = context;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (this.context == null) {
            this.context = new WebserverContext(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (int i = 0; i < settings.getHandlers(); i++) {
            Deploy.service(new WebHandler<>(context));
        }
        start.complete();
    }
}
