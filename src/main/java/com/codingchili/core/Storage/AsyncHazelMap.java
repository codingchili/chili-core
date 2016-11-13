package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.StorageContext;

/**
 * @author Robin Duda
 *         <p>
 *         Initializes a new Hazel async map.
 */
public class AsyncHazelMap<Key, Value> extends AsyncStorageBase<Key, Value> {
    /**
     * Initializes a new hazel async map.
     *
     * @param context the context requesting the map to be created.
     * @param future  called when the map is created.
     */
    public AsyncHazelMap(Future<AsyncStorage> future, StorageContext context) {
        context.vertx().sharedData().<Key, Value>getClusterWideMap(context.DB(), cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();
                future.complete(this);
            } else {
                future.fail(cluster.cause());
            }
        });
    }
}
