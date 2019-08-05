package com.codingchili.core.storage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.disk.DiskIndex;
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

    static {
        // as indexes are not loaded by cqengine on startup, objects added before the
        // call to addIndex (when the index already exists) does not index any attributes.
        // this leads to objects not being found and cannot be removed/updated.
        //
        // this happens when the application is restarted and add is called before any queries are made.
        // this incurs a performance penalty, as the index will be rebuilt when the application is restarted.
        System.setProperty("cqengine.reinit.preexisting.indexes", Boolean.TRUE.toString());
    }

    public IndexedMapPersisted(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super((idField) -> {
            synchronized (IndexedMapPersisted.class) {

                File file = new File(dbPath(context));
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
                }
                return new ConcurrentIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));
            }
        }, context);

        future.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }

    @Override
    protected void addIndexesForAttribute(Attribute<Value, String> attribute) {
        db.addIndex(DiskIndex.onAttribute(attribute));
    }
}
