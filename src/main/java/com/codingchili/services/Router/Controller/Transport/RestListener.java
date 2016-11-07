package com.codingchili.services.Router.Controller.Transport;

import io.vertx.core.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import com.codingchili.core.Configuration.RestHelper;
import com.codingchili.core.Exception.RequestPayloadSizeException;
import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Router.Configuration.ListenerSettings;
import com.codingchili.services.Router.Configuration.RouterContext;
import com.codingchili.services.Router.Controller.RouterHandler;
import com.codingchili.services.Router.Model.WireType;

/**
 * @author Robin Duda
 */
public class RestListener extends ClusterNode {
    private final RouterHandler<RouterContext> handler;
    private Router router;

    public RestListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        RestHelper.EnableCors(router);
        router.route("/*").handler(this::packet);
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer(listener().getHttpOptions())
                .requestHandler(router::accept).listen(listener().getPort());

        handler.start(start);
    }

    private void packet(RoutingContext context) {
        RestRouteRequest request = new RestRouteRequest(context, context.request(), listener());

        if (context.getBody().length() > listener().getMaxRequestBytes()) {
            request.bad(new RequestPayloadSizeException());
        } else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.REST);
    }
}
