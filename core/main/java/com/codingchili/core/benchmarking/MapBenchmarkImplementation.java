package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

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
    public Future<Void> initialize(CoreContext core) {
        return new StorageLoader<StorageObject>(new StorageContext<>(core))
                .withPlugin(plugin)
                .withValue(StorageObject.class)
                .withDB(DB, COLLECTION)
                .build()
                .onSuccess(store -> this.storage = store)
                .mapEmpty();
    }

    @Override
    public Future<Void> next() {
        counter = new AtomicInteger(0);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> reset() {
        return storage.clear();
    }

    @Override
    public Future<Void> shutdown() {
        return storage.clear();
    }

    /**
     * Measures time taken to put all entries into the map one by one.
     */
    private Future<?> putOne() {
        int id = counter.getAndIncrement();
        return storage.put(new StorageObject(getName(id), id));
    }

    private String getName(int id) {
        return id + ".name";
    }

    /**
     * Measures the time taken to get all entries one by one by their primary key.
     */
    private Future<?> getOne() {
        return storage.get(getName(counter.getAndIncrement()));
    }

    /**
     * Measures the time taken to get all entries starting with the given string.
     * The query does not target the primary key.
     */
    private Future<?> startsWithQuery() {
        return storage.query()
                .on(ID_NAME)
                .startsWith(counter.getAndIncrement() + "")
                .execute();
    }

    /**
     * Measures the time taken to get all entries that equally matches the given string.
     * The query does not target the primary key.
     */
    private Future<?> equalToQuery() {
        return storage.query()
                .on(ID_NAME)
                .equalTo(getName(counter.getAndIncrement()))
                .execute();
    }

    /**
     * Measures the time taken to get all entries that contains a specified value within
     * a given range. The query does not target the primary key.
     */
    private Future<?> betweenQuery() {
        int low = counter.getAndIncrement();
        return storage.query()
                .on(StorageObject.levelField)
                .between((long) (low - 1), (long) (low + 1))
                .execute();
    }

    /**
     * Measures the time taken to return all values stored in the map.
     */
    private Future<?> values() {
        return storage.values();
    }

    /**
     * Measures the time taken to get all entries that matches the given regular expression.
     * The query does not target the primary key.
     */
    private Future<?> regexpQuery() {
        return storage.query()
                .on(ID_NAME).matches(".*")
                .execute();
    }

    /**
     * Measures the time taken to get all entries that are equal to the given primary key.
     */
    private Future<?> equalToPrimaryKey() {
        return storage.query()
                .on(Storable.idField)
                .equalTo(counter.getAndIncrement() + "")
                .execute();
    }
}
