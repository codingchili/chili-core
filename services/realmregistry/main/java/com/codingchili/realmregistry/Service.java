package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.controller.ClientHandler;
import com.codingchili.realmregistry.controller.RealmHandler;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;


/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Service implements CoreService {
    private RegistryContext context;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        Future<RegistryContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                context = future.result();

                for (int i = 0; i < Configurations.system().getHandlers(); i++) {
                    context.handler(new RealmHandler(context), done -> {});
                    context.handler(new ClientHandler(context), done -> {});
                }
                context.logger().onServiceStarted(start);
            } else {
                start.fail(future.cause());
            }
        });
        RegistryContext.create(providerFuture, core);
    }
}
