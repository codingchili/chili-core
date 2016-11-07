package com.codingchili.services.Authentication.Controller;

import io.vertx.core.Future;

import java.util.ArrayList;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Protocol.*;
import com.codingchili.core.Security.Token;

import com.codingchili.services.Authentication.Configuration.AuthContext;
import com.codingchili.services.Authentication.Model.*;

import static com.codingchili.core.Protocol.Access.PUBLIC;
import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         Routing used to authenticate users and create/delete characters.
 */
public class ClientHandler<T extends AuthContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<ClientRequest>> protocol = new Protocol<>();
    private final AsyncRealmStore realmStore;
    private final AsyncAccountStore accounts;

    public ClientHandler(T context) {
        super(context, NODE_AUTHENTICATION_CLIENTS);

        accounts = context.getAccountStore();
        realmStore = context.getRealmStore();

        protocol.use(CLIENT_REALM_TOKEN, this::realmToken)
                .use(CLIENT_REALM_LIST, this::realmlist, PUBLIC)
                .use(CLIENT_REGISTER, this::register, PUBLIC)
                .use(CLIENT_AUTHENTICATE, this::authenticate, PUBLIC)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    private Access authenticate(Request request) {
        boolean authorized = context.verifyClientToken(request.token());
        return (authorized) ? Access.AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(authenticate(request), request.action()).handle(new ClientRequest(request));
    }

    private void realmToken(ClientRequest request) {
        Future<Token> token = Future.future();

        token.setHandler(result -> {
            if (result.succeeded()) {
                request.write(result.result());
            } else {
                request.error(new RealmMissingException());
            }
        });

        realmStore.signToken(token, request.realmName(), request.account());
    }


    private void register(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), request, true);
                } else {
                    throw future.cause();
                }
            } catch (AccountExistsException e) {
                request.conflict(e);
            } catch (Throwable e) {
                request.error(e);
            }
        });
        accounts.register(future, request.getAccount());
    }

    private void authenticate(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), request, false);
                } else
                    throw future.cause();

            } catch (AccountMissingException e) {
                request.missing(e);
            } catch (AccountPasswordException e) {
                context.onAuthenticationFailure(request.getAccount().getUsername(), request.sender());
                request.unauthorized(e);
            } catch (Throwable e) {
                request.error(e);
            }
        });
        accounts.authenticate(future, request.getAccount());
    }

    private void sendAuthentication(Account account, ClientRequest request, boolean registered) {
        Future<ArrayList<RealmMetaData>> future = Future.future();


        future.setHandler(metadata -> {
            request.write(
                    new ClientAuthentication(
                            account,
                            context.signClientToken(account.getUsername()),
                            registered,
                            metadata.result()));

            if (registered)
                context.onRegistered(account.getUsername(), request.sender());
            else
                context.onAuthenticated(account.getUsername(), request.sender());
        });

        realmStore.getMetadataList(future);
    }

    private void realmlist(Request request) {
        Future<ArrayList<RealmMetaData>> future = Future.future();

        future.setHandler(result -> request.write(new RealmList(result.result())));

        realmStore.getMetadataList(future);
    }
}
