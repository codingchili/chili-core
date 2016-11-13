package com.codingchili.core.Storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *
 * Map backed by a json-file.
 */
public class AsyncJsonMap<Key, Value> implements AsyncStorage<Key, Value> {


    @Override
    public void get(Key key, Handler<AsyncResult<Value>> asyncResultHandler) {

    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> completionHandler) {

    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> completionHandler) {

    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> completionHandler) {

    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> completionHandler) {

    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> asyncResultHandler) {

    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> resultHandler) {

    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> asyncResultHandler) {

    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> resultHandler) {

    }

    @Override
    public void clear(Handler<AsyncResult<Void>> resultHandler) {

    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {

    }
}
