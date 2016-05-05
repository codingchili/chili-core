package Authentication.Controller;

import Configuration.AuthServerSettings;
import Configuration.Config;
import Protocol.*;
import Authentication.Model.*;
import Utilities.Logger;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author Robin Duda
 *         <p>
 *         Router for the view-api.
 */
public class ClientHandler {
    private AuthServerSettings settings;
    private RealmHandler realms;
    private AsyncAccountStore accounts;
    private TokenFactory clientToken;
    private Vertx vertx;
    private Logger logger;

    public ClientHandler(Vertx vertx, Logger logger, RealmHandler realms) {
        this.vertx = vertx;
        this.logger = logger;
        this.realms = realms;
        this.settings = Config.instance().getAuthSettings();
        this.clientToken = new TokenFactory(settings.getClientSecret());
        this.accounts = new AccountDB(
                MongoClient.createShared(vertx, new JsonObject()
                        .put("db_name", settings.getDatabase().getName())
                        .put("connection_string", settings.getDatabase().getRemote())));

        startServer();
    }

    private void startServer() {
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

        router.post("/api/register").handler(this::register);
        router.post("/api/authenticate").handler(this::authenticate);
        router.get("/api/realmlist").handler(this::realmlist);

        vertx.createHttpServer().requestHandler(router::accept).listen(settings.getClientPort());
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }

    private void register(RoutingContext context) {
        HttpServerResponse response = context.response();
        Account account = (Account) Serializer.unpack(context.getBodyAsJson(), Account.class);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), context, true);
                } else
                    throw future.cause();

            } catch (AccountExistsException e) {
                response.setStatusCode(HttpResponseStatus.CONFLICT.code()).end();
            } catch (Throwable e) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
        accounts.register(future, account);
    }

    private void authenticate(RoutingContext context) {
        HttpServerResponse response = context.response();
        Account account = (Account) Serializer.unpack(context.getBodyAsJson(), Account.class);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), context, false);
                } else
                    throw future.cause();

            } catch (AccountMissingException e) {
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
            } catch (AccountPasswordException e) {
                logger.onAuthenticationFailure(account, context.request().remoteAddress().host());
                response.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            } catch (Throwable e) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
        accounts.authenticate(future, account);
    }

    private void sendAuthentication(Account account, RoutingContext context, boolean registered) {
        Token token = new Token(clientToken, account.getUsername());
        context.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Serializer.pack(new Authentication(account, token, registered, realms.getList())));

        if (registered)
            logger.onRegistered(account, context.request().remoteAddress().host());
        else
            logger.onAuthenticated(account, context.request().remoteAddress().host());
    }

    private void realmlist(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.setStatusCode(HttpResponseStatus.OK.code()).end(Serializer.pack(realms.getList()));
    }
}
