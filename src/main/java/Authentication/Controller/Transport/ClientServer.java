package Authentication.Controller.Transport;

import Authentication.Controller.AuthProvider;
import Protocols.AuthorizationHandler.Access;
import Authentication.Controller.ClientRequest;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Protocols.AuthorizationHandler;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Authentication.Configuration.AuthServerSettings;
import Protocols.Authorization.TokenFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 */
public class ClientServer extends AbstractVerticle {
    private Protocol<PacketHandler<ClientRequest>> protocol;
    private AuthServerSettings settings;
    private TokenFactory tokens;

    public ClientServer(AuthProvider provider) {
        this.settings = provider.getAuthserverSettings();
        this.protocol = provider.clientProtocol();
        this.tokens = new TokenFactory(settings.getClientSecret());
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

    public void handle(String action, ClientRequest request) {
        try {
            protocol.get(action, access(request)).handle(request);
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
        }
    }

    private AuthorizationHandler.Access access(ClientRequest request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? Access.AUTHORIZE : Access.PUBLIC;
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }

}
