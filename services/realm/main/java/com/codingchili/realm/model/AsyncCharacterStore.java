package com.codingchili.realm.model;

import com.codingchili.realm.instance.model.entity.PlayerCreature;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public interface AsyncCharacterStore {
    /**
     * Adds a character to an username.
     *
     * @param future    callback
     * @param character the character to be added.
     */
    void create(Handler<AsyncResult<Void>> future, PlayerCreature character);

    /**
     * Finds all characters associated with an account on specified realmName.
     *
     * @param future   callback
     * @param username the handler of the account the characters belong to.
     */
    void findByUsername(Handler<AsyncResult<Collection<PlayerCreature>>> future, String username);

    /**
     * Finds a single character.
     *
     * @param future    callback
     * @param username  the handler of the account the character belongs to.
     * @param character the handler of the character to find.
     */
    void findOne(Handler<AsyncResult<PlayerCreature>> future, String username, String character);

    /**
     * Finds and removes a character from specified realmName by its character handler.
     *
     * @param future    callback
     * @param username  the handler of the owning account.
     * @param character the handler of the character.
     */
    void remove(Handler<AsyncResult<Void>> future, String username, String character);
}
