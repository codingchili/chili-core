package com.codingchili.core.storage;

import io.vertx.core.Future;

import java.util.stream.Stream;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.ValueMissingException;

import static com.codingchili.core.context.FutureHelper.*;

/**
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
     * @param key a unique key identifying an entry
     * @return future completed with the value.
     */
    Future<Value> get(String key);

    /**
     * checks if an entry exists for the given key
     *
     * @param key the key to check if set
     * @return future completed with true if the key exists.
     */
    default Future<Boolean> contains(String key) {
        return get(key).transform(done -> {
            if (done.succeeded()) {
                return result(true);
            } else if (done.cause() instanceof ValueMissingException) {
                return result(false);
            } else {
                return error(done.cause());
            }
        });
    }

    /**
     * set the entry identified by the given key to the given value
     *
     * @param value the value to be set for the given key
     * @return future completed when the value is set.
     */
    Future<Void> put(Value value);


    /**
     * set the entry if it does not already exists. fails with
     * #{@link com.codingchili.core.storage.exception.ValueAlreadyPresentException}
     * if the key already has a value.
     *
     * @param value the value to be set if the entry does not exist.
     * @return future completed when the value is set.
     */
    Future<Void> putIfAbsent(Value value);

    /**
     * Removes an entry by its key.
     *
     * @param key identifies the entry to be removed.
     * @return future completed when the entry is removed.
     */
    Future<Void> remove(String key);

    /**
     * updates the value of the given key if a value already exists.
     *
     * @param value the new value of the entry
     * @return future completed when the entry is updated.
     */
    Future<Void> update(Value value);

    /**
     * Get all values contained within the storage as a stream.
     * Not recommended to use on large maps.
     *
     * @return future completed with a stream of all values.
     */
    Future<Stream<Value>> values();

    /**
     * removes all existing entries from the storage.
     *
     * @return future completed when the storage is cleared.
     */
    Future<Void> clear();

    /**
     * returns the amount of entries in the storage.
     *
     * @return future completed with the number of entries.
     */
    Future<Integer> size();

    /**
     * Get the context for the storage.
     *
     * @return a storage context.
     */
    StorageContext<Value> context();

    /**
     * @param field the path to the attribute to index, must include the array token in
     *              #{@link com.codingchili.core.configuration.CoreStrings#STORAGE_ARRAY}.
     */
    void addIndex(String field);

    /**
     * initialize the construction of a query.
     *
     * @return a builder String for constructing the query.
     */
    QueryBuilder<Value> query();

    /**
     * Creates a new query on the specified attribute.
     *
     * @param attribute the attribute to query.
     * @return a new query builder.
     */
    default QueryBuilder<Value> query(String attribute) {
        return query().on(attribute);
    }
}
