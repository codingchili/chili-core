package com.codingchili.services.realm.model;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.storage.AsyncStorage;

import com.codingchili.services.realm.instance.model.PlayerCharacter;

import static com.codingchili.services.Shared.Strings.MAP_CHARACTERS;
import static com.codingchili.services.Shared.Strings.MAP_ID;

/**
 * @author Robin Duda
 *
 * Storage for characters.
 */
public class CharacterDB implements AsyncCharacterStore {
    private final AsyncStorage<String, Map<String, PlayerCharacter>> characters;

    public CharacterDB(AsyncStorage<String, Map<String, PlayerCharacter>> map) {
        this.characters = map;
    }

    @Override
    public void create(Future future, String username, PlayerCharacter character) {
        Future<Map<String, PlayerCharacter>> get = Future.future();

        get.setHandler(list -> {
            if (list.succeeded()) {
                Map<String, PlayerCharacter> map = list.result();

                if (map.containsKey(character.getName())) {
                    future.fail(new CharacterExistsException(character.getName()));
                } else {
                    map.put(character.getName(), character);
                    save(future, username, map);
                }
            } else {
                future.fail(list.cause());
            }
        });

        find(get, username);
    }

    private void save(Future future, String username, Map<String, PlayerCharacter> map) {
        characters.put(username, map, put -> {
            if (put.succeeded()) {
                future.complete();
            } else {
                future.fail(put.cause());
            }
        });
    }

    @Override
    public void find(Future<Map<String, PlayerCharacter>> future, String username) {
        characters.get(username, get -> {
            if (get.succeeded()) {
                if (get.result() == null) {
                    future.complete(new HashMap<>());
                } else {
                    future.complete(get.result());
                }
            } else {
                future.fail(get.cause());
            }
        });
    }

    @Override
    public void findOne(Future<PlayerCharacter> future, String username, String character) {
        Future<Map<String, PlayerCharacter>> get = Future.future();

        get.setHandler(list -> {
            if (list.succeeded()) {
                Map<String, PlayerCharacter> map = list.result();

                if (map.containsKey(character)) {
                    future.complete(map.get(character));
                } else {
                    future.fail(new CharacterMissingException(character));
                }
            } else {
                future.fail(list.cause());
            }
        });

        find(get, username);
    }

    @Override
    public void remove(Future future, String username, String character) {
        Future<Map<String, PlayerCharacter>> get = Future.future();

        get.setHandler(list -> {
            if (list.succeeded()) {
                Map<String, PlayerCharacter> map = list.result();

                if (map.containsKey(character)) {
                    map.remove(character);
                    save(future, username, map);
                } else {
                    future.fail(new CharacterMissingException(character));
                }
            } else {
                future.fail(list.cause());
            }
        });
        find(get, username);
    }
}
