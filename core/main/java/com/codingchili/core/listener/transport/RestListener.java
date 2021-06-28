package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * HTTP/REST transport listener.
 */
public class RestListener implements CoreListener {
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private final Promise<Router> onRouter = Promise.promise();
    private Logger logger;
    private CoreContext core;
    private CoreHandler handler;
    private Router router;

    @Override
    public void init(CoreContext core) {
        logger = ListenerExceptionLogger.create(core, this, handler);
        router = Router.router(core.vertx());
        RestHelper.addHeaders(router, settings.isSecure());

        router.route()
                .handler(
                        BodyHandler.create()
                                .setBodyLimit(settings.getMaxRequestBytes())
                                .setDeleteUploadedFilesOnEnd(true)
                                .setHandleFileUploads(false)
                ).handler(this::packet);

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
    public void start(Promise<Void> start) {
        core.vertx().createHttpServer(settings.getHttpOptions())
                .requestHandler(router)
                .exceptionHandler(logger::onError)
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
    public void stop(Promise<Void> stop) {
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