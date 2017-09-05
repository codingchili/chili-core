package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         Indexed map configured with on-heap storage.
 */
public class IndexedMapVolatile<Value extends Storable> extends IndexedMap<Value> {

    public IndexedMapVolatile(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        super(future, context);
    }

    @Override
    protected SharedIndexCollection<Value> getImplementation(
            StorageContext<Value> ctx, SimpleAttribute<Value, String> attribute) {
        return SharedIndexCollection.onHeap();
    }
}
