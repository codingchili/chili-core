package com.codingchili.core.configuration;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * @author Robin Duda
 * <p>
 * Utility class to set CORS headers for HTTP routing.
 */
public abstract class RestHelper {

    /**
     * Enables cross origin resource sharing - useful for API's.
     * Enables security headers - useful for security resons.
     *
     * @param router the router to set headers for all requests.
     * @param secure indiciates if security headers should be set.
     */
    public static void addHeaders(Router router, boolean secure) {
        Consumer<RoutingContext> headers = (context) -> {
            addCORSHeaders(context);
            if (secure) {
                addSecurityHeaders(context);
            }
        };

        router.options("/*").handler(routing -> {
            headers.accept(routing);
            routing.response().setStatusCode(HttpResponseStatus.OK.code()).end();
        });

        router.route("/*").handler(routing -> {
            headers.accept(routing);
            routing.next();
        });
    }

    /**
     * Adds CORS headers to allow requests from other origins.
     *
     * @param context the context to add headers to.
     */
    private static void addCORSHeaders(RoutingContext context) {
        context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, files, X-Requested-With")
                .putHeader("Access-Control-Max-Age", "1728000");
    }

    /**
     * Adds STS, Cache control, frame denial, IE XSS protection.
     *
     * @param context the context to add headers to.
     */
    private static void addSecurityHeaders(RoutingContext context) {
        context.response()
                .putHeader("Cache-Control", "no-store, no-cache")
                //.putHeader("X-Content-Type-Options", "nosniff")
                .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
                .putHeader("X-Download-Options", "noopen")
                .putHeader("X-XSS-Protection", "1; mode=block")
                .putHeader("X-FRAME-OPTIONS", "DENY");
    }
}
