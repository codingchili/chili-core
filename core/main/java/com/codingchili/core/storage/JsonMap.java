package com.codingchili.core.storage;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToReplaceException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codingchili.core.configuration.CoreStrings.getFileReadError;
import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
 * @author Robin Duda
 * <p>
 * Map backed by a json-file.
 * <p>
 * Do not use for data that is changing frequently, as this is extremely inefficient.
 * The dirty-state of the map will be checked to determine when the map should be
 * persisted. This is done in intervals specified in plugin configuration.
 * <p>
 * this map flushes its contents to disk every now and then.
 */
public class JsonMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final String JSONMAP_WORKERS = "asyncjsonmap.workers";
    private static Map<String, JsonObject> maps = new ConcurrentHashMap<>();
    private static AtomicBoolean dirty = new AtomicBoolean(false);
    private WorkerExecutor fileWriter;
    private JsonObject db = new JsonObject();
    private StorageContext<Value> context;

    @SuppressWarnings("unchecked")
    public JsonMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        Logger logger = context.logger(getClass());

        if (maps.containsKey(context.identifier())) {
            this.db = maps.get(context.identifier());
        } else {
            try {
                this.db = JsonFileStore.readObject(context.dbPath());
            } catch (NoSuchResourceException e) {
                logger.log(getFileReadError(context.dbPath()));
            }
        }
        this.context = context;
        this.fileWriter = context.vertx().createSharedWorkerExecutor(JSONMAP_WORKERS);
        this.enableSave();
        future.complete(this);
    }

    private void enableSave() {
        context.periodic(() -> context.storage().getPersistInterval(),
                context.identifier(), event -> {
                    if (dirty.get()) {
                        save();
                        dirty.set(false);
                    }
                });
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Optional<Value> value = get(key);

        if (value.isPresent()) {
            handler.handle(result(value.get()));
        } else {
            handler.handle(error(new ValueMissingException(key)));
        }
    }

    @Override
    public void contains(String key, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(result(db.containsKey(key)));
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        put(value);
        handler.handle(FutureHelper.result());
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(value.id());

        if (current.isPresent()) {
            handler.handle(error(new ValueAlreadyPresentException(value.id())));
        } else {
            put(value);
            handler.handle(FutureHelper.result());
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(key);

        if (current.isPresent()) {
            remove(key);
            handler.handle(FutureHelper.result());
            dirty();
        } else {
            handler.handle(error(new NothingToRemoveException(key)));
        }
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        Optional<Value> current = get(value.id());

        if (current.isPresent()) {
            put(value);
            handler.handle(FutureHelper.result());
        } else {
            handler.handle(error(new NothingToReplaceException(value.id())));
        }
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(Future.succeededFuture(db.stream()
                .map(entry -> (JsonObject) entry.getValue())
                .map(json -> context.toValue(json))
                .collect(Collectors.toList())));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        db.clear();
        handler.handle(FutureHelper.result());
        dirty();
    }

    @Override
    public QueryBuilder<Value> query(String field) {
        return new JsonStreamQuery<>(this, this::streamSource).query(field);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    private Stream<JsonObject> streamSource() {
        return db.stream().map(entry -> (JsonObject) entry.getValue());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(result(db.size()));
    }

    private Optional<Value> get(String key) {
        JsonObject json = db.getJsonObject(key);

        if (json == null) {
            return Optional.empty();
        } else {
            return Optional.of(context.toValue(json));
        }
    }

    private void put(Value value) {
        db.put(value.id(), context.toJson(value));
        dirty();
    }

    private void remove(String key) {
        db.remove(key);
        dirty();
    }

    private void save() {
        if (context.storage().isPersisted()) {
            fileWriter.executeBlocking(execute -> {
                JsonFileStore.writeObject(db, context.dbPath());
            }, true, result -> {
            });
        }
    }

    private void dirty() {
        dirty.set(true);
    }
}