package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.ValueMissingException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;

import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
 * @author Robin Duda
 * <p>
 * Reuses the AsyncMap interface from hazelcast.
 * <p>
 * Storages implementing this class are recommended to create an index for
 * attributes that are queried. It is highly recommended to create an index
 * for the ID field, which is used by all {@link Storable} classes.
 */
public interface AsyncStorage<Value extends Storable> {
    /**
     * get an entry with the given key, if the key does not match a value fails with
     * #{@link ValueMissingException}
     *
     * @param key     a unique key identifying an entry
     * @param handler callback
     */
    void get(String key, Handler<AsyncResult<Value>> handler);

    /**
     * checks if an entry exists for the given key
     *
     * @param key     the key to check if set
     * @param handler callback
     */
    default void contains(String key, Handler<AsyncResult<Boolean>> handler) {
        get(key, done -> {
            if (done.succeeded()) {
                handler.handle(result(true));
            } else {
                if (done.cause() instanceof ValueMissingException) {
                    handler.handle(result(false));
                } else {
                    handler.handle(error(done.cause()));
                }
            }
        });
    }

    /**
     * set the entry identified by the given key to the given value
     *
     * @param value   the value to be set for the given key
     * @param handler callback
     */
    void put(Value value, Handler<AsyncResult<Void>> handler);


    /**
     * set the entry if it does not already exists. fails with
     * #{@link com.codingchili.core.storage.exception.ValueAlreadyPresentException}
     * if the key already has a value.
     *
     * @param value   the value to be set if the entry does not exist.
     * @param handler callback
     */
    void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler);

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
     * Get all values contained within the storage.
     * Not recommended to use on large maps.
     *
     * @param handler callback
     */
    void values(Handler<AsyncResult<Collection<Value>>> handler);

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
     * Get the context for the storage.
     *
     * @return a storage context.
     */
    StorageContext<Value> context();
}
