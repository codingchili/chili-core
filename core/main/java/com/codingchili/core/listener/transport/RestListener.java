package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Objects;
import java.util.function.Supplier;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * HTTP/REST transport listener.
 */
public class RestListener implements CoreListener {
    private Supplier<ListenerSettings> settings = ListenerSettings::getDefaultSettings;
    private String path;
    private String regex;
    private CoreContext core;
    private CoreHandler handler;
    private Router router;


    @Override
    public void init(CoreContext core) {
        router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());
        RestHelper.addHeaders(router, settings.get().isSecure());

        // enable routing for static resources.
        if (regex != null && path != null) {
            router.routeWithRegex(regex).handler(StaticHandler.create()
                    //.setCachingEnabled(false) -- set in HttpOptions.
                    .setWebRoot(path));
        }

        router.route().handler(this::packet);
        handler.init(core);
        this.core = core;
    }

    /**
     * @param path  a directory from which to serve static resources.
     * @param regex a regular expression that maps request routes to the static handler.
     * @return fluent.
     */
    public RestListener setResources(String path, String regex) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(regex);
        this.path = path;
        this.regex = regex;
        return this;
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
        handler.handle(new RestRequest(context, settings.get()));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.get().getPort();
    }
}