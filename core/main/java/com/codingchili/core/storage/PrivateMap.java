package com.codingchili.core.storage;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;


/**
 * @author Robin Duda
 * <p>
 * Implements an async map for use with local data.
 * <p>
 * This storage implementation implements a fallback for supporting queries.
 * When querying, all fields in the store are converted to json.
 * This is very inefficient, if query support is required use another implementation.
 * <p>
 * This map is private, it is not shared within the JVM.
 */
public class PrivateMap<Value extends Storable> implements AsyncStorage<Value> {
    private ConcurrentHashMap<String, Value> map = new ConcurrentHashMap<>();
    private StorageContext<Value> context;

    public PrivateMap(StorageContext<Value> context) {
        this.context = context;
    }

    public PrivateMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        future.complete(this);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Value value = map.get(key);

        if (value == null) {
            handler.handle(error(new ValueMissingException(key)));
        } else {
            handler.handle(result(value));
        }
    }

    @Override
    public void contains(String key, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(result(map.containsKey(key)));
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        map.put(value.id(), value);
        handler.handle(FutureHelper.result());
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(value.id())) {
            handler.handle(error(new ValueAlreadyPresentException(value.id())));
        } else {
            map.put(value.id(), value);
            handler.handle(FutureHelper.result());
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        if (map.containsKey(key)) {
            map.remove(key);
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToRemoveException(key)));
        }
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        Value previous = map.get(value.id());

        if (previous != null) {
            map.put(value.id(), value);
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToUpdateException(value.id())));
        }
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(Future.succeededFuture(map.values()));

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
    public QueryBuilder<Value> query(String field) {
        return new JsonStreamQuery<>(this, () -> map.values().stream().map(context::toJson)).query(field);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }
}