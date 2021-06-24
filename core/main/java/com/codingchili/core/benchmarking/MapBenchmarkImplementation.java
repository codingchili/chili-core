package com.codingchili.core.benchmarking;

import io.vertx.core.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.*;
import com.codingchili.core.testing.StorageObject;

import static com.codingchili.core.configuration.CoreStrings.ID_NAME;

/**
 * Implementation of a map for use with benchmarking.
 */
public class MapBenchmarkImplementation extends BenchmarkImplementationBuilder {
    private static final String COLLECTION = "collection";
    private static final String DB = "db";
    private AtomicInteger counter = new AtomicInteger(0);
    private AsyncStorage<StorageObject> storage;
    private Class<? extends AsyncStorage> plugin;

    public MapBenchmarkImplementation(BenchmarkGroup group, Class<? extends AsyncStorage> plugin, String implementation) {
        super(implementation);
        setGroup(group);
        this.plugin = plugin;

        add("put all", this::putOne)
                .add("get all", this::getOne)
                .add("values", this::values)
                .add("between query", this::betweenQuery)
                .add("equal to query", this::equalToQuery)
                .add("equal to primary key", this::equalToPrimaryKey)
                .add("regular expression", this::regexpQuery)
                .add("starts with", this::startsWithQuery);
    }

    @Override
    public void initialize(CoreContext core, Handler<AsyncResult<Void>> handler) {
        new StorageLoader<StorageObject>(new StorageContext<>(core))
                .withPlugin(plugin)
                .withValue(StorageObject.class)
                .withDB(DB, COLLECTION).build(store -> {
            this.storage = store.result();
            handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public void next(Promise<Void> promise) {
        counter = new AtomicInteger(0);
        promise.complete();
    }

    @Override
    public void reset(Handler<AsyncResult<Void>> future) {
        storage.clear(future);
    }

    @Override
    public void shutdown(Promise<Void> promise) {
        storage.clear(promise);
    }

    /**
     * Measures time taken to put all entries into the map one by one.
     */
    private void putOne(Promise<Void> promise) {
        int id = counter.getAndIncrement();
        storage.put(new StorageObject(getName(id), id), done -> promise.complete());
    }

    private String getName(int id) {
        return id + ".name";
    }

    /**
     * Measures the time taken to get all entries one by one by their primary key.
     */
    private void getOne(Promise<Void> promise) {
        storage.get(getName(counter.getAndIncrement()), done -> promise.complete());
    }

    /**
     * Measures the time taken to get all entries starting with the given string.
     * The query does not target the primary key.
     */
    private void startsWithQuery(Promise<Void> promise) {
        storage.query()
                .on(ID_NAME)
                .startsWith(counter.getAndIncrement() + "")
                .execute(done -> promise.complete());
    }

    /**
     * Measures the time taken to get all entries that equally matches the given string.
     * The query does not target the primary key.
     */
    private void equalToQuery(Promise<Void> promise) {
        storage.query()
                .on(ID_NAME)
                .equalTo(getName(counter.getAndIncrement()))
                .execute(done -> promise.complete());
    }

    /**
     * Measures the time taken to get all entries that contains a specified value within
     * a given range. The query does not target the primary key.
     */
    private void betweenQuery(Promise<Void> promise) {
        int low = counter.getAndIncrement();
        storage.query()
                .on(StorageObject.levelField)
                .between((long) (low - 1), (long) (low + 1))
                .execute(done -> promise.complete());
    }

    /**
     * Measures the time taken to return all values stored in the map.
     */
    private void values(Promise<Void> promise) {
        storage.values(done -> promise.complete());
    }

    /**
     * Measures the time taken to get all entries that matches the given regular expression.
     * The query does not target the primary key.
     */
    private void regexpQuery(Promise<Void> promise) {
        storage.query()
                .on(ID_NAME).matches(".*")
                .execute(done -> promise.complete());
    }

    /**
     * Measures the time taken to get all entries that are equal to the given primary key.
     */
    private void equalToPrimaryKey(Promise<Void> promise) {
        storage.query()
                .on(Storable.idField)
                .equalTo(counter.getAndIncrement() + "")
                .execute(done -> promise.complete());
    }
}
