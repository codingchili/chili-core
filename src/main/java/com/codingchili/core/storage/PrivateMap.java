package com.codingchili.core.storage;

import io.vertx.core.*;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;


/**
 * @author Robin Duda
 *         <p>
 *         Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class PrivateMap<Key, Value> extends BaseFilter<Value> implements AsyncStorage<Key, Value> {
    private ConcurrentHashMap<Key, Value> map = new ConcurrentHashMap<>();
    private StorageContext<Value> context;

    public PrivateMap(StorageContext<Value> context) {
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
            handler.handle(error(new ValueMissingException(key)));
        } else {
            handler.handle(result(value));
        }
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.put(key, value);
        handler.handle(FutureHelper.result());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key, (removed) -> {
        }));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(key)) {
            handler.handle(error(new ValueAlreadyPresentException(key)));
        } else {
            map.put(key, value);
            handler.handle(FutureHelper.result());
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> remove(key, (removed) -> {
        }));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(key)) {
            map.remove(key);
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToRemoveException(key)));
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        Value previous = map.get(key);

        if (previous != null) {
            map.put(key, value);
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToReplaceException(key)));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear();
        handler.handle(FutureHelper.result());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(result(map.size()));
    }

    @Override
    public void queryExact(String attribute, Comparable compare, Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(result(map.values().stream()
                .filter(item -> queryExact(item, attribute, compare))
                .collect(Collectors.toList())));
    }

    @Override
    public void querySimilar(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(result(map.values().stream()
                .filter(item -> querySimilar(item, attribute, comparable))
                .collect(Collectors.toList())));
    }

    @Override
    public void queryRange(String attribute, int from, int to, Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(result(map.values().stream()
                .filter(item -> queryRange(item, attribute, from, to))
                .collect(Collectors.toList())));
    }
}