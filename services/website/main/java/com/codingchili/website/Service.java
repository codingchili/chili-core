package com.codingchili.website;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;

import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.controller.WebHandler;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;

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
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < Configurations.system().getHandlers(); i++) {
            Future<String> future = Future.future();
            context.handler(new WebHandler(context), future);
            futures.add(future);
        }
        CompositeFuture.all(futures).setHandler(done -> {
            if (done.succeeded()) {
                context.logger().onServiceStarted(start);
            } else {
                context.logger().onServiceFailed(done.cause());
            }
        });
    }
}
