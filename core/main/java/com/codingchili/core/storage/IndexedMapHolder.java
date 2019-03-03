package com.codingchili.core.storage;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;

import java.util.*;

/**
 * A holder class that holds references to shared data between instantiated storages.
 */
public class IndexedMapHolder<Value> {
    public final Set<String> indexed = new HashSet<>(Collections.singleton(Storable.idField));
    public final IndexedCollection<Value> db;
    public Map<String, Attribute<Value, String>> attributes = new HashMap<>();

    public IndexedMapHolder(IndexedCollection<Value> db) {
        this.db = db;
    }
}
