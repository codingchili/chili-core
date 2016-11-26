package com.codingchili.services.authentication.controller;

import io.vertx.core.Future;

import java.time.Instant;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.protocol.*;

import com.codingchili.services.authentication.configuration.AuthContext;
import com.codingchili.services.authentication.model.*;
import com.codingchili.services.realm.configuration.RealmSettings;

import static com.codingchili.core.protocol.Access.*;
import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class AuthenticationHandler<T extends AuthContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<AuthenticationRequest>> protocol = new Protocol<>();
    private final AsyncRealmStore realms;

    public AuthenticationHandler(T context) {
        super(context, NODE_AUTHENTICATION_REALMS);

        realms = context.getRealmStore();

        protocol.use(REALM_UPDATE, this::update)
                .use(CLIENT_CLOSE, this::disconnected)
                .use(ID_PING, Request::accept, PUBLIC);

        StaleRealmHandler.watch(context, realms);
    }

    private Access authenticate(Request request) {
        boolean authorized = context.verifyRealmToken(request.token());
        return (authorized) ? AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.get(authenticate(request), request.route()).handle(new AuthenticationRequest(request));
    }

    private void update(AuthenticationRequest request) {
        Future<Void> realmFuture = Future.future();
        RealmSettings realm = request.getRealm();

        realm.setTrusted(context.isTrustedRealm(realm.getName()));
        realm.setUpdated(Instant.now().toEpochMilli());

        realmFuture.setHandler(insert -> {
            if (insert.succeeded()) {
                request.accept();
                context.onRealmUpdated(realm.getName(), realm.getPlayers());
            } else {
                request.error(new RealmUpdateException());
            }
        });

        realms.put(realmFuture, realm);
    }

    private void disconnected(AuthenticationRequest request) {
        Future<RealmSettings> realmFuture = Future.future();

        realmFuture.setHandler(remove -> {
            if (remove.succeeded() && remove.result() != null) {
                request.accept();
            } else {
                request.error(new RealmDisconnectException());
            }
        });

        realms.remove(realmFuture, request.realmName());
    }
}