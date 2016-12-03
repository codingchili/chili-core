package com.codingchili.realmregistry.controller;

import com.codingchili.common.Strings;
import com.codingchili.realmregistry.configuration.RealmSettings;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.model.*;
import io.vertx.core.Future;

import java.time.Instant;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class RealmHandler<T extends RegistryContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private final AsyncRealmStore realms;

    public RealmHandler(T context) {
        super(context, Strings.NODE_AUTHENTICATION_REALMS);

        realms = context.getRealmStore();

        protocol.use(Strings.REALM_UPDATE, this::update)
                .use(Strings.CLIENT_CLOSE, this::disconnected)
                .use(CoreStrings.ID_PING, Request::accept, Access.PUBLIC);

        StaleRealmHandler.watch(context, realms);
    }

    private Access authenticate(Request request) {
        boolean authorized = context.verifyRealmToken(request.token());
        return (authorized) ? Access.AUTHORIZED : Access.PUBLIC;
    }

    @Override
    public void handle(Request request) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.get(authenticate(request), request.route()).handle(new RealmRequest(request));
    }

    private void update(RealmRequest request) {
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

    private void disconnected(RealmRequest request) {
        Future<Void> realmFuture = Future.future();

        realmFuture.setHandler(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(new RealmDisconnectException());
            }
        });

        realms.remove(realmFuture, request.realmName());
    }
}