package com.codingchili.core.storage;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import io.vertx.core.Future;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;

/**
 * A storage implementation that is local and indexed. Always use this when using queries.
 * The indexing is fully based on CQEngine. see http://github.com/npgall/cqengine
 * The db/collection is shared over multiple instances.
 */
public class IndexedMapVolatile<Value extends Storable> extends IndexedMap<Value> {

    @SuppressWarnings("unchecked")
    public IndexedMapVolatile(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super((idField) -> {
            IndexedCollection<Value> db = new ConcurrentIndexedCollection<>(OnHeapPersistence.onPrimaryKey(idField));
            db.addIndex(UniqueIndex.onAttribute(idField));
            return db;
        }, context);
        future.complete(this);

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
    }

    @Override
    protected void addIndexesForAttribute(Attribute<Value, String> attribute) {
        db.addIndex(NavigableIndex.onAttribute(attribute));
        db.addIndex(RadixTreeIndex.onAttribute(attribute));
    }
}
