package Routing.Controller.Transport;

import Configuration.FileConfiguration;
import Configuration.Routing;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Controller.PatchHandler;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 */
public class ClientServerPatch implements Verticle {
    private PatchHandler handler;
    private Vertx vertx;
    private Logger logger;
    private PatchServerSettings settings;

    public ClientServerPatch(PatchProvider provider) {
        this.logger = provider.getLogger();
        this.settings = provider.getSettings();
        this.handler = new PatchHandler(provider);
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.settings = FileConfiguration.instance().getPatchServerSettings();
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        Routing.EnableCors(router);
        setLogging(router);
        serveAPI(router);
        setCatchAll(router);

       /* vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).listen(settings.getPort());*/

        start.complete();
    }

    private void setLogging(Router router) {
        router.route("/").handler(context -> {
            logger.onPageLoaded(context.request());
            context.next();
        });
    }

    private void serveAPI(Router router) {
        router.route("/api/*").handler(this::packet);
    }

    private void packet(RoutingContext context) {
        String method = context.request().path().replace("/api/", "");
        //ClientRequest request = new ClientRestRequest(context, method);
        //handler.process(request);
    }

    private void setCatchAll(Router router) {
        router.route().handler(context ->
                context.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end("{\"page\" : 404}"));
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
