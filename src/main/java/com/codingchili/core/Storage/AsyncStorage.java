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

    void get(Key key, Handler<AsyncResult<Value>> handler);

    void put(Key key, Value value, Handler<AsyncResult<Void>> handler);

    void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler);

    void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler);

    void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler);

    void remove(Key key, Handler<AsyncResult<Void>> handler);

    void replace(Key key, Value value, Handler<AsyncResult<Void>> handler);

    void clear(Handler<AsyncResult<Void>> handler);

    void size(Handler<AsyncResult<Integer>> handler);

    void queryExact(String attribute, Comparable compare, Handler<AsyncResult<Collection<Value>>> handler);

    void querySimilar(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler);

    void queryRange(String attribute, int from, int to, Handler<AsyncResult<Collection<Value>>> handler);
}
