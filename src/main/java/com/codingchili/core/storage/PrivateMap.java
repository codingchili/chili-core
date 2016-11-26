package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.failed;
import static com.codingchili.core.context.FutureHelper.succeeded;


/**
 * @author Robin Duda
 *
 * Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class PrivateMap<Key, Value> implements AsyncStorage<Key, Value> {
    private ConcurrentHashMap<Key, Value> map = new ConcurrentHashMap<>();
    private StorageContext context;

    public PrivateMap(StorageContext context) {
        this.context = context;
    }

    public PrivateMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        Value value = map.get(key);

        if (value == null) {
            handler.handle(failed(new MissingEntityException(key)));
        } else {
            handler.handle(succeeded(value));
        }
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.put(key, value);
        handler.handle(succeeded());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key, (removed) -> {}));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(key)){
            handler.handle(failed(new ValueAlreadyPresentException(key)));
        } else {
            map.put(key, value);
            handler.handle(succeeded());
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> remove(key, (removed) -> {}));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(key)) {
            map.remove(key);
            handler.handle(succeeded());
        } else {
            handler.handle(failed(new NothingToRemoveException(key)));
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        Value previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
            handler.handle(succeeded());
        } else {
            handler.handle(failed(new NothingToReplaceException(key)));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear();
        handler.handle(succeeded());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(succeeded(map.size()));
    }

    @Override
    public void queryExact(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler) {

    }

    @Override
    public void querySimilar(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler) {

    }

    @Override
    public void queryRange(int from, int to, Handler<AsyncResult<List<Value>>> handler, String... attributes) {

    }
}
