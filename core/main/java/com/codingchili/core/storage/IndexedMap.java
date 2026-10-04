package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;
import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static io.vertx.core.Future.succeededFuture;

/**
 * Implementation of the in-memory/disk indexed collections using CQEngine.
 * <p>
 * Common base class used for both in-memory and disk storage.
 *
 * @param <Value> the value that is stored in the collection.
 */
public abstract class IndexedMap<Value extends Storable> implements AsyncStorage<Value> {
    // new mode of attribute generation to avoid json serialization: does not yet work with nested objects.
    private static final Map<String, IndexedMapHolder> maps = new HashMap<>();
    private static final String INDEXED_MAP_TX = "indexedmap-tx-%s-%s";

    protected final SimpleAttribute<Value, String> FIELD_ID;
    protected final StorageContext<Value> context;
    protected final IndexedCollection<Value> db;

    private final WorkerExecutor executor;
    private final IndexedMapHolder<Value> holder;
    private Function<Value, Value> mapper = Function.identity();

    @SuppressWarnings("unchecked")
    public IndexedMap(Function<SimpleAttribute<Value, String>, IndexedCollection<Value>> supplier,
                      StorageContext<Value> context) {

        this.context = context;
        this.executor = context.vertx().createSharedWorkerExecutor(
                String.format(INDEXED_MAP_TX, context.database(), context.collection()), 1
        );

        FIELD_ID = attribute(context.valueClass(), String.class, Storable.idField, Storable::getId);

        // share collections that share the same identifier.
        synchronized (maps) {
            if (maps.containsKey(context.identifier())) {
                holder = maps.get(context.identifier());
            } else {
                holder = new IndexedMapHolder<>(supplier.apply(FIELD_ID));
                maps.put(context.identifier(), holder);
            }
            db = holder.db;
        }
    }

    public IndexedCollection<Value> getDatabase() {
        return db;
    }

    @SuppressWarnings("unchecked")
    public Attribute<Value, String> getAttribute(String fieldName) {
        return AttributeRegistry.get(context.valueClass(), fieldName);
    }

    @Override
    public void addIndex(String fieldName) {
        fieldName = fieldName.replace(STORAGE_ARRAY, "");

        if (!holder.indexed.contains(fieldName)) {
            synchronized (maps) {
                if (!holder.indexed.contains(fieldName)) {
                    try {
                        var attribute = getAttribute(fieldName);
                        addIndexesForAttribute(attribute);
                    } catch (Throwable e) {
                        context.logger(getClass()).onError(e);
                    } finally {
                        // only attempt to add the index once.
                        holder.indexed.add(fieldName);
                    }
                }
            }
        }
    }

    protected <T> Future<T> blocking(Handler<Promise<T>> blocking) {
        var inner = Promise.<T>promise();
        return executor.executeBlocking(() -> {
                    blocking.handle(inner);
                    return inner.future();
                }, false)
                .compose(outer -> outer);
    }

    /**
     * @param attribute the attribute to add an index for based on implementation.
     */
    protected abstract void addIndexesForAttribute(Attribute<Value, ?> attribute);

    /**
     * @param mapper a mapper that is executed on all values returned from the map.
     */
    public void setMapper(Function<Value, Value> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Future<Value> get(String key) {
        return blocking(blocking -> {
            try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, key))) {
                if (result.isNotEmpty()) {
                    blocking.complete(mapper.apply(result.iterator().next()));
                } else {
                    blocking.fail(new ValueMissingException(key));
                }
            }
        });
    }

    @Override
    public Future<Void> put(Value value) {
        return blocking(blocking -> {
            try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, value.getId()))) {
                if (result.isEmpty()) {
                    db.add(mapper.apply(value));
                    blocking.complete();
                } else {
                    boolean updated = db.update(Collections.singleton(result.iterator().next()),
                            Collections.singleton(mapper.apply(value)));

                    if (updated) {
                        blocking.complete();
                    } else {
                        blocking.fail(new NothingToRemoveException(value.getId()));
                    }
                }
            }
        });
    }

    @Override
    public Future<Void> putIfAbsent(Value value) {
        return blocking(blocking -> {
            try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, value.getId()))) {
                if (result.isNotEmpty()) {
                    blocking.fail(new ValueAlreadyPresentException(value.getId()));
                } else {
                    db.add(mapper.apply(value));
                    blocking.complete();
                }
            }
        });
    }

    @Override
    public Future<Void> remove(String key) {
        return blocking(blocking -> {
            try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, key))) {
                if (result.isNotEmpty()) {
                    result.stream().forEach(entry -> {
                        if (!db.remove(entry)) {
                            blocking.tryFail(new NothingToRemoveException(key));
                        }
                    });
                    blocking.tryComplete();
                } else {
                    blocking.fail(new NothingToRemoveException(key));
                }
            }
        });
    }

    @Override
    public Future<Void> update(Value value) {
        return blocking(blocking -> {
            try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, value.getId()))) {
                if (result.isNotEmpty()) {
                    boolean updated = db.update(Collections.singleton(result.iterator().next()), Collections.singleton(mapper.apply(value)));

                    if (updated) {
                        blocking.complete();
                    } else {
                        blocking.fail(new NothingToRemoveException(value.getId()));
                    }
                } else {
                    blocking.fail(new NothingToUpdateException(value.getId()));
                }
            }
        });
    }

    @Override
    public Future<Stream<Value>> values() {
        return succeededFuture(db.stream());
    }

    @Override
    public Future<Void> clear() {
        return blocking(blocking -> {
            db.clear();
            blocking.complete();
        });
    }

    @Override
    public Future<Integer> size() {
        return blocking(blocking -> blocking.complete(db.size()));
    }

    @Override
    public QueryBuilder<Value> query() {
        return new IndexedMapQuery<>(this).setMapper(mapper);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }
}
