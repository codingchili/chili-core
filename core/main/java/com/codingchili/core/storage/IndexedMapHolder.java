package com.codingchili.core.storage;

import com.googlecode.cqengine.IndexedCollection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A holder class that holds references to shared data between instantiated storages.
 */
public class IndexedMapHolder<Value> {
    public final Set<String> indexed = new HashSet<>(Collections.singleton(Storable.idField));
    public final IndexedCollection<Value> db;

    public IndexedMapHolder(IndexedCollection<Value> db) {
        this.db = db;
    }
}
