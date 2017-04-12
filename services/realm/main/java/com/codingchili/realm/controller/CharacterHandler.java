package com.codingchili.realm.controller;

import java.util.*;

import com.codingchili.core.context.*;
import com.codingchili.core.protocol.*;
import com.codingchili.realm.configuration.*;
import com.codingchili.realm.instance.configuration.*;
import com.codingchili.realm.instance.controller.*;
import com.codingchili.realm.instance.model.*;
import com.codingchili.realm.model.*;

import io.vertx.core.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class CharacterHandler implements CoreHandler {
    private final Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private final AsyncCharacterStore characters;
    private boolean registered = false;
    private RealmContext context;

    public CharacterHandler(RealmContext context) {
        this.context = context;

        characters = context.getCharacterStore();

        startInstances();
        context.periodic(context::updateRate, getClass().getSimpleName(), this::registerRealm);

        protocol.use(ANY, this::instanceHandler)
                .use(CLIENT_CHARACTER_LIST, this::characterList)
                .use(CLIENT_CHARACTER_CREATE, this::characterCreate)
                .use(CLIENT_CHARACTER_REMOVE, this::characterRemove)
                .use(ID_PING, Request::accept, Access.PUBLIC);
    }

    private void startInstances() {
        for (InstanceSettings instance : context.instances()) {
            InstanceContext iContext = new InstanceContext(context, instance);

            context.deploy(new InstanceHandler(iContext));
        }
    }

    private void registerRealm(Long handler) {
        sendMaster(new RealmUpdate(context.realm()));
    }

    private void sendMaster(RealmUpdate realm) {
        context.bus().send(NODE_AUTHENTICATION_REALMS, Serializer.json(realm), response -> {
            if (response.succeeded()) {
                UpdateResponse update = new UpdateResponse(response.result());

                if (update.is(ACCEPTED)) {
                    if (!registered) {
                        context.onRealmRegistered(context.realm().getName());
                    }

                    registered = true;
                } else {
                    registered = false;
                    context.onRealmRejected(context.realm().getName(), update.message());
                }
            }
        });
    }

    private void characterRemove(RealmRequest request) {
        characters.remove(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(remove.cause());
            }
        }, request.account(), request.character());
    }

    private void characterList(RealmRequest request) {
        characters.findByUsername(characters -> {
            if (characters.succeeded()) {
                Collection<PlayerCharacter> result = characters.result();

                if (result != null) {
                    request.write(new CharacterList(context.realm(), result));
                } else {
                    request.error(new CharacterMissingException(request.account()));
                }
            } else {
                request.error(characters.cause());
            }
        }, request.account());
    }

    private void characterCreate(RealmRequest request) {
        try {
            characters.create(creation -> {
                if (creation.succeeded()) {
                    request.accept();
                } else {
                    request.error(new CharacterExistsException(request.character()));
                }
            }, request.account(), readTemplate(request.character(), request.className()));
        } catch (PlayerClassDisabledException e) {
            request.error(e);
        }
    }

    private PlayerCharacter readTemplate(String characterName, String className) throws PlayerClassDisabledException {
        boolean enabled = false;

        for (PlayerClass pc : context.getClasses()) {
            if (pc.getName().equals(className))
                enabled = true;
        }

        if (enabled) {
            return new PlayerCharacter(context.getTemplate(), characterName, className);
        } else
            throw new PlayerClassDisabledException();
    }

    private void instanceHandler(RealmRequest request) {
        context.bus().send(request.instance(), request, result -> {

        });
    }

    private Access authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(authenticator(request), request.route()).handle(new RealmRequest(request));
    }

    @Override
    public RealmContext context() {
        return context;
    }

    @Override
    public String address() {
        return context.address();
    }

    @Override
    public void stop(Future<Void> future) {
        context.onRealmStopped(future, context.realm().getName());
    }

    @Override
    public void start(Future<Void> future) {
        context.onRealmStarted(context.realm().getName());
        future.complete();
    }
}
