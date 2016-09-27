package Routing.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Controller.ClientHandler;
import Configuration.Routing;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 */
public class ClientServer extends AbstractVerticle {
    private AuthServerSettings settings;
    private ClientHandler handler;

    public ClientServer(AuthProvider provider) {
        this.handler = new ClientHandler(provider);
        this.settings = provider.getAuthserverSettings();
    }

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());


        Routing.EnableCors(router);
        router.route("/api/*").handler(this::packet);

        //vertx.createHttpServer(new HttpServerOptions()
        //        .setCompressionSupported(true))
        //        .requestHandler(router::accept).listen(settings.getClientPort());

        future.complete();
    }

    private void packet(RoutingContext context) {
        handler.process(new ClientRestRequest(context));
    }
}
