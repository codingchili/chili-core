package com.codingchili.realmregistry;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.controller.ClientHandler;
import com.codingchili.realmregistry.controller.RealmHandler;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import static com.codingchili.core.context.FutureHelper.generic;


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
    public void start(Future<Void> start) {
        Future<RegistryContext> providerFuture = Future.future();

        providerFuture.setHandler(future -> {
            if (future.succeeded()) {
                context = future.result();

                CompositeFuture.all(
                        context.handler(() -> new RealmHandler(context)),
                        context.handler(() -> new ClientHandler(context))
                ).setHandler(generic(start));
            } else {
                start.fail(future.cause());
            }
        });
        RegistryContext.create(providerFuture, core);
    }
}
