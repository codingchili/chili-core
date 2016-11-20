package com.codingchili.core.Storage;

import io.vertx.core.*;

import java.util.HashMap;

import com.codingchili.core.Context.StorageContext;


/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncElasticMap<Key, Value> implements AsyncStorage<Key, Value> {

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {

    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {

    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {

    }
}
