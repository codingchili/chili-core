package com.codingchili.realm.controller;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.controller.InstanceRequest;
import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.model.*;

import java.util.Collection;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 *
 * Handles messages between the realm handler and clients.
 */
@Address(Address.WEBSOCKET)
public class RealmClientHandler implements CoreHandler {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private AsyncCharacterStore characters;
    private RealmContext context;

    public RealmClientHandler(RealmContext context) {
        this.context = context;
        this.characters = context.characters();
    }

    @Api(PUBLIC)
    public void ping(Request request) {
        request.accept();
    }

    @Api(route = ANY)
    public void instanceMessage(RealmRequest request) {
        request.connection().getProperty(ID_INSTANCE).ifPresent(instance -> {
            message(instance, request.data());
        });
    }

    private void message(String target, Object msg) {
        context.bus().send(target, msg);
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticator(request)).submit(new RealmRequest(request));
    }

    @Api(route = CLIENT_INSTANCE_JOIN)
    public void join(RealmRequest request) {
        if (context.connections().containsKey(request.account())) {
            throw new CoreRuntimeException("Failure: Already connected to " + request.connection().getProperty(ID_INSTANCE));
        } else {
            characters.findOne(find -> {
                        if (find.succeeded()) {
                            PlayerCreature creature = find.result();

                            InstanceRequest join = new InstanceRequest()
                                .setPlayer(find.result())
                                .setRealmName(context.realm().getName());

                            // save the instance the player is connected to on the request object.
                            request.connection().setProperty(ID_INSTANCE, creature.getInstance());
                            context.connections().put(request.account(), request.connection());

                            message(creature.getInstance(), join);
                        } else {
                            request.result(find);
                        }
                    }, request.account(),
                    request.character());
        }
    }

    @Api(route = CLIENT_INSTANCE_LEAVE)
    public void leave(RealmRequest request) {
        context.connections().remove(request.account());
    }

    @Api(route = CLIENT_CHARACTER_REMOVE)
    public void characterRemove(RealmRequest request) {
        characters.remove(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(remove.cause());
            }
        }, request.account(), request.character());
    }

    @Api(route = CLIENT_CHARACTER_LIST)
    public void characterList(RealmRequest request) {
        characters.findByUsername(characters -> {
            if (characters.succeeded()) {
                Collection<PlayerCreature> result = characters.result();

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

    @Api(route = CLIENT_CHARACTER_CREATE)
    public void characterCreate(RealmRequest request) {
        PlayerCreature creature = new PlayerCreature(request.character());
        creature.setAccount(request.account());
        creature.setClassName(request.className());

        characters.create(creation -> {
            if (creation.succeeded()) {
                request.accept();
            } else {
                request.error(new CharacterExistsException(request.character()));
            }

            // todo: create a PlayerCreature from the class template.
            //
        }, creature);
    }

    private PlayableClass readTemplate(String characterName, String className) throws PlayerClassDisabledException {

        for (PlayableClass pc : context.getClasses()) {
            if (pc.getName().equals(className))
                return pc;
        }
        throw new PlayerClassDisabledException();
    }


    private Role authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Role.USER;
        } else {
            return Role.PUBLIC;
        }
    }
}
