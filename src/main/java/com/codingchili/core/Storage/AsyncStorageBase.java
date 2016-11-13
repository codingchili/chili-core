package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.shareddata.AsyncMap;

/**
 * @author Robin Duda
 *
 * Implements the basic get/put operations for the AsyncStorage modules.
 */
abstract class AsyncStorageBase<Key, Value> implements AsyncStorage<Key, Value> {
    protected AsyncMap<Key, Value> map;

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        map.get(key, handler);
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.put(key, value, handler);
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        map.put(key, value, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        map.putIfAbsent(key, value, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        map.putIfAbsent(key, value, ttl, handler);
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        map.remove(key, handler);
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        map.removeIfPresent(key, value, handler);
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        map.replace(key, value, handler);
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        map.replaceIfPresent(key, oldValue, newValue, handler);
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear(handler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        map.size(handler);
    }
}
