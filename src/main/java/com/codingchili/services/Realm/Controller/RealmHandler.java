package com.codingchili.services.Realm.Controller;

import io.vertx.core.Future;

import java.util.Map;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Protocol.*;

import com.codingchili.services.Authentication.Model.PlayerClassDisabledException;
import com.codingchili.services.Realm.Configuration.RealmContext;
import com.codingchili.services.Realm.Instance.Configuration.InstanceContext;
import com.codingchili.services.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.services.Realm.Instance.Controller.InstanceHandler;
import com.codingchili.services.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.services.Realm.Instance.Model.PlayerClass;
import com.codingchili.services.Realm.Model.*;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.core.Protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class RealmHandler<T extends RealmContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private boolean registered = false;
    private final AsyncCharacterStore characters;


    public RealmHandler(T context) {
        super(context, context.address());

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

            context.deploy(new InstanceHandler<>(iContext));
        }
    }

    private void registerRealm(Long handler) {
        sendMaster(new RealmUpdate(context.realm()));
    }

    private void sendMaster(Object object) {
        context.bus().send(NODE_AUTHENTICATION_REALMS, Serializer.json(object), response -> {
            UpdateResponse update = UpdateResponse.from(response);

            if (update.is(ACCEPTED)) {
                if (!registered) {
                    context.onRealmRegistered(context.realm().getName());
                }

                registered = true;
            } else {
                registered = false;
                context.onRealmRejected(context.realm().getName());
            }
        });
    }

    private void characterRemove(RealmRequest request) {
        Future<PlayerCharacter> future = Future.future();

        future.setHandler(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(remove.cause());
            }
        });

        characters.remove(future, request.account(), request.character());
    }

    private void characterList(RealmRequest request) {
        Future<Map<String, PlayerCharacter>> characterFuture = Future.future();

        characterFuture.setHandler(characters -> {
            if (characters.succeeded()) {
                Map<String, PlayerCharacter> result = characters.result();

                if (result != null) {
                    request.write(new CharacterList(context.realm(), result));
                } else {
                    request.error(new CharacterMissingException(request.account()));
                }
            } else {
                request.error(characters.cause());
            }
        });
        characters.find(characterFuture, request.account());
    }

    private void characterCreate(RealmRequest request) {
        Future<PlayerCharacter> create = Future.future();

        create.setHandler(creation -> {
            if (creation.succeeded()) {
                request.accept();
            } else {
                request.conflict(new CharacterExistsException(request.character()));
            }
        });

        try {
            characters.create(create, request.account(), readTemplate(request.character(), request.className()));
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
    public void stop(Future<Void> future) {
        context.onRealmStopped(future, context.realm().getName());
    }

    @Override
    public void start(Future<Void> future) {
        context.onRealmStarted(context.realm().getName());
        future.complete();
    }
}
