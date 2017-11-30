package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.codegen.AttributeBytecodeGenerator;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.*;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * @author Robin Duda
 * <p>
 * A storage implementation that is local and indexed. Always use this when using queries.
 * The indexing is fully based on CQEngine. see http://github.com/npgall/cqengine
 * The db/collection is shared over multiple instances.
 */
public abstract class IndexedMapVolatile<Value extends Storable> implements IndexedMap<Value> {
    private static final Map<String, ConcurrentIndexedCollection> maps = new HashMap<>();
    private Map<String, ? extends Attribute<Value, ?>> attributes;
    private final ConcurrentIndexedCollection<Value> db;
    private final SimpleAttribute<Value, String> FIELD_ID;
    private final StorageContext<Value> context;

    @SuppressWarnings("unchecked")
    public IndexedMapVolatile(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        FIELD_ID = attribute(context.valueClass(), String.class, Storable.idField, Storable::id);

        attributes = AttributeBytecodeGenerator.createAttributes(context.valueClass());

        // share collections that share the same identifier.
        synchronized (maps) {
            if (maps.containsKey(context.identifier())) {
                db = maps.get(context.identifier());
            } else {
                db = new ConcurrentIndexedCollection<>(OnHeapPersistence.onPrimaryKey(FIELD_ID));
                db.addIndex(UniqueIndex.onAttribute(FIELD_ID));
                maps.put(context.identifier(), db);
            }
        }
        future.complete(this);
    }


    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Iterator<Value> result = db.retrieve(equal(FIELD_ID, key)).iterator();

        if (result.hasNext()) {
            handler.handle(succeededFuture(result.next()));
        } else {
            handler.handle(failedFuture(new ValueMissingException(key)));
        }
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(blocking -> get(value.id(), get -> {
            db.update((get.result() != null) ?
                    Collections.singleton(get.result()) : Collections.emptyList(), Collections.singleton(value));

            blocking.complete();
        }), handler);
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (!db.contains(value)) {
            put(value, handler);
        } else {
            handler.handle(failedFuture(new ValueAlreadyPresentException(value.id())));
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        get(key, got -> {
            if (got.succeeded() && db.remove(got.result())) {
                handler.handle(succeededFuture());
            } else {
                handler.handle(failedFuture(new NothingToRemoveException(key)));
            }
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        get(value.id(), get -> {
            if (get.succeeded() && db.update(Collections.singleton(get.result()), Collections.singleton(value))) {
                handler.handle(succeededFuture());
            } else {
                handler.handle(failedFuture(new NothingToUpdateException(value.id())));
            }
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
        handler.handle(succeededFuture(db.size()));
    }

    /**
     * Adds an index for the given attribute if one is not already added.
     * hack CQEngine to avoid having to specify attributes. (serialize to json, then get field value.)
     *
     * @param fieldName the field to add indexing to.
     */
    @SuppressWarnings("unchecked")
    private Attribute<Value, ?> createIndex(String fieldName, boolean multiValue) {
        Attribute<Value, ?> attribute = attributes.get(fieldName);
        // todo: only if not already indexed.
        synchronized (maps) {
            db.addIndex(NavigableIndex.onAttribute((Attribute<Value, Comparable>) attribute));
            //db.addIndex(SuffixTreeIndex.onAttribute(attribute));
            if (attribute.getAttributeType().equals(String.class)) {
                db.addIndex(RadixTreeIndex.onAttribute((Attribute<Value, String>) attribute));
            }
        }

        return attributes.get(fieldName);
    }

    @Override
    public QueryBuilder<Value> query(String attribute) {
        return new IndexedMapQuery<>(this, attribute);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    /**
     * when creating an index on a multivalued attribute a reflective operation is invoked.
     * This reflective invocation fails since Value is of generic type. To circumvent this,
     * a class that implements Storable, which is the common interface with Value is used.
     */
    private abstract class Generic implements Storable {
    }
}
