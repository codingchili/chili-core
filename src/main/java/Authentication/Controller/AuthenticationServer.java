package Authentication.Controller;

import Configuration.Configuration.*;
import Authentication.Model.AccountDB;
import Authentication.Model.AsyncAccountStore;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by Robin on 2016-04-07.
 */
public class AuthenticationServer implements Verticle {
    private AsyncAccountStore accounts;
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, "Authentication");

        this.accounts = new AccountDB(
                MongoClient.createShared(vertx, Database.configuration));
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.options("/*").handler(context -> {
            allowCors(context).end();
        });

        router.route("/*").handler(context -> {
            allowCors(context);
            context.next();
        });

        new APIRouter().register(router, accounts);

        vertx.createHttpServer().requestHandler(router::accept).listen(Authentication.PORT);
        start.complete();
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
