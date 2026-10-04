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
     * @param promise  completed when the storage is ready.
     * @param context the storage context contains metadata about the stored objects.
     */
    public SharedMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        this.context = context;
        this.map = context.vertx().sharedData().getLocalMap(context.database() + "." + context.collection());
        promise.complete(this);
    }

    @Override
    public Future<Value> get(String key) {
        Value value = map.get(key);

        if (value != null) {
            return result(value);
        } else {
            return error(new ValueMissingException(key));
        }
    }

    @Override
    public Future<Boolean> contains(String key) {
        return result(map.containsKey(key));
    }

    @Override
    public Future<Void> put(Value value) {
        map.put(value.getId(), value);
        return FutureHelper.result();
    }

    @Override
    public Future<Void> putIfAbsent(Value value) {
        if (map.putIfAbsent(value.getId(), value) == null) {
            return FutureHelper.result();
        } else {
            return error(new ValueAlreadyPresentException(value.getId()));
        }
    }

    @Override
    public Future<Void> remove(String key) {
        Value value = map.remove(key);

        if (value == null) {
            return error(new NothingToRemoveException(key));
        } else {
            return FutureHelper.result();
        }
    }

    @Override
    public Future<Void> update(Value value) {
        if (map.replace(value.getId(), value) != null) {
            return FutureHelper.result();
        } else {
            return error(new NothingToUpdateException(value.getId()));
        }
    }

    @Override
    public Future<Stream<Value>> values() {
        return result(map.values().stream());
    }

    @Override
    public Future<Void> clear() {
        map.clear();
        return FutureHelper.result();
    }

    @Override
    public Future<Integer> size() {
        return result(map.size());
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