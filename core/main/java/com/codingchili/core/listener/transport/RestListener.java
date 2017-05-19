package com.codingchili.core.listener.transport;

import java.util.function.Supplier;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.RequestProcessor;

import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         HTTP/REST transport listener.
 */
public class RestListener implements CoreListener {
    private CoreContext core;
    private CoreHandler handler;
    private Supplier<ListenerSettings> listener;
    private Router router;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());
        RestHelper.EnableCors(router);
        router.route().handler(this::packet);
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.listener = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Future<Void> start) {
        core.vertx().createHttpServer(listener.get().getHttpOptions())
                .requestHandler(router::accept)
                .listen(listener.get().getPort(), getBindAddress(), listen -> {
                    if (listen.succeeded()) {
                        listener.get().addListenPort(listen.result().actualPort());
                        handler.start(start);
                    } else {
                        start.fail(listen.cause());
                    }
                });
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    private void packet(RoutingContext context) {
        RequestProcessor.accept(core, handler, new RestRequest(context, listener.get(), context.request()));
    }
}