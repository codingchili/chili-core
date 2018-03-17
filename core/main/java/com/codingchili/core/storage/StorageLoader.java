package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Objects;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Builder to load storage plugins.
 */
public class StorageLoader<Value extends Storable> {
    private Class<? extends AsyncStorage> plugin;
    private Class<Value> valueClass;
    private CoreContext context;
    private Logger logger;
    private String pluginString;
    private String database;
    private String collection;

    public StorageLoader() {
    }

    /**
     * Creates a new storage loader
     *
     * @param context the context to use.
     */
    public StorageLoader(CoreContext context) {
        this.context = context;
    }

    private void load(Handler<AsyncResult<AsyncStorage<Value>>> handler) {
        Future<AsyncStorage<Value>> future = Future.future();
        context.blocking(blocking -> {
            try {
                future.setHandler(handler);

                prepare();
                StorageContext<Value> storage = new StorageContext<Value>(context)
                        .setDatabase(database)
                        .setCollection(collection)
                        .setClass(valueClass)
                        .setPlugin(plugin);

                plugin.getConstructor(Future.class, StorageContext.class)
                        .<Value>newInstance(future, storage);
                blocking.complete();

            } catch (Throwable e) {
                logger.log(CoreStrings.getStorageLoaderError(plugin, database, collection), Level.ERROR);
                logger.onError(e);
                blocking.fail(e);
            }
        }, (done) -> {
            if (done.failed()) {
                future.tryFail(done.cause());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void prepare() throws ClassNotFoundException {
        checkIsSet(context, ID_CONTEXT);
        checkIsSet(valueClass, ID_CLASS);

        if (pluginString != null)
            this.plugin = (Class<? extends AsyncStorage>) Class.forName(pluginString);


        if (collection == null) {
            this.collection = valueClass.getSimpleName();
        }

        if (database == null) {
            database = Configurations.storage().getSettingsForPlugin(plugin).getDatabase();
        }

        checkIsSet(plugin, ID_PLUGIN);
    }

    /**
     * @param DB database name to use, if unset defaults to application name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB) {
        this.database = DB;
        return this;
    }

    /**
     * @param DB         database name to use, if unset uses application name.
     * @param collection collection name to use, if unset uses storable class name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB, String collection) {
        this.database = DB;
        this.collection = collection;
        return this;
    }

    /**
     * @param valueClass the class to be stored.
     * @return fluent.
     */
    public StorageLoader<Value> withValue(Class<Value> valueClass) {
        this.valueClass = valueClass;
        return this;
    }

    /**
     * @param plugin a plugin implementing #{@link AsyncStorage}.
     * @return fluent.
     */
    public StorageLoader<Value> withPlugin(Class<? extends AsyncStorage> plugin) {
        this.plugin = plugin;
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
    @SuppressWarnings("unchecked")
    public StorageLoader<Value> withPlugin(String plugin) {
        this.pluginString = plugin;
        return this;
    }

    /**
     * Loads the configured storage. Throws an exception if context,
     * class or plugin is unset.
     *
     * @param handler completed when the storage is loaded.
     */
    @SuppressWarnings("unchecked")
    public void build(Handler<AsyncResult<AsyncStorage<Value>>> handler) {
        this.logger = context.logger(getClass());
        this.load(handler);
    }

    private void checkIsSet(Object object, String type) {
        Objects.requireNonNull(object, CoreStrings.getStorageLoaderMissingArgument(type));
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

    private StorageLoader<Value> makeWith(CoreContext context, Class<? extends AsyncStorage> plugin) {
        this.plugin = plugin;
        this.context = context;
        return this;
    }
}
