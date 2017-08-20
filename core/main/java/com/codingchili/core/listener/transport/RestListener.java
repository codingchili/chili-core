package com.codingchili.core.listener.transport;

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

import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.LOG_AT;
import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         HTTP/REST transport listener.
 */
public class RestListener implements CoreListener {
    private CoreContext core;
    private CoreHandler handler;
    private Supplier<ListenerSettings> settings;
    private Router router;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());
        RestHelper.EnableCors(router);
        router.route().handler(this::packet);
        handler.init(core);
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Future<Void> start) {
        core.vertx().createHttpServer(settings.get().getHttpOptions())
                .requestHandler(router::accept)
                .listen(settings.get().getPort(), getBindAddress(), listen -> {
                    if (listen.succeeded()) {
                        settings.get().addListenPort(listen.result().actualPort());
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
        RequestProcessor.accept(core, handler, new RestRequest(context, settings.get(), context.request()));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.get().getPort();
    }
}