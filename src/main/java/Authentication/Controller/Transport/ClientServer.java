package Authentication.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.ClientRequest;
import Configuration.Routing;
import Protocols.Access;
import Protocols.Authorization.TokenFactory;
import Protocols.Exception.AuthorizationRequiredException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 */
public class ClientServer extends AbstractVerticle {
    private AuthServerSettings settings;
    private TokenFactory tokens;
    private ClientHandler handler;

    public ClientServer(AuthProvider provider) {
        this.handler = new ClientHandler(provider);
        this.settings = provider.getAuthserverSettings();
        this.tokens = new TokenFactory(settings.getClientSecret());
    }

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());


        Routing.EnableCors(router);
        router.route("/api/*").handler(this::packet);

        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).listen(settings.getClientPort());

        future.complete();
    }

    private void packet(RoutingContext context) {
        handle(new ClientRestRequest(context));
    }

    public void handle(ClientRequest request) {
        try {
            handler.process(request, access(request));
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (Exception e) {
            request.error();
        }
    }

    private Access access(ClientRequest request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? Access.AUTHORIZE : Access.PUBLIC;
    }
}
