package Authentication.Controller;

import Configuration.AuthServerSettings;
import io.netty.handler.codec.http.HttpResponseStatus;
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
public class ClientRestProtocol implements ClientProtocol {
    private HashMap<String, ClientPacketHandler> handlers = new HashMap<>();

    public ClientRestProtocol(Vertx vertx, AuthServerSettings settings) {
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
    }

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler) {
        handlers.put("/api/" + action, handler);
        return this;
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
