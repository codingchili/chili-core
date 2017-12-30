package com.codingchili.core.storage;

import com.googlecode.cqengine.*;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import io.vertx.core.Future;

import java.io.File;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 *         <p>
 *         Adds disk persistence to IndexedMap.
 *         <p>
 *         Overrides some methods where the underlying implementation differs between
 *         on heap and on disk indexes.
 *         <p>
 *         The update method for disk persistence cannot be trusted.
 *         It only replaces an existing version by using the objects serialized form as its
 *         composite PK.
 */
public class IndexedMapPersisted<Value extends Storable> extends IndexedMap<Value> {

    public IndexedMapPersisted(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super((idField) -> {
            synchronized (IndexedMapPersisted.class) {

                File file = new File(dbPath(context));
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
                }
                IndexedCollection<Value> db = new ObjectLockingIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));

                db.addIndex(HashIndex.onSemiUniqueAttribute(idField));
                return db;
            }
        }, context);

        setMapper((value) -> Serializer.kryo((kryo) -> kryo.copy(value)));
        future.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }
}
