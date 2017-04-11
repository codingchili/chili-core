package com.codingchili.website;

import com.codingchili.core.context.*;
import com.codingchili.core.protocol.*;
import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.controller.WebHandler;
import io.vertx.core.*;

/**
 * @author Robin Duda
 */
public class Service implements CoreService {
    private WebserverContext context;

    public Service() {}

    public Service(WebserverContext context) {
        this.context = context;
    }

    @Override
    public void init(CoreContext core) {
        if (this.context == null) {
            this.context = new WebserverContext(core);
        }
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServerStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < settings().getHandlers(); i++) {
            Deploy.service(new WebHandler(context));
        }
        context.logger().onServerStarted(start);
    }
}
