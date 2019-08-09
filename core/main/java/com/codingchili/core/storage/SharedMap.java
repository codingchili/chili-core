package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;

import java.util.stream.Stream;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * Storage implementation that uses vertx local-shared map.
 * <p>
 * This storage implementation implements a fallback for supporting queries.
 * When querying, all fields in the store are converted to json.
 * This is very inefficient, if query support is required use another implementation.
 */
public class SharedMap<Value extends Storable> implements AsyncStorage<Value> {
    private StorageContext<Value> context;
    private LocalMap<String, Value> map;

    /**
     * Creates a shared vertx map that is thread safe and can be concurrently accessed from
     * multiple workers/verticles. It's recommended to use the storage loader to instantiate it.
     *
     * @param future  completed when the storage is ready.
     * @param context the storage context contains metadata about the stored objects.
     */
    public SharedMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.map = context.vertx().sharedData().getLocalMap(context.database() + "." + context.collection());
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
    public void contains(String key, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(result(map.containsKey(key)));
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        map.put(value.getId(), value);
        handler.handle(FutureHelper.result());
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (map.putIfAbsent(value.getId(), value) == null) {
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new ValueAlreadyPresentException(value.getId())));
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
        if (map.replace(value.getId(), value) != null) {
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToUpdateException(value.getId())));
        }
    }

    @Override
    public void values(Handler<AsyncResult<Stream<Value>>> handler) {
        handler.handle(Future.succeededFuture(map.values().stream()));
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
    public QueryBuilder<Value> query() {
        return new StreamQuery<>(this, () -> map.values().stream()).query();
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    @Override
    public void addIndex(String field) {
        // no-op.
    }
}