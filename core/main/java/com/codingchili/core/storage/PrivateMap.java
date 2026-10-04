package com.codingchili.core.storage;

import io.vertx.core.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;


/**
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

    public PrivateMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        this.context = context;
        promise.complete(this);
    }

    @Override
    public Future<Value> get(String key) {
        Value value = map.get(key);

        if (value == null) {
            return error(new ValueMissingException(key));
        } else {
            return result(value);
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
        if (map.containsKey(value.getId())) {
            return error(new ValueAlreadyPresentException(value.getId()));
        } else {
            map.put(value.getId(), value);
            return FutureHelper.result();
        }
    }

    @Override
    public Future<Void> remove(String key) {
        if (map.containsKey(key)) {
            map.remove(key);
            return FutureHelper.result();
        } else {
            return error(new NothingToRemoveException(key));
        }
    }

    @Override
    public Future<Void> update(Value value) {
        Value previous = map.get(value.getId());

        if (previous != null) {
            map.put(value.getId(), value);
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