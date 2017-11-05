package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.launcher;

/**
 * @author Robin Duda
 * <p>
 * Builder to load storage plugins.
 */
public class StorageLoader<Value extends Storable> {
    private CoreContext context;
    private Logger logger;
    private String DB = launcher().getApplication();
    private Class<Value> aClass;
    private String collection;
    private String plugin;

    public StorageLoader() {
    }

    /**
     * Creates a new storage loader
     * @param context the context to use.
     */
    public StorageLoader(CoreContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
    }

    private void load(Handler<AsyncResult<AsyncStorage<Value>>> handler) {
        context.blocking(blocking -> {
            try {
                Future<AsyncStorage<Value>> future = Future.future();
                future.setHandler(handler);

                StorageContext<Value> storage = new StorageContext<Value>(context)
                        .setDatabase(DB)
                        .setCollection(collection)
                        .setClass(aClass)
                        .setPlugin(plugin);

                Class.forName(plugin)
                        .getConstructor(Future.class, StorageContext.class)
                        .<Value>newInstance(future, storage);
                blocking.complete();

            } catch (ReflectiveOperationException e) {
                logger.log(CoreStrings.getStorageLoaderError(plugin, DB, collection), Level.ERROR);
                blocking.fail(e);
            }
        }, (done) -> {
            if (done.failed()) {
                handler.handle(Future.failedFuture(done.cause()));
            }
        });
    }

    /**
     * @param context the context to use
     * @return fluent
     */
    public StorageLoader<Value> withContext(CoreContext context) {
        this.context = context;
        return this;
    }

    /**
     * @param DB database name to use, if unset defaults to application name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB) {
        this.DB = DB;
        return this;
    }

    /**
     * @param DB database name to use, if unset uses application name.
     * @param collection collection name to use, if unset uses storable class name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB, String collection) {
        this.DB = DB;
        this.collection = collection;
        return this;
    }

    /**
     * @param clazz the class to be stored.
     * @return fluent.
     */
    @SuppressWarnings("unchecked")
    public StorageLoader<Value> withClass(Class<? extends Value> clazz) {
        this.aClass = (Class<Value>) clazz;
        return this;
    }

    /**
     * @param plugin a plugin implementing #{@link AsyncStorage}.
     * @return fluent.
     */
    public StorageLoader<Value> withPlugin(Class<? extends AsyncStorage> plugin) {
        this.plugin = plugin.getCanonicalName();
        return this;
    }

    /**
     * @param collection collection name to use, defaults to storables class name.
     * @return fluent.
     */
    public StorageLoader<Value> withCollection(String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * @param plugin a plugin to store the given class, must implement
     *               #{@link AsyncStorage}
     * @return fluent.
     */
    public StorageLoader<Value> withPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    /**
     * Loads the configured storage. Throws an exception if context,
     * class or plugin is unset.
     * @param future completed when the storage is loaded.
     */
    public void build(Handler<AsyncResult<AsyncStorage<Value>>> future) {
        checkIsSet(context, ID_CONTEXT);
        checkIsSet(aClass, ID_CLASS);
        checkIsSet(plugin, ID_PLUGIN);

        if (collection == null) {
            collection = aClass.getSimpleName();
        }

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
