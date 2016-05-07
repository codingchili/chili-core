package Website;

import Configuration.Config;
import Configuration.WebServerSettings;
import Utilities.DefaultLogger;
import Utilities.Logger;
import Utilities.Serializer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by Robin on 2016-04-07.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private Logger logger;
    private WebServerSettings settings;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.settings = Config.instance().getWebServerSettings();
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        setLogging(router);
        setApi(router);
        setResources(router);
        setCatchAll(router);

        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).listen(settings.getPort());
        logger.onServerStarted();
        start.complete();
    }

    private void setApi(Router router) {
        router.get("/api/news").handler(context -> {
            context.response().setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader("Content-Type", "application/json")
                    .end(Serializer.pack(settings.getNews()));
        });

        router.get("/api/authserver").handler(context -> {
            context.response().end(Serializer.pack(settings.getAuthserver()));
        });

        router.get("/api/gameinfo").handler(context -> {
            context.response().setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader("Content-Type", "application/json")
                    .end(Serializer.pack(settings.getInfo()));
        });

        router.get("/api/patchnotes").handler(context -> {
            context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Serializer.pack(settings.getPatch()));
        });
    }

    private void setLogging(Router router) {
        router.route("/").handler(context -> {
            logger.onPageLoaded(context.request());
            context.next();
        });
    }

    private void setResources(Router router) {
        router.route("/*").handler(StaticHandler.create()
                .setCachingEnabled(true));
    }

    private void setCatchAll(Router router) {
        router.route().handler(context -> {
            HttpServerResponse response = context.response();
            response.setStatusCode(404);
            response.putHeader("content-type", "application/json");
            response.end("{\"page\" : 404}");
        });
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
