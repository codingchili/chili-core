package com.codingchili.core.Shared;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;

import java.util.HashMap;

/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncMapMock<K, V> implements AsyncMap<K, V> {
    private HashMap<K, V> map = new HashMap<>();

    @Override
    public void get(K key, Handler<AsyncResult<V>> handler) {
        Future<V> future = Future.future();
        future.setHandler(handler).complete(map.get(key));
    }

    @Override
    public void put(K key, V value, Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        map.put(key, value);
        future.setHandler(handler).complete();
    }

    @Override
    public void put(K key, V value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);
    }

    @Override
    public void putIfAbsent(K key, V value, Handler<AsyncResult<V>> handler) {
        Future<V> future = Future.future();
        future.setHandler(handler);

        if (map.containsKey(key)){
            future.complete(map.get(key));
        } else {
            map.put(key, value);
            future.complete(null);
        }
    }

    @Override
    public void putIfAbsent(K key, V value, long ttl, Handler<AsyncResult<V>> handler) {
        putIfAbsent(key, value, handler);
    }

    @Override
    public void remove(K key, Handler<AsyncResult<V>> handler) {
        Future<V> future = Future.future();
        future.setHandler(handler);

        future.complete(map.remove(key));
    }

    @Override
    public void removeIfPresent(K key, V value, Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> future = Future.future();
        future.setHandler(handler);

        if (map.remove(key) != null) {
            future.complete(true);
        } else {
            future.complete(false);
        }
    }

    @Override
    public void replace(K key, V value, Handler<AsyncResult<V>> handler) {
        Future<V> future = Future.future();
        future.setHandler(handler);

        V previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
        }

        future.complete(previous);
    }

    @Override
    public void replaceIfPresent(K key, V value, V newValue, Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> future = Future.future();
        future.setHandler(handler);

        V current = map.get(key);

        if (current.equals(value)) {
            map.put(key, newValue);
            future.complete(true);
        } else {
            future.complete(false);
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        future.setHandler(handler).complete();
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        future.setHandler(handler);

        future.complete(map.size());
    }
}
