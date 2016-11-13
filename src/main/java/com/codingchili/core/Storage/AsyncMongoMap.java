package com.codingchili.core.Storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *
 * MongoDB backed asyncmap.
 */
public class AsyncMongoMap<K, V> implements AsyncStorage<K, V> {
    @Override
    public void clear(Handler handler) {

    }

    @Override
    public void size(Handler handler) {

    }

    @Override
    public void get(K key, Handler<AsyncResult<V>> asyncResultHandler) {

    }

    @Override
    public void put(K key, V value, Handler<AsyncResult<Void>> completionHandler) {

    }

    @Override
    public void put(K key, V value, long ttl, Handler<AsyncResult<Void>> completionHandler) {

    }

    @Override
    public void putIfAbsent(K key, V value, Handler<AsyncResult<V>> completionHandler) {

    }

    @Override
    public void putIfAbsent(K key, V value, long ttl, Handler<AsyncResult<V>> completionHandler) {

    }

    @Override
    public void remove(K key, Handler<AsyncResult<V>> asyncResultHandler) {

    }

    @Override
    public void removeIfPresent(K key, V value, Handler<AsyncResult<Boolean>> resultHandler) {

    }

    @Override
    public void replace(K key, V value, Handler<AsyncResult<V>> asyncResultHandler) {

    }

    @Override
    public void replaceIfPresent(K key, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {

    }
}
