package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *         <p>
 *         Reuses the AsyncMap interface from hazelcast.
 *         <p>
 *         Storages implementing this class are recommended to create an index for
 *         attributes that are queried. It is highly recommended to create an index
 *         for the ID field, which is used by all {@link Storable} classes.
 */
public interface AsyncStorage<Value extends Storable>
{
    /**
     * get an entry with the given key
     *
     * @param key     a unique key identifying an entry
     * @param handler callback
     */
    void get(String key, Handler<AsyncResult<Value>> handler);

    /**
     * set the entry identified by the given key to the given value
     *
     * @param value   the value to be set for the given key
     * @param handler callback
     */
    void put(Value value, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry identified by the given key to the given value
     *
     * @param value   the value to be set for the given key
     * @param ttl     the time to live for the entry. Recommended to use
     *                only for objects that will never change. As the timers
     *                are not cleared on remove or update.
     * @param handler callback
     */
    void put(Value value, long ttl, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry if it does not already exists
     *
     * @param value   the value to be set if the entry does not exist.
     * @param handler callback
     */
    void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry if it does not already exists
     *
     * @param value   the value to be set if the entry does not exist.
     * @param ttl     the time to live for the entry. Recommended to use
     *                only for objects that will never change. As the timers
     *                are not cleared on remove or update.
     * @param handler callback
     */
    void putIfAbsent(Value value, long ttl, Handler<AsyncResult<Void>> handler);

    /**
     * @param key     identifies the entry to be removed.
     * @param handler callback
     */
    void remove(String key, Handler<AsyncResult<Void>> handler);

    /**
     * updates the value of the given key if a value already exists.
     *
     * @param value   the new value of the entry
     * @param handler callback
     */
    void update(Value value, Handler<AsyncResult<Void>> handler);

    /**
     * removes all existing entries from the storage.
     *
     * @param handler callback
     */
    void clear(Handler<AsyncResult<Void>> handler);

    /**
     * returns the amount of entries in the storage.
     *
     * @param handler callback
     */
    void size(Handler<AsyncResult<Integer>> handler);

    /**
     * initialize the construction of a query.
     *
     * @param attribute the attribute to be queried.
     * @return a builder String for constructing the query.
     */
    QueryBuilder<Value> query(String attribute);

    /**
     * Constructs a query from a json object
     * @param query to build from
     * @return a querybuilder object
     */
    //QueryBuilder<Value> query(JsonObject query);
}
