package com.codingchili.patching;

import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.controller.PatchHandler;
import io.vertx.core.*;

import com.codingchili.core.context.Deploy;
import com.codingchili.core.protocol.ClusterNode;

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
