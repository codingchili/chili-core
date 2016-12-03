package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;

import java.util.*;
import java.util.stream.Collectors;

import com.codingchili.core.context.*;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * @author Robin Duda
 *         <p>
 *         Storage implementation that uses vertx local-shared map.
 */
public class SharedMap<Key, Value> extends BaseFilter<Value> implements AsyncStorage<Key, Value> {
    private StorageContext<Value> context;
    private LocalMap<Key, Value> map;

    public SharedMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.map = context.vertx().sharedData().getLocalMap(context.DB() + "." + context.collection());
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        Value value = map.get(key);

        if (value != null) {
            handler.handle(result(value));
        } else {
            handler.handle(error(new ValueMissingException(key)));
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
        if (map.putIfAbsent(key, value) == null) {
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new ValueAlreadyPresentException(key)));
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
        Value value = map.remove(key);

        if (value == null) {
            handler.handle(error(new NothingToRemoveException(key)));
        } else {
            handler.handle(FutureHelper.result());
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        if (map.replace(key, value) != null) {
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
    public void queryExact(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(result(map.values().stream()
                .filter(item -> queryExact(item, attribute, comparable))
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