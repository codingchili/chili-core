package com.codingchili.services.realm.model;

import io.vertx.core.Future;

import java.util.Map;

import com.codingchili.services.realm.instance.model.PlayerCharacter;

/**
 * @author Robin Duda
 */
public interface AsyncCharacterStore {
    /**
     * Adds a character to an username.
     *
     * @param username   the name of the username the character is added to.
     * @param character the character to be added.
     */
    void create(Future future, String username, PlayerCharacter character);

    /**
     * Finds all characters associated with an account on specified realmName.
     *
     * @param username the name of the account the characters belong to.
     */
    void find(Future<Map<String, PlayerCharacter>> future, String username);

    /**
     * Finds a single character.
     *
     * @param username the name of the account the character belongs to.
     * @param character     the name of the character to find.
     */
    void findOne(Future<PlayerCharacter> future, String username, String character);

    /**
     * Finds and removes a character from specified realmName by its character name.
     *
     * @param username  the name of the owning account.
     * @param character the name of the character.
     */
    void remove(Future future, String username, String character);
}
