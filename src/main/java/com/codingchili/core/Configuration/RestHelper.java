package com.codingchili.core.Configuration;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 */
public abstract class RestHelper {

    public static void EnableCors(Router router) {
        router.options("/*").handler(routing -> {
            setHeaders(routing);
            routing.response().setStatusCode(HttpResponseStatus.OK.code()).end();
        });

        router.route("/*").handler(routing -> {
            setHeaders(routing);
            routing.next();
        });
    }

    private static void setHeaders(RoutingContext context) {
        context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Files, X-Requested-With");
    }

}
