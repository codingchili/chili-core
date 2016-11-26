package com.codingchili.services.authentication;

import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.context.Deploy;

import com.codingchili.services.authentication.configuration.AuthContext;
import com.codingchili.services.authentication.controller.AuthenticationHandler;
import com.codingchili.services.authentication.controller.ClientHandler;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        Future<AuthContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                AuthContext context = future.result();

                for (int i = 0; i < settings.getHandlers(); i++) {
                    Deploy.service(new AuthenticationHandler<>(context));
                    Deploy.service(new ClientHandler<>(context));
                }

                start.complete();
            } else {
                start.fail(future.cause());
            }
        });
        AuthContext.create(providerFuture, vertx);
    }
}
