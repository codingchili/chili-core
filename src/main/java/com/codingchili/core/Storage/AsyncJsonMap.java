package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

import com.codingchili.core.Context.StorageContext;
import com.codingchili.core.Files.JsonFileStore;

/**
 * @author Robin Duda
 *         <p>
 *         Map backed by a json-file.
 */
public class AsyncJsonMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final String ASYNCJSONMAP_WORKERS = "asyncjsonmap.workers";
    private final WorkerExecutor fileWriter;
    private JsonObject db = new JsonObject();
    private StorageContext<Value> context;

    public AsyncJsonMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.fileWriter = context.vertx().createSharedWorkerExecutor(ASYNCJSONMAP_WORKERS);
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        Optional<Value> value = get(key);

        if (value.isPresent()) {
            handler.handle(Future.succeededFuture(value.get()));
        } else {
            handler.handle(Future.succeededFuture());
        }
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        put(key, value);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            handler.handle(Future.succeededFuture(current.get()));
        } else {
            put(key, value);
            handler.handle(Future.succeededFuture());
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);
        context.timer(ttl, event -> remove(key));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            handler.handle(Future.succeededFuture(current.get()));
            remove(key);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent() && current.get().equals(value)) {
            remove(key);
            handler.handle(Future.succeededFuture(true));
        } else {
            handler.handle(Future.succeededFuture(false));
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            handler.handle(Future.succeededFuture(current.get()));
        } else {
            put(key, value);
            handler.handle(Future.succeededFuture());
        }
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {

            if (current.get().equals(oldValue)) {
                put(key, newValue);
                handler.handle(Future.succeededFuture(true));
            } else {
                handler.handle(Future.succeededFuture(false));
            }

        } else {
            handler.handle(Future.succeededFuture(false));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        db.clear();
        handler.handle(Future.succeededFuture());
        save();
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(Future.succeededFuture(db.size()));
    }

    private Optional<Value> get(Key key) {
        Value value = context.toValue(db.getJsonObject(key.toString()));

        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }

    private void put(Key key, Value value) {
        db.put(key.toString(), context.toJson(value));
        save();
    }

    private void remove(Key key) {
        db.remove(key.toString());
        save();
    }

    private void save() {
        fileWriter.executeBlocking(execute -> {
            JsonFileStore.writeObject(db, context.DB());
        }, true, result -> {
        });
    }
}
