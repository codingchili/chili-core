package com.codingchili.core.Storage;

import io.vertx.core.*;

import java.util.HashMap;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.StorageContext;

/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncLocalMap<String, Value> implements AsyncStorage<String, Value> {
    private HashMap<String, Value> map = new HashMap<>();
    private StorageContext context;

    public AsyncLocalMap(StorageContext context) {
        this.context = context;
    }

    public AsyncLocalMap(Future<AsyncStorage<String, Value>> future, StorageContext<Value> context) {
        this.context = context;
        future.complete(this);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler).complete(map.get(key));
    }

    @Override
    public void put(String key, Value value, Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        map.put(key, value);
        future.setHandler(handler).complete();
    }

    @Override
    public void put(String key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void putIfAbsent(String key, Value value, Handler<AsyncResult<Value>> handler) {
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
    public void putIfAbsent(String key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler);

        future.complete(map.remove(key));
    }

    @Override
    public void removeIfPresent(String key, Value value, Handler<AsyncResult<Boolean>> handler) {
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
    public void replace(String key, Value value, Handler<AsyncResult<Value>> handler) {
        Future<Value> future = Future.future();
        future.setHandler(handler);

        Value previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
        }

        future.complete(previous);
    }

    @Override
    public void replaceIfPresent(String key, Value value, Value newValue, Handler<AsyncResult<Boolean>> handler) {
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
