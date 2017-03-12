package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.model.WireType;
import io.vertx.core.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         HTTP/REST transport listener.
 */
public class RestListener extends ClusterNode {
    private static final Map<Integer, Router> routers = new HashMap<>();
    private final RouterHandler<RouterContext> handler;
    private Router router;

    public RestListener(RouterHandler<RouterContext> handler) {
        this.handler = handler;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        synchronized (routers) {
            if (routers.containsKey(listener().getPort())) {
                router = routers.get(listener().getPort());
            }
            else {
                router = Router.router(vertx);
                router.route().handler(BodyHandler.create());
                RestHelper.EnableCors(router);
                router.route().handler(this::packet);
                routers.put(listener().getPort(), router);
            }
        }
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer(listener().getHttpOptions())
                .requestHandler(router::accept)
                .listen(listener().getPort(), getBindAddress(), listen -> {
                    if (listen.succeeded()) {
                        handler.start(start);
                    } else {
                        start.fail(listen.cause());
                    }
                });
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