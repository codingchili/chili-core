package com.codingchili.core.storage;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.codingchili.core.configuration.CoreStrings.EXT_JSON;
import static com.codingchili.core.configuration.CoreStrings.getFileReadError;
import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
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
    private static final Map<String, JsonDatabase<?>> maps = new ConcurrentHashMap<>();
    private static final AtomicBoolean dirty = new AtomicBoolean(false);
    private final WorkerExecutor fileWriter;
    private final StorageContext<Value> context;
    private JsonDatabase<Value> db;

    /**
     * Creates a new possibly shared instance of the JsonMap storage plugin. It's recommended
     * to use the storage loader instead of invoking this constructor.
     *
     * @param promise completed when the storage is loaded and ready.
     * @param context contains metadata about the stored objects.
     */
    @SuppressWarnings("unchecked")
    public JsonMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        this.context = context;
        var logger = context.logger(getClass());
        var path = dbPath();

        synchronized (JsonMap.class) {
            if (maps.containsKey(context.identifier())) {
                this.db = (JsonDatabase<Value>) maps.get(context.identifier());
            } else {
                try {
                    this.db = new JsonDatabase<>(context.valueClass(), path);
                } catch (NoSuchResourceException e) {
                    logger.log(getFileReadError(path));
                }
                maps.put(context.identifier(), db);
            }
        }
        this.fileWriter = context.vertx().createSharedWorkerExecutor(JSONMAP_WORKERS);
        this.enableSave();
        promise.complete(this);
    }

    private String dbPath() {
        if (!context.collection().contains(".")) {
            context.setCollection(context.collection() + EXT_JSON);
        }

        return String.format("%s/%s", context.database(), context.collection());
    }

    private void enableSave() {
        TimerSource timer = TimerSource.of(context.storage()::getPersistInterval)
                .setName(context.identifier());

        context.periodic(timer, event -> {
            if (dirty.get()) {
                save();
                dirty.set(false);
            }
        });
    }

    @Override
    public Future<Value> get(String key) {
        Optional<Value> value = find(key);

        if (value.isPresent()) {
            return result(value.get());
        } else {
            return error(new ValueMissingException(key));
        }
    }

    @Override
    public Future<Boolean> contains(String key) {
        return result(db.containsKey(key));
    }

    @Override
    public Future<Void> put(Value value) {
        store(value);
        return FutureHelper.result();
    }

    @Override
    public Future<Void> putIfAbsent(Value value) {
        Optional<Value> current = find(value.getId());

        if (current.isPresent()) {
            return error(new ValueAlreadyPresentException(value.getId()));
        } else {
            store(value);
            return FutureHelper.result();
        }
    }

    @Override
    public Future<Void> remove(String key) {
        Optional<Value> current = find(key);

        if (current.isPresent()) {
            delete(key);
            return FutureHelper.result();
        } else {
            return error(new NothingToRemoveException(key));
        }
    }

    @Override
    public Future<Void> update(Value value) {
        Optional<Value> current = find(value.getId());

        if (current.isPresent()) {
            store(value);
            return FutureHelper.result();
        } else {
            return error(new NothingToUpdateException(value.getId()));
        }
    }

    @Override
    public Future<Stream<Value>> values() {
        return context.blocking(() -> db.stream().map(Map.Entry::getValue));
    }

    @Override
    public Future<Void> clear() {
        db.clear();
        dirty();
        return FutureHelper.result();
    }

    @Override
    public QueryBuilder<Value> query() {
        return new StreamQuery<>(this, () -> db.stream()
                .map(Map.Entry::getValue))
                .query();
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    @Override
    public void addIndex(String field) {
        // no-op.
    }

    @Override
    public Future<Integer> size() {
        return result(db.size());
    }

    private Optional<Value> find(String key) {
        Value value = db.get(key);

        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }

    private void store(Value value) {
        db.put(value.getId(), value);
        dirty();
    }

    private void delete(String key) {
        db.remove(key);
        dirty();
    }

    private void save() {
        if (context.storage().isPersisted()) {
            fileWriter.<Void>executeBlocking(() -> {
                ConfigurationFactory.writeObject(db.toJson(), dbPath());
                return null;
            }, true).onComplete(result -> {});
        }
    }

    private void dirty() {
        dirty.set(true);
    }
}