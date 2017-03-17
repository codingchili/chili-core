package com.codingchili.realmregistry.model;

import com.codingchili.realmregistry.configuration.RealmSettings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

import com.codingchili.core.security.Token;


/**
 * @author Robin Duda
 *         <p>
 *         Asynchronous cluster-wide realm store.
 */
public interface AsyncRealmStore
{
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
     * @param realmName name of the realm that should sign the token.
     * @param domain    the domain (username) in which the token is valid.
     */
    void signToken(Handler<AsyncResult<Token>> future, String realmName, String domain);

    /**
     * Get all information available about a realm.
     *
     * @param future    callback
     * @param realmName name of the realm to find.
     */
    void get(Handler<AsyncResult<RealmSettings>> future, String realmName);

    /**
     * Place a realm into the cluster-wide get.
     *
     * @param future callback
     * @param realm  realm information to be inserted.
     */
    void put(Handler<AsyncResult<Void>> future, RealmSettings realm);

    /**
     * Removes a realm from the cluster-wide get.
     *
     * @param future    callback
     * @param realmName name of the realm.
     */
    void remove(Handler<AsyncResult<Void>> future, String realmName);

    /**
     *
     * @return
     */
    int getTimeout();

    /**
     *
     * @param timeout
     * @return
     */
    RealmDB setTimeout(int timeout);
}
