package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.StorageContext;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.disk.DiskIndex;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import io.vertx.core.Promise;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adds disk persistence to IndexedMap.
 * <p>
 * Overrides some methods where the underlying implementation differs between
 * on heap and on disk indexes.
 * <p>
 * The update method for disk persistence cannot be trusted.
 * It only replaces an existing version by using the objects serialized form as its
 * composite PK.
 */
public class IndexedMapPersisted<Value extends Storable> extends IndexedMap<Value> {
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    /**
     * Creates a possibly shared instance of the persisted IndexedMap. It is recommended
     * to not call this directly and instead use the storage loader.
     *
     * @param promise  completed when the storage is ready.
     * @param context the storage context to set up file locations etc.
     */
    public IndexedMapPersisted(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        super((idField) -> {
            synchronized (IndexedMapPersisted.class) {
                LOADED.set(true);

                File file = new File(dbPath(context));
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
                }
                return new ConcurrentIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));
            }
        }, context);

        promise.complete(this);
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }

    @Override
    public void addIndexesForAttribute(Attribute<Value, String> attribute) {
        db.addIndex(DiskIndex.onAttribute(attribute));
    }

    /**
     * Disk indexes are not discovered by CQEngine at startup. This means that CQEngine
     * will not update existing indexes unless AsyncStorage#addIndex is called before any
     * items are added.
     * <p>
     * To work around this there are two options,
     * <p>
     * a) Always add all indexes before inserting ANY items.
     * b) Call this method before loading the IndexedMapPersisted plugin. Then items
     * can be added before adding an index. Because when the index is added later it
     * will be rebuilt. In the window between adding the object and adding the index
     * the object will not be visible in the affected indexes.
     * <p>
     * If this method is called after an instance of IndexedMapPersisted has been loaded
     * through the storage loader an exception is thrown.
     */
    public static void reindex() {
        if (LOADED.get()) {
            throw new CoreRuntimeException(CoreStrings.ERROR_ALREADY_INITIALIZED);
        } else {
            System.setProperty("cqengine.reinit.preexisting.indexes", Boolean.TRUE.toString());
        }
    }
}
