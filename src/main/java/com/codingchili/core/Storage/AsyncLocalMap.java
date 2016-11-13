package com.codingchili.core.Storage;

import io.vertx.core.*;

import java.util.HashMap;

import com.codingchili.core.Context.CoreContext;

/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncLocalMap<Key, Value> implements AsyncStorage<Key, Value> {
    private HashMap<Key, Value> map = new HashMap<>();
    private CoreContext context;

    public AsyncLocalMap(CoreContext context) {
        this.context = context;
    }

    public AsyncLocalMap(Future<AsyncStorage<Key, Value>> future, CoreContext context, String db) {
        this.context = context;
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler).complete(map.get(key));
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        map.put(key, value);
        future.setHandler(handler).complete();
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler);

        if (map.containsKey(key)){
            future.complete(map.get(key));
        } else {
            map.put(key, value);
            future.complete(null);
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler);

        future.complete(map.remove(key));
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> future = Future.future();
        future.setHandler(handler);

        Value previous = map.get(key);

        if (previous != null && previous.equals(value)) {
            future.complete(true);
        } else {
            future.complete(false);
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler);

        Value previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
        }

        future.complete(previous);
    }

    @Override
    public void replaceIfPresent(Key key, Value value, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> future = Future.future();
        future.setHandler(handler);

        Value current = map.get(key);

        if (current != null && current.equals(value)) {
            map.put(key, newValue);
            future.complete(true);
        } else {
            future.complete(false);
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear();
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
