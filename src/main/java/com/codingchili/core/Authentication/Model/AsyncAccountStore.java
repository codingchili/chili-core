package com.codingchili.core.Authentication.Model;

import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Asynchronous account store.
 */
public interface AsyncAccountStore {

    /**
     * Finds an account in the store.
     *
     * @param username username of the account to find.
     */
    void find(Future<Account> future, String username);

    /**
     * Authenticates an user in the accountstore.
     *
     * @param account unauthenticated account containing username and password.
     */
    void authenticate(Future<Account> future, Account account);

    /**
     * Registers a new account in the store.
     *
     * @param account contains account data to be created.
     */
    void register(Future<Account> future, Account account);

    /**
     * Adds a character to an username.
     *
     * @param realm     the realmName which the character is added to.
     * @param username   the name of the username the character is added to.
     * @param character the character to be added.
     */
    void upsertCharacter(Future future, String realm, String username, PlayerCharacter character);

    /**
     * Finds all characters associated with an account on specified realmName.
     *
     * @param realm       the realmName of which to search for the characters.
     * @param username the name of the account the characters belong to.
     */
    void findCharacters(Future<ArrayList<PlayerCharacter>> future, String realm, String username);

    /**
     * Finds a single character.
     *
     * @param realm    the realmName of which to search for the character.
     * @param username the name of the account the character belongs to.
     * @param character     the name of the character to find.
     */
    void findCharacter(Future<PlayerCharacter> future, String realm, String username, String character);

    /**
     * Finds and removes a character from specified realmName by its character name.
     *
     * @param realm     the realmName of which the character resides.
     * @param username  the name of the owning account.
     * @param character the name of the character.
     */
    void removeCharacter(Future future, String realm, String username, String character);
}
