package com.codingchili.core.Storage;

import io.vertx.core.shareddata.AsyncMap;

/**
 * @author Robin Duda
 *
 * Reuses the AsyncMap interface from hazelcast.
 */
public interface AsyncStorage<Key, Value> extends AsyncMap<Key, Value> {
}
