package com.codingchili.authentication.controller;

import com.codingchili.authentication.configuration.AuthenticationContext;
import com.codingchili.authentication.model.AsyncAccountStore;
import com.codingchili.authentication.model.ClientAuthentication;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.AUTHORIZED;
import static com.codingchili.core.protocol.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to register/authenticate accounts.
 */
public class ClientHandler<T extends AuthenticationContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<ClientRequest>> protocol = new Protocol<>();
    private final AsyncAccountStore accounts;

    public ClientHandler(T context) {
        super(context, NODE_AUTHENTICATION_CLIENTS);

        accounts = context.getAccountStore();

        protocol.use(CLIENT_REGISTER, this::register, PUBLIC)
                .use(CLIENT_AUTHENTICATE, this::authenticate, PUBLIC)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) throws CoreException {
        Access access = (context.verifyClientToken(request.token())) ? AUTHORIZED : PUBLIC;
        protocol.get(access, request.route()).handle(new ClientRequest(request));
    }

    private void register(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            if (future.succeeded()) {
                sendAuthentication(result.result(), request, true);
            } else {
                request.error(result.cause());
            }
        });
        accounts.register(future, request.getAccount());
    }

    private void authenticate(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            if (future.succeeded()) {
                sendAuthentication(result.result(), request, false);
            } else {
                request.error(result.cause());
            }
        });
        accounts.authenticate(future, request.getAccount());
    }

    private void sendAuthentication(Account account, ClientRequest request, boolean registered) {
        request.write(
                new ClientAuthentication(
                        account,
                        context.signClientToken(account.getUsername()),
                        registered));

        if (registered)
            context.onRegistered(account.getUsername(), request.sender());
        else
            context.onAuthenticated(account.getUsername(), request.sender());
    }
}