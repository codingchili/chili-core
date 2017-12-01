package com.codingchili.core.storage;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.query.option.QueryOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.*;
import java.util.function.Function;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.exception.*;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static io.vertx.core.Future.*;

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

    protected final SimpleAttribute<Value, String> FIELD_ID;
    protected final StorageContext<Value> context;
    protected final IndexedCollection<Value> db;

    private IndexedMapHolder<Value> holder;
    private Function<Value, Value> mapper = (value) -> value;

    @SuppressWarnings("unchecked")
    public IndexedMap(Function<SimpleAttribute<Value, String>, IndexedCollection<Value>> supplier,
                      StorageContext<Value> context) {

        this.context = context;
        FIELD_ID = attribute(context.valueClass(), String.class, Storable.idField, Storable::id);

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

    public Attribute<Value, String> getAttribute(String fieldName, boolean multiValue) {
        if (holder.attributes.containsKey(fieldName)) {
            return holder.attributes.get(fieldName);
        } else {
            Attribute<Value, String> attribute;

            if (multiValue) {
                attribute = new MultiValueAttribute<Value, String>((Class<Value>) Generic.class, String.class, fieldName) {
                    @Override
                    public Iterable<String> getValues(Value indexing, QueryOptions queryOptions) {
                        return Serializer.getValueByPath(indexing, fieldName).stream()
                                .map(item -> (item + ""))::iterator;
                    }
                };
            } else {
                attribute = attribute(fieldName, (indexing) ->
                        (Serializer.getValueByPath(indexing, fieldName).iterator().next() + ""));
            }
            holder.attributes.put(fieldName, attribute);
            return attribute;
        }
    }

    @SuppressWarnings("unchecked")
    public void createIndex(String fieldName, boolean multiValued) {

        if (!holder.indexed.contains(fieldName)) {
            synchronized (holder.indexed) {
                if (!holder.indexed.contains(fieldName)) {
                    Attribute<Value, String> attribute = getAttribute(fieldName, multiValued);
                    holder.attributes.put(fieldName, attribute);
                    holder.db.addIndex(NavigableIndex.onAttribute(attribute));
                    holder.db.addIndex(RadixTreeIndex.onAttribute(attribute));
                    holder.indexed.add(fieldName);
                }
            }
        }
    }

    /**
     * @param mapper a mapper that is executed on all values returned from the map.
     */
    public void setMapper(Function<Value, Value> mapper) {
        this.mapper = mapper;
    }

    /**
     * when creating an index on a multivalued attribute a reflective operation is invoked.
     * This reflective invocation fails since Value is of generic type. To circumvent this,
     * a class that implements Storable, which is the common interface with Value is used.
     */
    private abstract class Generic implements Storable {
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        context.blocking(blocking -> {
            Iterator<Value> result = db.retrieve(equal(FIELD_ID, key)).iterator();
            if (result.hasNext()) {
                blocking.complete(mapper.apply(result.next()));
            } else {
                blocking.fail(new ValueMissingException(key));
            }
        }, handler);
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(blocking -> get(value.id(), get -> {
            if (get.succeeded()) {
                db.remove(get.result());
            }
            db.add(mapper.apply(value));
            blocking.complete();
        }), handler);
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        context.<Boolean>blocking(blocking -> {
            blocking.complete(db.contains(value));
        }, done -> {
            if (done.result()) {
                handler.handle(failedFuture(new ValueAlreadyPresentException(value.id())));
            } else {
                put(value, handler);
            }
        });
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        get(key, got -> {
            context.blocking(blocking -> {
                if (got.succeeded() && db.remove(got.result())) {
                    blocking.complete();
                } else {
                    blocking.fail(new NothingToRemoveException(key));
                }
            }, handler);
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        get(value.id(), get -> {
            context.blocking(blocking -> {
                if (get.succeeded()) {
                    db.remove(get.result());
                    db.add(value);
                    blocking.complete();
                } else {
                    blocking.fail(new NothingToUpdateException(value.id()));
                }
            }, handler);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(succeededFuture(db));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        context.blocking(blocking -> {
            db.clear();
            blocking.complete();
        }, handler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        context.blocking(blocking -> {
            blocking.complete(db.size());
        }, handler);
    }

    @Override
    public QueryBuilder<Value> query(String attribute) {
        return new IndexedMapQuery<>(this, attribute).setMapper(mapper);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }
}
