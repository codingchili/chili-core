package com.codingchili.core.storage;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.ObjectLockingIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import io.vertx.core.Future;

import java.io.File;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;

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
                IndexedCollection<Value> db = new ObjectLockingIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));

                db.addIndex(HashIndex.onSemiUniqueAttribute(idField));
                //db.addIndex(UniqueIndex.onAttribute(idField));
                return db;
            }
        }, context);

        // we perform this expensive operation to simplify clients - otherwise
        // clients would need to copy objects when using this storage and not
        // others for updates. Should probably be removed later and documented instead.
        setMapper((value) -> {
            return Serializer.kryo((kryo) -> {
                // this needs to be set for all kryo instances - new ones may be created
                // from the kryo factory when using pooling.
                @SuppressWarnings("unchecked")
                FieldSerializer<Value> serializer = (FieldSerializer<Value>) kryo.getSerializer(context.valueClass());
                FieldSerializer.FieldSerializerConfig config = serializer.getFieldSerializerConfig();

                if (config.getCopyTransient()) {
                    // avoid calling updateFields if transient fields are already disabled.
                    config.setCopyTransient(false);
                    config.setSerializeTransient(false);
                    serializer.updateFields();
                }
                return kryo.copy(value);
            });
        });

        future.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }

    @Override
    protected void addIndexesForAttribute(Attribute<Value, String> attribute) {
        db.addIndex(NavigableIndex.onAttribute(attribute));
        db.addIndex(RadixTreeIndex.onAttribute(attribute));
    }
}
