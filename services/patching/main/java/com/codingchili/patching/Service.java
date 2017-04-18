package com.codingchili.patching;

import com.codingchili.core.context.*;
import com.codingchili.core.listener.*;
import com.codingchili.patching.configuration.*;
import com.codingchili.patching.controller.*;

import io.vertx.core.*;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Service implements CoreService {
    private PatchContext context;

    @Override
    public void init(CoreContext core) {
        this.context = new PatchContext(core);
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < context.system().getHandlers(); i++) {
            context.handler(new PatchHandler(context), (done) -> {
            });
        }
        context.logger().onServiceStarted(start);
    }
}
