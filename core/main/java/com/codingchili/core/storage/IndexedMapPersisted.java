package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.googlecode.cqengine.TransactionalIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.codegen.AttributeBytecodeGenerator;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

/**
 * @author Robin Duda
 * <p>
 * Adds disk persistence to IndexedMap.
 * <p>
 * Overrides some methods where the underlying implementation differs between
 * on heap and on disk indexes.
 * <p>
 * The update method for disk persistence cannot be trusted.
 * It only replaces an existing version by using the objects serialized form as its
 * composite PK.
 */
public class IndexedMapPersisted<Value extends Storable> implements IndexedMap<Value> {
    private final SimpleAttribute<Value, String> FIELD_ID;
    private TransactionalIndexedCollection<Value> db;
    private Map<String, ? extends Attribute<Value, ?>> attributes;
    private StorageContext<Value> context;

    public IndexedMapPersisted(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        FIELD_ID = attribute(context.valueClass(), String.class, Storable.idField, Storable::id);

        db = new TransactionalIndexedCollection<>(context.valueClass(), DiskPersistence.onPrimaryKeyInFile(
                FIELD_ID, new File(dbPath(context))
        ));

        attributes = AttributeBytecodeGenerator.createAttributes(context.valueClass());

        // todo implement sharing, todo synchronize file creation as done in SharedIndexCollection prev.

        future.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }


    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(blocking -> {
            get(value.id(), get -> {
                if (get.succeeded()) {
                    db.update(Collections.singleton(get.result()), Collections.singleton(value));
                    blocking.complete();
                } else {
                    blocking.fail(new NothingToUpdateException(value.id()));
                }
            });
        }, handler);
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(Future.succeededFuture(db));
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

    }

    @Override
    public QueryBuilder<Value> query(String attribute) {
        return new IndexedMapQuery<>(this, attribute);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }
}
