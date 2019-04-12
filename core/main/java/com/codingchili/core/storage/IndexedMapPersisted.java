package com.codingchili.core.storage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import io.vertx.core.Future;

import java.io.File;

import com.codingchili.core.context.StorageContext;

/**
 * Adds disk persistence to IndexedMap.
 * <p>
 * Overrides some methods where the underlying implementation differs between
 * on heap and on disk indexes.
 * <p>
 * The update method for disk persistence cannot be trusted.
 * It only replaces an existing version by using the objects serialized form as its
 * composite PK.
 */
public class IndexedMapPersisted<Value extends Storable> extends IndexedMap<Value> {

    public IndexedMapPersisted(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super((idField) -> {
            synchronized (IndexedMapPersisted.class) {

                File file = new File(dbPath(context));
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
                }
                IndexedCollection<Value> db = new ConcurrentIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));
                return db;
            }
        }, context);

        future.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }

    @Override
    protected void addIndexesForAttribute(Attribute<Value, String> attribute) {
        // adding another disk index breaks everything - queries on attributes
        // in two different index does not work. The primary key has a separate index.
    }
}
