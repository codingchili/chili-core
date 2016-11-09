package com.codingchili.services.Authentication;

import io.vertx.core.Future;

import com.codingchili.core.Protocol.ClusterNode;
import com.codingchili.core.Context.Deploy;

import com.codingchili.services.Authentication.Configuration.AuthContext;
import com.codingchili.services.Authentication.Controller.AuthenticationHandler;
import com.codingchili.services.Authentication.Controller.ClientHandler;

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
