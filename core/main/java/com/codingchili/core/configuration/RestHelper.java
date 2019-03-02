package com.codingchili.core.configuration;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

/**
 * @author Robin Duda
 * <p>
 * Utility class to set CORS headers for HTTP routing.
 */
public abstract class RestHelper {
    private static final CharSequence HEADER_HSTS = HttpHeaders.createOptimized("Strict-Transport-Security");
    private static final CharSequence HEADER_DOWNLOAD_OPTIONS = HttpHeaders.createOptimized("X-Download-Options");
    private static final CharSequence HEADER_XSS_PROTECTION = HttpHeaders.createOptimized("X-XSS-Protection");
    private static final CharSequence HEADER_X_FRAME = HttpHeaders.createOptimized("X-FRAME-OPTIONS");

    private static final CharSequence CORS_ORIGIN = HttpHeaders.createOptimized("*");
    private static final CharSequence CORS_AGE = HttpHeaders.createOptimized("1728000");
    private static final CharSequence CORS_METHODS = HttpHeaders.createOptimized("*");
    private static final CharSequence CORS_ALLOWED_HEADERS = HttpHeaders.createOptimized("*");

    private static final CharSequence STORE_CONTROL = HttpHeaders.createOptimized("no-store, no-cache");
    private static final CharSequence HSTS_AGE = HttpHeaders.createOptimized("max-age=" + 15768000);
    private static final CharSequence DOWNLOAD_OPTIONS = HttpHeaders.createOptimized("noopen");
    private static final CharSequence XSS_PROTECTION = HttpHeaders.createOptimized("1; mode=block");
    private static final CharSequence X_FRAME = HttpHeaders.createOptimized("DENY");

    /**
     * Enables cross origin resource sharing - useful for API's.
     * Enables security headers - useful for security resons.
     *
     * @param router the router to set headers for all requests.
     * @param secure indiciates if security headers should be set.
     */
    public static void addHeaders(Router router, boolean secure) {
        Consumer<RoutingContext> headers = (routing) -> {
            addCORSHeaders(routing);
            if (secure) {
                addSecurityHeaders(routing);
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
        context.response().headers()
                .add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ORIGIN)
                .add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, CORS_METHODS)
                .add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, CORS_ALLOWED_HEADERS)
                .add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, CORS_AGE);
    }

    /**
     * Adds STS, Cache control, frame denial, IE XSS protection.
     *
     * @param context the context to add headers to.
     */
    private static void addSecurityHeaders(RoutingContext context) {
        context.response().headers()
                .add(HttpHeaders.CACHE_CONTROL, STORE_CONTROL)
                //.putHeader("X-Content-Type-Options", "nosniff")
                .add(HEADER_HSTS, HSTS_AGE)
                .add(HEADER_DOWNLOAD_OPTIONS, DOWNLOAD_OPTIONS)
                .add(HEADER_XSS_PROTECTION, XSS_PROTECTION)
                .add(HEADER_X_FRAME, X_FRAME);
    }
}
