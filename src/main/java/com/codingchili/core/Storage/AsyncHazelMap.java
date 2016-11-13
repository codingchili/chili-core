package com.codingchili.core.Storage;

import io.vertx.core.*;

import com.codingchili.core.Context.CoreContext;

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
     * @param name the name of the cluster-map to use.
     * @param future  called when the map is created.
     */
    public AsyncHazelMap(Future<AsyncStorage<Key, Value>> future, CoreContext context, String name) {
        context.vertx().sharedData().<Key, Value>getClusterWideMap(name, cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();
                future.complete(this);
            } else {
                future.fail(cluster.cause());
            }
        });
    }
}
