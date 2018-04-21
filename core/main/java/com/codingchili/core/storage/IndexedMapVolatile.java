package com.codingchili.core.storage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import io.vertx.core.Future;

import com.codingchili.core.context.StorageContext;

/**
 * @author Robin Duda
 *         <p>
 *         A storage implementation that is local and indexed. Always use this when using queries.
 *         The indexing is fully based on CQEngine. see http://github.com/npgall/cqengine
 *         The db/collection is shared over multiple instances.
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
    }

    @Override
    protected void addIndexesForAttribute(Attribute<Value, String> attribute) {
        db.addIndex(NavigableIndex.onAttribute(attribute));
        db.addIndex(RadixTreeIndex.onAttribute(attribute));
    }
}
