package com.codingchili.services.Patching;

import io.vertx.core.*;

import com.codingchili.core.Protocol.ClusterNode;
import com.codingchili.core.Context.Deploy;

import com.codingchili.services.Patching.Configuration.PatchContext;
import com.codingchili.services.Patching.Controller.PatchHandler;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Service extends ClusterNode {
    private PatchContext context;

    public Service() {
    }

    public Service(PatchContext context) {
        this.context = context;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (this.context == null) {
            this.context = new PatchContext(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < settings.getHandlers(); i++) {
            Deploy.service(new PatchHandler<>(context));
        }

        start.complete();
    }
}
