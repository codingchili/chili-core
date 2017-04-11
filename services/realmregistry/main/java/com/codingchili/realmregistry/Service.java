package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.controller.ClientHandler;
import com.codingchili.realmregistry.controller.RealmHandler;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.Deploy;
import com.codingchili.core.protocol.CoreService;


/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Service implements CoreService {
    private CoreContext core;

    @Override
    public void init(CoreContext context) {
        this.core = context;
    }

    @Override
    public void stop(Future<Void> stop) {
        core.logger().onServerStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        Future<RegistryContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                RegistryContext context = future.result();

                for (int i = 0; i < settings().getHandlers(); i++) {
                    Deploy.service(new RealmHandler(context));
                    Deploy.service(new ClientHandler(context));
                }
                core.logger().onServerStarted(start);
            } else {
                start.fail(future.cause());
            }
        });
        RegistryContext.create(providerFuture, core);
    }
}
