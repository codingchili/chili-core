package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 * <p>
 * Adds disk persistence to IndexedMap.
 */
public class IndexedMapPersisted<Value extends Storable> extends IndexedMap<Value> {

    public IndexedMapPersisted(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super(future, context);
    }

    @Override
    protected SharedIndexCollection<Value> getImplementation(
            StorageContext<Value> ctx, SimpleAttribute<Value, String> attribute) {
        return SharedIndexCollection.onDisk(ctx, attribute);
    }

}
