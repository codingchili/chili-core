package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * @author Robin Duda
 *         <p>
 *         Storage implementation that uses vertx local-shared map.
 *         <p>
 *         This storage implementation implements a fallback for supporting queries.
 *         When querying, all fields in the store are converted to json.
 *         This is very inefficient, if query support is required use another implementation.
 */
public class SharedMap<Value extends Storable> implements AsyncStorage<Value> {
    private StorageContext<Value> context;
    private LocalMap<String, Value> map;

    public SharedMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.map = context.vertx().sharedData().getLocalMap(context.DB() + "." + context.collection());
        future.complete(this);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Value value = map.get(key);

        if (value != null) {
            handler.handle(result(value));
        } else {
            handler.handle(error(new ValueMissingException(key)));
        }
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        map.put(value.id(), value);
        handler.handle(FutureHelper.result());
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (map.putIfAbsent(value.id(), value) == null) {
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new ValueAlreadyPresentException(value.id())));
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        Value value = map.remove(key);

        if (value == null) {
            handler.handle(error(new NothingToRemoveException(key)));
        } else {
            handler.handle(FutureHelper.result());
        }
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        if (map.replace(value.id(), value) != null) {
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToReplaceException(value.id())));
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
    public QueryBuilder<Value> query(String field) {
        return new JsonStreamQuery<>(this, () -> map.values().stream().map(context::toJson)).query(field);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }
}