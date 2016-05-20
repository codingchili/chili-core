package Authentication.Controller;

import Configuration.AuthServerSettings;
import Configuration.Config;
import Game.Model.PlayerCharacter;
import Authentication.Model.*;
import Protocol.Authentication.ClientAuthentication;
import Utilities.Logger;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Router used to authenticate users and create/delete characters.
 */
public class ClientHandler {
    private AuthServerSettings settings;
    private RealmHandler realms;
    private AsyncAccountStore accounts;
    private TokenFactory clientToken;
    private Vertx vertx;
    private Logger logger;

    public ClientHandler(Vertx vertx, Logger logger, AsyncAccountStore accounts, RealmHandler realms) {
        this.vertx = vertx;
        this.logger = logger;
        this.settings = Config.instance().getAuthSettings();
        this.clientToken = new TokenFactory(settings.getClientSecret());
        this.realms = realms;

        this.accounts = accounts;

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

        router.post("/api/realmtoken").handler(this::realmtoken);
        router.post("/api/character-list").handler(this::realmdata);
        router.post("/api/character-create").handler(this::createCharacter);
        router.post("/api/character-remove").handler(this::removeCharacter);
        router.post("/api/register").handler(this::register);
        router.post("/api/authenticate").handler(this::authenticate);
        router.get("/api/realmlist").handler(this::realmlist);

        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).listen(settings.getClientPort());
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }

    private void realmtoken(RoutingContext context) {
        if (verifyClient(context))
            context.response().end(Serializer.pack(realms.signToken(getRealm(context), getAccountName(context))));
    }

    private boolean verifyClient(RoutingContext context) {
        boolean verified = clientToken.verifyToken(getToken(context));

        if (!verified)
            context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();

        return verified;
    }

    private Token getToken(RoutingContext context) {
        return (Token) Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class);
    }

    private String getRealm(RoutingContext context) {
        return context.getBodyAsJson().getString("realm");
    }

    private String getAccountName(RoutingContext context) {
        return getToken(context).getDomain();
    }

    private void realmdata(RoutingContext context) {

        if (verifyClient(context)) {
            Future<ArrayList<PlayerCharacter>> future = Future.future();

            future.setHandler(result -> {
                if (result.succeeded()) {
                    JsonObject realm = Serializer.json(realms.getRealm(getRealm(context)));
                    realm.remove("authentication");

                    context.response().end(new JsonObject()
                            .put("characters", result.result())
                            .put("realm", realm)
                            .encode());
                } else
                    context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
            });

            accounts.findCharacters(future, getRealm(context), getAccountName(context));
        }
    }

    private void createCharacter(RoutingContext context) {
        if (verifyClient(context)) {
            Future<PlayerCharacter> find = Future.future();
            String characterName = context.getBodyAsJson().getString("name");

            find.setHandler(found -> {
                if (found.succeeded()) {
                    context.response().setStatusCode(HttpResponseStatus.CONFLICT.code()).end();
                } else {
                    upsertCharacter(context);
                }
            });
            accounts.findCharacter(find, getRealm(context), getAccountName(context), characterName);
        }
    }

    private void upsertCharacter(RoutingContext context) {
        try {
            PlayerCharacter character = createCharacterFromTemplate(context);

            accounts.addCharacter(Future.future().setHandler(creation -> {
                if (creation.succeeded()) {
                    context.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                } else {
                    context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
                }
            }), getRealm(context), getAccountName(context), character);
        } catch (PlayerClassDisabledException e) {
            context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
        }
    }


    private PlayerCharacter createCharacterFromTemplate(RoutingContext context) throws PlayerClassDisabledException {
        JsonObject data = context.getBodyAsJson();
        String name = data.getString("name");
        String className = data.getString("className");
        return realms.createCharacter(getRealm(context), name, className);
    }

    private void removeCharacter(RoutingContext context) {
        if (verifyClient(context)) {
            String name = context.getBodyAsJson().getString("name");
            Future<Void> future = Future.future();

            future.setHandler(remove -> {
                if (remove.succeeded())
                    context.response().end();
                else
                    context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            });

            accounts.removeCharacter(future, getRealm(context), getAccountName(context), name);
        }
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
                .end(Serializer.pack(new ClientAuthentication(account, token, registered, realms.getMetadataList())));

        if (registered)
            logger.onRegistered(account, context.request().remoteAddress().host());
        else
            logger.onAuthenticated(account, context.request().remoteAddress().host());
    }

    private void realmlist(RoutingContext context) {
        context.response().end(Serializer.pack(realms.getMetadataList()));
    }
}
