package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.controller.ClientHandler;
import com.codingchili.realmregistry.controller.RealmHandler;
import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.context.Deploy;


/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        Future<RegistryContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                RegistryContext context = future.result();

                for (int i = 0; i < settings.getHandlers(); i++) {
                    Deploy.service(new RealmHandler<>(context));
                    Deploy.service(new ClientHandler<>(context));
                }

                start.complete();
            } else {
                start.fail(future.cause());
            }
        });
        RegistryContext.create(providerFuture, vertx);
    }
}
