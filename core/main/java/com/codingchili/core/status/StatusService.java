package com.codingchili.core.status;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Resource;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * A service that reports the status of the application over HTTP, as an API and as a web page.
 * <p>
 * <ul>
 * <li>{@code /health/live}: 200 for as long as the application responds. Use it for liveness probes.</li>
 * <li>{@code /health/ready}: 200 when the application is ready to serve requests, 503 when it is not: when a
 * {@link #check(String, ReadinessCheck) readiness check} fails, or when the context is shutting down.
 * Use it for readiness probes and load balancers.</li>
 * <li>{@code /status}: a JSON report on the application, the JVM, vertx and the metrics, see {@link StatusReport}.</li>
 * <li>{@code /}: a web page that shows the report, and refreshes it.</li>
 * </ul>
 * <p>
 * The service is deployed like any other, it needs a port of its own:
 * <pre>{@code
 * core.service(() -> new StatusService()
 *         .settings(new ListenerSettings().setPort(8081).setSecure(false))
 *         .check("accounts", () -> accounts.size().mapEmpty()));
 * }</pre>
 * <p>
 * <b>The status and web page expose information about the application:</b> deploy the service on an internal
 * port or interface, see {@link #bind(String)}, and use {@link #details(boolean)} to only serve the health endpoints.
 * No CORS headers are sent, browsers on other sites cannot read the responses.
 */
public class StatusService implements CoreService {
    /**
     * The port that is used unless other settings are provided.
     */
    public static final int DEFAULT_PORT = 8081;
    private static final String RESOURCE_PAGE = "/status/status.html";
    private static final String NONCE_PLACEHOLDER = "{{nonce}}";
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String CHECK_SHUTDOWN = "shutdown";
    private final Map<String, ReadinessCheck> checks = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private ListenerSettings settings = new ListenerSettings().setPort(DEFAULT_PORT).setSecure(false);
    private HttpServerConfig http = new HttpServerConfig();
    private String host = getBindAddress();
    private long checkTimeoutMS = 2000;
    private boolean details = true;
    private String page;
    private CoreContext core;
    private Logger logger;
    private HttpServer server;

    /**
     * Creates a status service that listens on {@link #DEFAULT_PORT} without TLS, on all interfaces.
     */
    public StatusService() {
        // when shutting down the application stops being ready first: services run until the shutdown delay is up.
        checks.put(CHECK_SHUTDOWN, () -> (core != null && core.isShuttingDown())
                ? Future.failedFuture("shutting down") : Future.succeededFuture());
    }

    /**
     * @param settings the port and TLS settings to listen with.
     * @return fluent
     */
    public StatusService settings(ListenerSettings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * @param host the address of the interface to listen on, for example 127.0.0.1 to only accept connections
     *             from the machine itself. Listens on all interfaces by default.
     * @return fluent
     */
    public StatusService bind(String host) {
        this.host = host;
        return this;
    }

    /**
     * @param http replaces the HTTP configuration of the server. HTTP/1.1 and HTTP/2 are enabled by default.
     * @return fluent
     */
    public StatusService http(HttpServerConfig http) {
        this.http = http;
        return this;
    }

    /**
     * Adds a check that must succeed for the application to be ready. A check that fails, throws or does not
     * complete within the check timeout makes {@code /health/ready} respond with 503. Adding a check with the name of an
     * existing check replaces it.
     *
     * @param name  the name of the check, shown in the readiness report.
     * @param check the check to execute when readiness is requested.
     * @return fluent
     */
    public StatusService check(String name, ReadinessCheck check) {
        checks.put(Objects.requireNonNull(name), Objects.requireNonNull(check));
        return this;
    }

    /**
     * @param timeoutMS how long to wait for a check to complete before it is considered to have failed, default 2000.
     * @return fluent
     */
    public StatusService checkTimeout(long timeoutMS) {
        this.checkTimeoutMS = timeoutMS;
        return this;
    }

    /**
     * @param details false to only serve the health endpoints, {@code /status} and the web page are then not found.
     *                True by default.
     * @return fluent
     */
    public StatusService details(boolean details) {
        this.details = details;
        return this;
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = core.logger(getClass());
        this.page = new Resource(RESOURCE_PAGE).read()
                .map(buffer -> buffer.toString(StandardCharsets.UTF_8))
                .orElseThrow(() -> new IllegalStateException("Missing resource " + RESOURCE_PAGE));
    }

    @Override
    public void start(Promise<Void> start) {
        Router router = Router.router(core.vertx());

        router.route().handler(this::headers);
        get(router, "/health/live", this::live);
        get(router, "/health/ready", this::ready);

        if (details) {
            get(router, "/status", this::status);
            get(router, "/", this::page);
        }

        router.route().handler(routing -> json(routing, HttpResponseStatus.NOT_FOUND,
                new JsonObject().put("error", "not found")));

        core.vertx().createHttpServer(http, settings.getSecurity())
                .requestHandler(router)
                .exceptionHandler(logger::onError)
                .listen(settings.getPort(), host)
                .onSuccess(result -> {
                    server = result;
                    settings.addListenPort(result.actualPort());
                    start.complete();
                })
                .onFailure(start::fail);
    }

    @Override
    public void stop(Promise<Void> stop) {
        if (server == null) {
            stop.complete();
        } else {
            server.close().onComplete(stop);
        }
    }

    @Override
    public String name() {
        return "status:" + settings.getPort();
    }

    /**
     * Routes GET and HEAD to the handler, other methods are not allowed.
     */
    private void get(Router router, String path, io.vertx.core.Handler<RoutingContext> handler) {
        router.route(path).method(HttpMethod.GET).method(HttpMethod.HEAD).handler(handler);
        router.route(path).handler(routing -> {
            routing.response().putHeader(HttpHeaders.ALLOW, "GET, HEAD");
            json(routing, HttpResponseStatus.METHOD_NOT_ALLOWED, new JsonObject().put("error", "method not allowed"));
        });
    }

    private void headers(RoutingContext routing) {
        routing.response().headers()
                .add(HttpHeaders.CACHE_CONTROL, "no-store")
                .add("X-Content-Type-Options", "nosniff")
                .add("X-Frame-Options", "DENY")
                .add("Referrer-Policy", "no-referrer");

        if (settings.isSecure()) {
            routing.response().headers().add("Strict-Transport-Security", "max-age=15768000");
        }
        routing.next();
    }

    private void live(RoutingContext routing) {
        json(routing, HttpResponseStatus.OK, new JsonObject().put("status", STATUS_UP));
    }

    private void ready(RoutingContext routing) {
        readiness().onSuccess(readiness -> json(routing,
                readiness.getBoolean("ready") ? HttpResponseStatus.OK : HttpResponseStatus.SERVICE_UNAVAILABLE,
                readiness));
    }

    private void status(RoutingContext routing) {
        readiness()
                .compose(readiness -> StatusReport.create(core, readiness))
                .onSuccess(report -> json(routing, HttpResponseStatus.OK, report))
                .onFailure(e -> {
                    logger.onError(e);
                    json(routing, HttpResponseStatus.INTERNAL_SERVER_ERROR, new JsonObject().put("error", "failed"));
                });
    }

    /**
     * The page is script and style in one file: a new nonce is generated for each response and is the only
     * thing that the content security policy allows to run.
     */
    private void page(RoutingContext routing) {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        routing.response().headers()
                .add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8")
                .add("Content-Security-Policy", "default-src 'none'; script-src 'nonce-" + nonce + "'; style-src 'nonce-" +
                        nonce + "'; connect-src 'self'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'");
        routing.response().end(page.replace(NONCE_PLACEHOLDER, nonce));
    }

    private void json(RoutingContext routing, HttpResponseStatus status, JsonObject body) {
        routing.response()
                .setStatusCode(status.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .end(body.encode());
    }

    /**
     * Runs all checks, concurrently.
     *
     * @return the readiness: ready only if all checks are UP, and the result of each check.
     */
    Future<JsonObject> readiness() {
        Map<String, Future<JsonObject>> results = new TreeMap<>();
        checks.forEach((name, check) -> results.put(name, run(check)));

        // the futures of the checks never fail: failures are results.
        return Future.join(new ArrayList<>(results.values()))
                .map(done -> {
                    JsonObject report = new JsonObject();
                    boolean ready = true;

                    for (var entry : results.entrySet()) {
                        JsonObject result = entry.getValue().result();
                        ready &= STATUS_UP.equals(result.getString("status"));
                        report.put(entry.getKey(), result);
                    }
                    return new JsonObject().put("ready", ready).put("checks", report);
                });
    }

    private Future<JsonObject> run(ReadinessCheck check) {
        long start = System.nanoTime();
        Promise<JsonObject> done = Promise.promise();
        long timer = core.vertx().setTimer(checkTimeoutMS, id ->
                done.tryComplete(result(start, "timed out after " + checkTimeoutMS + " ms")));

        Future<Void> checked;
        try {
            checked = check.check();
        } catch (Throwable e) {
            checked = Future.failedFuture(e);
        }
        checked.onComplete(result -> {
            core.vertx().cancelTimer(timer);
            done.tryComplete(result(start, result.succeeded() ? null :
                    Optional.ofNullable(result.cause().getMessage()).orElse("failed")));
        });
        return done.future();
    }

    private static JsonObject result(long start, String failure) {
        JsonObject result = new JsonObject()
                .put("status", (failure == null) ? STATUS_UP : STATUS_DOWN)
                .put("ms", Math.round((System.nanoTime() - start) / 10_000.0) / 100.0);

        if (failure != null) {
            result.put("message", failure);
        }
        return result;
    }
}
