package com.codingchili.patching;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.controller.PatchHandler;
import io.vertx.core.Future;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * website and resource server.
 */
public class Service implements CoreService {
    private PatchContext context;

    @Override
    public void init(CoreContext core) {
        this.context = new PatchContext(core);
    }

    @Override
    public void start(Future<Void> start) {
        context.handler(() -> new PatchHandler(context)).setHandler(untyped(start));
    }
}
