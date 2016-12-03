package com.codingchili.realm.model;

import io.vertx.core.Future;

import java.util.Collection;

import com.codingchili.core.storage.AsyncStorage;

import com.codingchili.realm.instance.model.PlayerCharacter;

import static com.codingchili.common.Strings.ID_ACCOUNT;

/**
 * @author Robin Duda
 *         <p>
 *         Storage for characters.
 */
public class CharacterDB implements AsyncCharacterStore {
    private final AsyncStorage<String, PlayerCharacter> characters;

    public CharacterDB(AsyncStorage<String, PlayerCharacter> map) {
        this.characters = map;
    }

    @Override
    public void create(Future future, String username, PlayerCharacter character) {
        characters.putIfAbsent(character.getName(), character, result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void findByUsername(Future<Collection<PlayerCharacter>> future, String username) {
        characters.queryExact(ID_ACCOUNT, username, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void findOne(Future<PlayerCharacter> future, String username, String character) {
        characters.get(character, result -> {
            if (result.succeeded()) {
                PlayerCharacter found = result.result();

                if (found.getAccount().equals(username)) {
                    future.complete(found);
                } else {
                    future.fail(new CharacterMissingException(character));
                }
            } else {
                future.fail(new CharacterMissingException(character));
            }
        });
    }

    @Override
    public void remove(Future future, String username, String character) {
        characters.get(character, result -> {
            if (result.succeeded()) {
                PlayerCharacter found = result.result();

                if (username.equals(found.getAccount())) {
                    characters.remove(character, removed -> {
                        if (removed.succeeded()) {
                            future.complete();
                        } else {
                            future.fail(new CharacterMissingException(character));
                        }
                    });
                } else {
                    future.fail(new CharacterMissingException(character));
                }
            } else {
                future.fail(result.cause());
            }
        });
    }
}
