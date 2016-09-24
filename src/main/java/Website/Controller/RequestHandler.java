package Website.Controller;

import Configuration.Routing;
import Logging.Model.Logger;
import Website.Configuration.WebserverProvider;
import Website.Configuration.WebserverSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author Robin Duda
 */
public class RequestHandler extends AbstractVerticle {
    private Logger logger;
    private int coreId;
    private WebserverSettings settings;


    public RequestHandler(WebserverProvider provider, int coreId) {
        this.logger = provider.getLogger();
        this.settings = provider.getSettings();
        this.coreId = coreId;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        Routing.EnableCors(router);
        setLogging(router);
        serveWebsite(router);
        setCatchAll(router);


        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(settings.getCompress()))
                .requestHandler(router::accept).listen(settings.getPort());
    }

    private void setLogging(Router router) {
        router.route("/").handler(context -> {
            context.response().putHeader("served-by", coreId + "");
            logger.onPageLoaded(context.request());
            context.next();
        });
    }

    private void serveWebsite(Router router) {
        router.route("/*").handler(StaticHandler.create("website/")
                .setCachingEnabled(settings.getCache()));
    }

    private void setCatchAll(Router router) {
        router.route().handler(context ->
                context.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end("{\"page\" : 404}"));
    }

}
