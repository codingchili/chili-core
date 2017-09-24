package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import io.vertx.core.impl.ConcurrentHashSet;

import java.io.File;
import java.util.Set;

/**
 * @author Robin Duda
 * <p>
 * Keeps track of which fields are already indexed on shared instances of maps.
 */
public class SharedIndexCollection<Value> extends ConcurrentIndexedCollection<Value> {
    private Set<String> indexed = new ConcurrentHashSet<>();

    private SharedIndexCollection(SimpleAttribute<Value, String> attribute) {
        super(OnHeapPersistence.onPrimaryKey(attribute));
    }

    private SharedIndexCollection(StorageContext<Value> ctx, SimpleAttribute<Value, String> attribute) {
        super(DiskPersistence.onPrimaryKeyInFile(attribute, new File(ctx.dbPath())));
    }

    public static <Value extends Storable> SharedIndexCollection<Value> onHeap(
            SimpleAttribute<Value, String> attribute) {
        return new SharedIndexCollection<>(attribute);
    }

    public static <Value extends Storable> SharedIndexCollection<Value> onDisk(
            StorageContext<Value> ctx, SimpleAttribute<Value, String> attribute) {
        File file = new File(ctx.dbPath()).getParentFile();
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
        }
        return new SharedIndexCollection<>(ctx, attribute);
    }

    public boolean isIndexed(String field) {
        return indexed.contains(field);
    }

    public void setIndexed(String field) {
        this.indexed.add(field);
    }
}
