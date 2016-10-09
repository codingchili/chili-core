package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Configuration.Routing;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Routing.Model.ListenerSettings;
import com.codingchili.core.Routing.Model.WireType;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class RestListener extends ClusterVerticle {
    private RouteHandler handler;
    private ListenerSettings listener;
    private RoutingSettings settings;
    private HttpServerOptions options;
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
        Routing.EnableCors(router);
        router.route("/*").handler(this::packet);
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer(options).requestHandler(router::accept).listen(listener.getPort());
    }

    private void packet(RoutingContext context) {
        HttpServerRequest request = context.request();

        handler.process(new RestRouteRequest(context, request, listener));
    }
}
