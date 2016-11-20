package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.StorageContext;

/**
 * @author Robin Duda
 */
public class AsyncSharedMap<Key, Value> implements AsyncStorage<Key, Value> {
    private CoreContext context;
    private LocalMap<Key, Value> map;

    public AsyncSharedMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.map = context.vertx().sharedData().getLocalMap(context.DB());
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
        handler.handle(Future.succeededFuture(map.putIfAbsent(key, value)));
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
        handler.handle(Future.succeededFuture(map.removeIfPresent(key, value)));
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        handler.handle(Future.succeededFuture(map.remove(key)));
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(Future.succeededFuture(map.replaceIfPresent(key, oldValue, newValue)));
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