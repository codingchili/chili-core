package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.List;

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

    void queryExact(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler);

    void querySimilar(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler);

    void queryRange(int from, int to, Handler<AsyncResult<List<Value>>> handler, String... attributes);
}
