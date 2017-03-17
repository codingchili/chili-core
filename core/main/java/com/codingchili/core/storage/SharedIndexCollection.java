package com.codingchili.core.storage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Set;

/**
 * @author Robin Duda
 *
 * Keeps track of which fields are already indexed on shared instances of maps.
 */
public class SharedIndexCollection<Value> extends ConcurrentIndexedCollection<Value> {
    private Set<String> indexed = new ConcurrentHashSet<>();

    public boolean isIndexed(String field) {
        return indexed.contains(field);
    }

    public void setIndexed(String field) {
        this.indexed.add(field);
    }
}
