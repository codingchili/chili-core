package com.codingchili.realm.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.model.PlayerCharacter;
import com.codingchili.realm.instance.model.PlayerClass;
import com.codingchili.realm.model.*;
import io.vertx.core.Future;

import java.util.Collection;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 * Handles traveling between instances.
 */
public class CharacterHandler implements CoreHandler {
    private final Protocol<RealmRequest> protocol = new Protocol<>();
    private final AsyncCharacterStore characters;
    private boolean registered = false;
    private RealmContext context;

    public CharacterHandler(RealmContext context) {
        this.context = context;
        this.characters = context.getCharacterStore();

        context.periodic(context::updateRate, getClass().getSimpleName(), this::registerRealm);

        protocol.use(ANY, this::instanceHandler)
                .use(CLIENT_CHARACTER_LIST, this::characterList)
                .use(CLIENT_CHARACTER_CREATE, this::characterCreate)
                .use(CLIENT_CHARACTER_REMOVE, this::characterRemove)
                .use(ID_PING, Request::accept, Role.PUBLIC);
    }

    @Override
    public void start(Future<Void> future) {
        context.onRealmStarted(context.realm().getName());
        future.complete();
    }

    private void instanceHandler(Request request) {
        // forward on bus?
    }

    private void registerRealm(Long handler) {
        RealmUpdate realm = new RealmUpdate(context.realm());

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

    private Role authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Role.USER;
        } else {
            return Role.PUBLIC;
        }
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticator(request)).submit(new RealmRequest(request));
    }

    @Override
    public String address() {
        return context.address();
    }

    @Override
    public void stop(Future<Void> future) {
        context.onRealmStopped(future, context.realm().getName());
        future.complete();
    }
}
