package com.codingchili.realmregistry.controller;

import com.codingchili.core.context.*;
import com.codingchili.core.protocol.*;
import com.codingchili.realmregistry.configuration.*;
import com.codingchili.realmregistry.model.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.*;

/**
 * @author Robin Duda
 *         Routing used to authenticate users and create/delete characters.
 */
public class ClientHandler implements CoreHandler {
    private final Protocol<RequestHandler<ClientRequest>> protocol = new Protocol<>();
    private final AsyncRealmStore realms;
    private RegistryContext context;

    public ClientHandler(RegistryContext context) {
        this.context = context;
        realms = context.getRealmStore();

        protocol.use(CLIENT_REALM_LIST, this::realmlist, PUBLIC)
                .use(CLIENT_REALM_TOKEN, this::realmToken)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    private Access authenticate(Request request) {
        boolean authorized = context.verifyClientToken(request.token());
        return (authorized) ? Access.AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(authenticate(request), request.route()).handle(new ClientRequest(request));
    }

    @Override
    public RegistryContext context() {
        return context;
    }

    @Override
    public String address() {
        return NODE_REALM_CLIENTS;
    }

    private void realmToken(ClientRequest request) {
        realms.signToken(result -> {
            if (result.succeeded()) {
                request.write(result.result());
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
