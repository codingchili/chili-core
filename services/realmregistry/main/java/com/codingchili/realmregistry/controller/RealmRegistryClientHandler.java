package com.codingchili.realmregistry.controller;

import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.model.*;
import io.vertx.core.Future;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Role.PUBLIC;

/**
 * @author Robin Duda
 * Routing used to authenticate users and create/delete characters.
 */
public class RealmRegistryClientHandler implements CoreHandler {
    private final Protocol<ClientRequest> protocol = new Protocol<>();
    private AsyncRealmStore realms;
    private RegistryContext context;

    public RealmRegistryClientHandler(RegistryContext context) {
        this.context = context;

        protocol.use(CLIENT_REALM_LIST, this::realmlist, PUBLIC)
                .use(CLIENT_REALM_TOKEN, this::realmToken)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void start(Future<Void> start) {
        context.getRealmStore(done -> {
            if (done.succeeded()) {
                realms = done.result();
                start.complete();
            } else {
                start.fail(done.cause());
            }
        });
    }

    private Role authenticate(Request request) {
        boolean authorized = context.verifyClientToken(request.token());
        return (authorized) ? Role.USER : PUBLIC;
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticate(request)).submit(new ClientRequest(request));
    }

    @Override
    public String address() {
        return NODE_REALM_CLIENTS;
    }

    private void realmToken(ClientRequest request) {
        realms.signToken(result -> {
            if (result.succeeded()) {
                request.write(new TokenResponse(result.result()));
            } else {
                request.error(new RealmMissingException());
            }
        }, request.realmName(), request.account());
    }

    private void realmlist(Request request) {
        realms.getMetadataList(result -> {
            if (result.succeeded()) {
                request.write(new RealmList(result.result()));
            } else {
                request.error(result.cause());
            }
        });
    }
}
