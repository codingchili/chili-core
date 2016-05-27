package Authentication.Controller.Transport;

import Authentication.Controller.ClientPacketHandler;
import Authentication.Controller.ClientProtocol;
import Authentication.Controller.ClientRequest;
import Authentication.Model.AuthorizationHandler;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Model.AuthorizationRequired;
import Authentication.Model.HandlerMissingException;
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

/**
 * @author Robin Duda
 */
public class ClientRestProtocol extends AbstractVerticle implements ClientProtocol {
    private AuthorizationHandler<ClientPacketHandler> handlers;
    private Vertx vertx;
    private AuthServerSettings settings;

    public ClientRestProtocol(Provider provider, Access access) {
        this.settings = provider.getAuthserverSettings();
        this.handlers = new AuthorizationHandler<>(access);
    }

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler) {
        handlers.use(action, handler);
        return this;
    }

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler, Access access) {
        handlers.use(action, handler, access);
        return this;
    }

    @Override
    public void handle(String action, ClientRequest request) {
        try {
            handlers.get(action, Access.PUBLIC);
        } catch (AuthorizationRequired e) {
            request.unauthorize();
        } catch (HandlerMissingException e) {
            request.error();
        }
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
        String path = context.request().path().replace("/api/", "");
        handle(path, new ClientRestRequest(context));
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }
}
