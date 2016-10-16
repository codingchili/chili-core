package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Authentication.Model.PlayerClassDisabledException;
import com.codingchili.core.Protocols.*;
import com.codingchili.core.Protocols.Authentication.CharacterList;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceProvider;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Instance.Controller.InstanceHandler;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;
import com.codingchili.core.Realm.Model.AsyncCharacterStore;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class RealmHandler extends AbstractHandler {
    private final Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private final RealmSettings realm;
    private final TokenFactory tokenFactory;
    private final Vertx vertx;
    private AsyncCharacterStore characters;


    public RealmHandler(RealmProvider provider) {
        super(provider.getRealm().getRemote());

        logger = provider.getLogger();
        realm = provider.getRealm();
        vertx = provider.getVertx();
        characters = provider.getCharacterStore();
        tokenFactory = new TokenFactory(realm.getAuthentication());

        startInstances();
        registerRealm();

        protocol.use(ANY, this::instanceHandler)
                .use(CLIENT_CHARACTER_LIST, this::characterList)
                .use(CLIENT_CHARACTER_CREATE, this::characterCreate)
                .use(CLIENT_CHARACTER_REMOVE, this::characterRemove)
                .use(ID_PING, Request::accept, Access.PUBLIC);
    }

    private void startInstances() {
        for (InstanceSettings instance : realm.getInstance()) {
            InstanceProvider provider = new InstanceProvider(realm, instance, vertx);
            vertx.deployVerticle(new ClusterListener(new InstanceHandler(provider)));
        }
    }

    private void registerRealm() {
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
                        request.write(new CharacterList(realm, result));
                    } else {
                        request.error();
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
                request.conflict();
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

        for (PlayerClass pc : realm.getClasses()) {
            if (pc.getName().equals(className))
                enabled = true;
        }

        if (enabled) {
            return new PlayerCharacter(realm.getTemplate(), characterName, className);
        } else
            throw new PlayerClassDisabledException();
    }

    private void instanceHandler(RealmRequest request) {
        request.missing();
    }

    private Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        protocol.get(authenticator(request), request.action()).handle(new RealmRequest(request));
    }

    @Override
    public void stop(Future<Void> future) {
        logger.onRealmStopped(future, realm);
    }

    @Override
    public void start(Future<Void> future) {
        logger.onRealmStarted(realm);
        future.complete();
    }
}
