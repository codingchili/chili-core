package com.codingchili.core.listener.transport;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static com.codingchili.core.configuration.CoreStrings.LOG_AT;
import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * HTTP/REST transport listener.
 */
public class RestListener implements CoreListener {
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private final Promise<Router> onRouter = Promise.promise();
    private CoreContext core;
    private CoreHandler handler;
    private Router router;

    @Override
    public void init(CoreContext core) {
        router = Router.router(core.vertx());
        RestHelper.addHeaders(router, settings.isSecure());

        router.route()
                .handler(BodyHandler.create().setBodyLimit(settings.getMaxRequestBytes()))
                .handler(this::packet);

        handler.init(core);
        this.core = core;

        // invoke the callback when router is created from the context.
        onRouter.complete(router);
    }

    /**
     * @return exposes the router object for further configuration, manual route setups, fallbacks
     * and support for adding a static handler.
     */
    public Future<Router> router() {
        return onRouter.future();
    }

    @Override
    public CoreListener settings(ListenerSettings settings) {
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
        core.vertx().createHttpServer(settings.getHttpOptions())
                .requestHandler(router)
                .listen(settings.getPort(), getBindAddress(), listen -> {
                    if (listen.succeeded()) {
                        settings.addListenPort(listen.result().actualPort());
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
        handler.handle(new RestRequest(context, settings));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}