package com.codingchili.core.Storage;

import io.vertx.core.*;

import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.Context.StorageContext;


/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncPrivateMap<Key, Value> implements AsyncStorage<Key, Value> {
    private ConcurrentHashMap<Key, Value> map = new ConcurrentHashMap<>();
    private StorageContext context;

    public AsyncPrivateMap(StorageContext context) {
        this.context = context;
    }

    public AsyncPrivateMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        handler.handle(Future.succeededFuture(map.get(key)));
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.put(key, value);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        if (map.containsKey(key)){
            handler.handle(Future.succeededFuture(map.get(key)));
        } else {
            map.put(key, value);
            handler.handle(Future.succeededFuture());
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);
        context.timer(ttl, event -> remove(key, (result) -> {}));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        handler.handle(Future.succeededFuture(map.remove(key)));
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        Value current = map.get(key);

        if (current != null && current.equals(value)) {
            map.remove(key);
            handler.handle(Future.succeededFuture(true));
        } else {
            handler.handle(Future.succeededFuture(false));
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Value previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
        }

        handler.handle(Future.succeededFuture(previous));
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        Value current = map.get(key);

        if (current != null && current.equals(oldValue)) {
            map.put(key, newValue);
            handler.handle(Future.succeededFuture(true));
        } else {
            handler.handle(Future.succeededFuture(false));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(Future.succeededFuture(map.size()));
    }
}
