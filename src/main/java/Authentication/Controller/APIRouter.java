package Authentication.Controller;

import Protocol.*;
import Authentication.Model.*;
import Configuration.Configuration.Security;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 *         <p>
 *         Router for the view-api.
 */
class APIRouter {
    private AsyncAccountStore accounts;
    private TokenFactory clientToken;

    /**
     * Register all existing API routes onto the router.
     *
     * @param router   the router which the routes should be registered to.
     * @param accounts the account storage.
     */
    public void register(Router router, AsyncAccountStore accounts) {
        this.accounts = accounts;

        clientToken = new TokenFactory(Security.secret);

        router.post("/api/register").handler(this::register);
        router.post("/api/authenticate").handler(this::authenticate);
    }

    private void register(RoutingContext context) {
        HttpServerResponse response = context.response();
        Account account = (Account) Serializer.unpack(context.getBodyAsJson(), Account.class);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded())
                    sendAuthentication(result.result(), context, true);
                else
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
                if (future.succeeded())
                    sendAuthentication(result.result(), context, false);
                else
                    throw future.cause();

            } catch (AccountMissingException e) {
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
            } catch (AccountPasswordException e) {
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
                .end(Serializer.pack(new Authentication(account, token, registered)));
    }


    private Token tokenFrom(RoutingContext context) {
        return (Token) Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class);
    }

    private boolean authorized(RoutingContext context) {
        boolean authorized = clientToken.verifyToken(tokenFrom(context));

        if (!authorized) {
            context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
        }

        return authorized;
    }
}
