package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.logging.Level;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Loads the given storage subsystem.
 */
public class StorageLoader<Value extends Storable> {
    private CoreContext context;
    private String DB = DEFAULT_DB;
    private Class clazz;
    private String collection;
    private String plugin;

    public StorageLoader() {
    }

    public StorageLoader(CoreContext context) {
        this.context = context;
    }

    private void load(Handler<AsyncResult<AsyncStorage<Value>>> handler) {
        try {
            Future<AsyncStorage<Value>> future = Future.future();
            future.setHandler(handler);

            StorageContext<Value> storage = new StorageContext<Value>(context)
                    .setDB(DB)
                    .setCollection(collection)
                    .setClass(clazz)
                    .setPlugin(plugin);

            Class.forName(plugin)
                    .getConstructor(Future.class, StorageContext.class)
                    .<Value>newInstance(future, storage);

        } catch (ReflectiveOperationException e) {
            context.logger().log(CoreStrings.getStorageLoaderError(plugin, DB, collection), Level.SEVERE);
            e.printStackTrace();
            System.exit(0);
        }
    }

    public StorageLoader<Value> withContext(CoreContext context) {
        this.context = context;
        return this;
    }

    public StorageLoader<Value> withDB(String DB) {
        this.DB = DB;
        return this;
    }

    public StorageLoader<Value> withDB(String DB, String collection) {
        this.DB = DB;
        this.collection = collection;
        return this;
    }

    public StorageLoader<Value> withClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public StorageLoader<Value> withPlugin(Class plugin) {
        this.plugin = plugin.getCanonicalName();
        return this;
    }

    public StorageLoader<Value> withCollection(String collection) {
        this.collection = collection;
        return this;
    }

    public StorageLoader<Value> withPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public void build(Handler<AsyncResult<AsyncStorage<Value>>> future) {
        checkIsSet(context, ID_CONTEXT);
        checkIsSet(DB, ID_DB);
        checkIsSet(clazz, ID_CLASS);
        checkIsSet(plugin, ID_PLUGIN);
        checkIsSet(collection, ID_COLLECTION);

        this.load(future);
    }

    private void checkIsSet(Object object, String type) {
        if (object == null) {
            throw new RuntimeException(CoreStrings.getStorageLoaderMissingArgument(type));
        }
    }

    public StorageLoader<Value> mongodb(CoreContext context) {
        return makeWith(context, MongoDBMap.class);
    }

    public StorageLoader<Value> elasticsearch(CoreContext context) {
        return makeWith(context, ElasticMap.class);
    }

    public StorageLoader<Value> hazelcast(CoreContext context) {
        return makeWith(context, HazelMap.class);
    }

    public StorageLoader<Value> memIndex(CoreContext context) {
        return makeWith(context, IndexedMapVolatile.class);
    }

    public StorageLoader<Value> diskIndex(CoreContext context) {
        return makeWith(context, IndexedMapPersisted.class);
    }

    public StorageLoader<Value> jsonmap(CoreContext context) {
        return makeWith(context, JsonMap.class);
    }

    public StorageLoader<Value> privatemap(CoreContext context) {
        return makeWith(context, PrivateMap.class);
    }

    public StorageLoader<Value> sharedmap(CoreContext context) {
        return makeWith(context, SharedMap.class);
    }

    private StorageLoader<Value> makeWith(CoreContext context, Class plugin) {
        this.plugin = plugin.getCanonicalName();
        this.context = context;
        return this;
    }
}
