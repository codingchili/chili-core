package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Configuration.RestHelper;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Routing.Configuration.ListenerSettings;
import com.codingchili.core.Routing.Model.WireType;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 */
public class RestListener extends ClusterVerticle {
    private final RouteHandler handler;
    private final ListenerSettings listener;
    private final RoutingSettings settings;
    private final HttpServerOptions options;
    private Router router;

    public RestListener(RouteHandler handler, RoutingSettings settings) {
        this.handler = handler;
        this.settings = settings;
        this.listener = settings.getListener(WireType.REST);

        options = new HttpServerOptions().setCompressionSupported(false);
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        logger = new DefaultLogger(vertx, settings.getLogserver());
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        RestHelper.EnableCors(router);
        router.route("/*").handler(this::packet);
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer(options).requestHandler(router::accept).listen(listener.getPort());
        handler.start(start);
    }

    private void packet(RoutingContext context) {
        RestRouteRequest request = new RestRouteRequest(context, context.request(), listener);

        if (context.getBody().length() > listener.getMaxRequestBytes()) {
            request.bad();
        } else {
            handler.process(request);
        }
    }
}
