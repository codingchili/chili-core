package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;

/**
 * @author Robin Duda
 *         <p>
 *         Reuses the AsyncMap interface from hazelcast.
 */
public interface AsyncStorage<Key, Value> {

    /**
     * get an entry with the given key
     * @param key a unique key identifying an entry
     */
    void get(Key key, Handler<AsyncResult<Value>> handler);

    /**
     * set the entry identified by the given key to the given value
     * @param key a key to uniquely identify the entry
     * @param value the value to be set for the given key
     */
    void put(Key key, Value value, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry identified by the given key to the given value
     * @param key a key to uniquely identify the entry
     * @param value the value to be set for the given key
     * @param ttl the time to live for the entry
     */
    void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry if it does not already exists
     * @param key used to check if the entry exists and to set the value for.
     * @param value the value to be set if the entry does not exist.
     */
    void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler);

    /**
     * set the entry if it does not already exists
     * @param key used to check if the entry exists and to set the value for.
     * @param value the value to be set if the entry does not exist.
     * @param ttl the time to live for the entry
     */
    void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler);

    /**
     * @param key identifies the entry to be removed.
     */
    void remove(Key key, Handler<AsyncResult<Void>> handler);

    /**
     * replaces the value of the given key if a value already exists.
     * @param key the entry to be replaced if exists
     * @param value the new value of the entry
     */
    void replace(Key key, Value value, Handler<AsyncResult<Void>> handler);

    /**
     * removes all existing entries from the storage.
     */
    void clear(Handler<AsyncResult<Void>> handler);

    /**
     * returns the amount of entries in the storage.
     */
    void size(Handler<AsyncResult<Integer>> handler);

    /**
     * queries the storage for an exact match for a given single attribute.
     * @param attribute the attribute as a string to query for
     * @param comparable the value to match in the given attribute
     */
    void queryExact(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler);

    /**
     * queries the storage for a similar match given a single attribute
     * @param attribute the attribute as a string to query for
     * @param comparable the value to match with using starts-with
     */
    void querySimilar(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler);

    /**
     * queries the storage on an attribute within the given range
     * @param attribute the attribute as a string to query for
     * @param from inclusive minimum value
     * @param to inclusive max value
     */
    void queryRange(String attribute, int from, int to, Handler<AsyncResult<Collection<Value>>> handler);
}
