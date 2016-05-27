package Authentication.Controller;

import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class ClientRestProtocol extends AbstractVerticle implements ClientProtocol {
    private HashMap<String, ClientPacketHandler> handlers = new HashMap<>();
    private HashMap<String, Access> access = new HashMap<>();
    private Access accessLevel;
    private Vertx vertx;
    private AuthServerSettings settings;

    public ClientRestProtocol(Provider provider, Access access) {
        this.settings = provider.getAuthserverSettings();
        this.accessLevel = access;
    }

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler) {
        return use(action, handler, accessLevel);
    }

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler, Access level) {
        String method = "/api/" + action;

        handlers.put(method, handler);
        access.put(method, level);
        return this;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.options("/*").handler(context -> {
            allowCors(context);
            context.response().setStatusCode(HttpResponseStatus.OK.code()).end();
        });

        router.route("/*").handler(context -> {
            allowCors(context);
            context.next();
        });

        router.route("/api/*").handler(this::packet);

        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).listen(settings.getClientPort());

        future.complete();
    }

    private void packet(RoutingContext context) {
        String path = context.request().path();

        if (handlers.containsKey(path))
            handlers.get(context.request().path()).handle(new ClientRestRequest(context));
        else
            context.request().response().setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code()).end();
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }
}
