package com.codingchili.authentication;

import com.codingchili.authentication.controller.ClientHandler;
import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.context.Deploy;

import com.codingchili.authentication.configuration.AuthenticationContext;

/**
 * @author Robin Duda
 *         Starts up the client handler and the realmName handler.
 */
public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        Future<AuthenticationContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                AuthenticationContext context = future.result();

                for (int i = 0; i < settings.getHandlers(); i++) {
                    Deploy.service(new ClientHandler<>(context));
                }

                start.complete();
            } else {
                start.fail(future.cause());
            }
        });
        AuthenticationContext.create(providerFuture, vertx);
    }
}
