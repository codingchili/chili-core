package com.codingchili.patching;

import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.controller.PatchHandler;
import io.vertx.core.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Service implements CoreService{
    private PatchContext context;

    @Override
    public void init(CoreContext core) {
        if (this.context == null) {
            this.context = new PatchContext(core);
        }
    }

    @Override
    public void stop(Future<Void> stop) {
        stop.complete();
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < context.system().getHandlers(); i++) {
            context.handler(new PatchHandler(context), (done) -> {});
        }
        start.complete();
    }
}
