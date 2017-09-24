package com.codingchili.realmregistry.model;

import com.codingchili.core.security.Token;
import com.codingchili.realmregistry.configuration.RegisteredRealm;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;


/**
 * @author Robin Duda
 * <p>
 * Asynchronous cluster-wide realm store.
 */
public interface AsyncRealmStore {
    /**
     * Assemble a list of metadata for all available realms.
     *
     * @param future callback
     */
    void getMetadataList(Handler<AsyncResult<List<RealmMetaData>>> future);

    /**
     * Sign an user authentication token with a realms secret.
     *
     * @param future    callback
     * @param realmName handler of the realm that should sign the token.
     * @param domain    the domain (username) in which the token is valid.
     */
    void signToken(Handler<AsyncResult<Token>> future, String realmName, String domain);

    /**
     * Get all information available about a realm.
     *
     * @param future    callback
     * @param realmName handler of the realm to find.
     */
    void get(Handler<AsyncResult<RegisteredRealm>> future, String realmName);

    /**
     * Place a realm into the cluster-wide get.
     *
     * @param future callback
     * @param realm  realm information to be inserted.
     */
    void put(Handler<AsyncResult<Void>> future, RegisteredRealm realm);

    /**
     * Removes a realm from the cluster-wide get.
     *
     * @param future    callback
     * @param realmName handler of the realm.
     */
    void remove(Handler<AsyncResult<Void>> future, String realmName);

    /**
     * Get the timeout in milliseconds before a Realm is removed unless it has
     * received an update.
     *
     * @return integer value representing milliseconds
     */
    int getTimeout();

    /**
     * @param timeout timeout in milliseconds before a realm is removed unless
     *                it has received an update.
     * @return fluent
     */
    RealmDB setTimeout(int timeout);
}
