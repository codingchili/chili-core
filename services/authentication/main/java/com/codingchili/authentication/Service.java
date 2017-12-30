package com.codingchili.authentication;

import com.codingchili.authentication.configuration.AuthenticationContext;
import com.codingchili.authentication.controller.ClientHandler;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import io.vertx.core.Future;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * Starts up the client handler and the realm handler.
 */
public class Service implements CoreService {
    private CoreContext core;
    private AuthenticationContext context;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future<Void> start) {
        Future<AuthenticationContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                context = future.result();
                context.handler(() -> new ClientHandler(context)).setHandler(untyped(start));
            } else {
                start.fail(future.cause());
            }
        });
        AuthenticationContext.create(providerFuture, core);
    }
}
