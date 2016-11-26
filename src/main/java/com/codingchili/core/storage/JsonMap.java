package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.failed;
import static com.codingchili.core.context.FutureHelper.succeeded;

/**
 * @author Robin Duda
 *         <p>
 *         Map backed by a json-file.
 */
public class JsonMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final String JSONMAP_WORKERS = "asyncjsonmap.workers";
    private final WorkerExecutor fileWriter;
    private JsonObject db;
    private StorageContext<Value> context;

    public JsonMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.fileWriter = context.vertx().createSharedWorkerExecutor(JSONMAP_WORKERS);

        try {
            db = JsonFileStore.readObject(context.DB());
        } catch (IOException e) {
            db = new JsonObject();
            context.console().log(Strings.getFileReadError(context.DB()));
        }

        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        Optional<Value> value = get(key);

        if (value.isPresent()) {
            handler.handle(succeeded(value.get()));
        } else {
            handler.handle(failed(new MissingEntityException(key)));
        }
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        put(key, value);
        handler.handle(succeeded());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, event -> remove(key));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            handler.handle(failed(new ValueAlreadyPresentException(key)));
        } else {
            put(key, value);
            handler.handle(succeeded());
        }
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, handler);
        context.timer(ttl, event -> remove(key));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            remove(key);
            handler.handle(succeeded());
            save();
        } else {
            handler.handle(failed(new NothingToRemoveException(key)));
        }
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            put(key, value);
            handler.handle(succeeded());
        } else {
            handler.handle(failed(new NothingToReplaceException(key)));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        db.clear();
        handler.handle(succeeded());
        save();
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(succeeded(db.size()));
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
