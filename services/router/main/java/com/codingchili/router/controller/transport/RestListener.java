package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.model.WireType;
import io.vertx.core.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

/**
 * @author Robin Duda
 *
 * HTTP/REST transport listener.
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
        System.out.println("Started listening on port " + listener().getPort());
        vertx.createHttpServer(listener().getHttpOptions())
                .requestHandler(router::accept).listen(listener().getPort());

        handler.start(start);
    }

    private void packet(RoutingContext context) {
        RestRequest request = new RestRequest(context, context.request(), listener());

        if (context.getBody().length() > listener().getMaxRequestBytes()) {
            request.error(new RequestPayloadSizeException());
        } else {
            handler.process(request);
        }
    }

    private ListenerSettings listener() {
        return handler.context().getListener(WireType.REST);
    }
}
