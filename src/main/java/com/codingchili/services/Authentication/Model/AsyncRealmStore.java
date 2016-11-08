package com.codingchili.services.Authentication.Model;

import io.vertx.core.Future;

import java.util.ArrayList;

import com.codingchili.core.Security.Token;

import com.codingchili.services.Realm.Configuration.RealmSettings;

/**
 * @author Robin Duda
 *
 * Asynchronous cluster-wide realm store.
 */
public interface AsyncRealmStore {

    /**
     * Assemble a list of metadata for all available realms.
     */
    void getMetadataList(Future<ArrayList<RealmMetaData>> future);

    /**
     * Sign an user authentication token with a realms secret.
     * @param realmName name of the realm that should sign the token.
     * @param domain the domain (username) in which the token is valid.
     */
    void signToken(Future<Token> future, String realmName, String domain);

    /**
     * Get all information available about a realm.
     * @param realmName name of the realm to find.
     */
    void get(Future<RealmSettings> future, String realmName);

    /**
     * Place a realm into the cluster-wide get.
     * @param realm realm information to be inserted.
     */
    void put(Future<Void> future, RealmSettings realm);

    /**
     * Removes a realm from the cluster-wide get.
     * @param realmName name of the realm.
     */
    void remove(Future<RealmSettings> future, String realmName);
}
