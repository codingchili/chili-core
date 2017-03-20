package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.Storable;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.core.testing.StorageObject;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *
 * Implementation of a map for use with benchmarking.
 */
public class MapBenchmarkImplementation implements BenchmarkImplementation {
    private List<Benchmark> benchmarks = new ArrayList<>();
    private AsyncStorage<Storable> storage;
    private Vertx vertx;
    private Class plugin;
    private String group;
    private String implementation;

    public MapBenchmarkImplementation(Class plugin, String group, String implementation) {
        this.group = group;
        this.implementation = implementation;
        this.plugin = plugin;

        add(this::putAll, "put all")
                .add(this::getAll, "get all")
                .add(this::betweenQuery, "between query")
                .add(this::equalToQuery, "equal to query")
                .add(this::equalToPrimaryKey, "equal to primary key")
                .add(this::regexpQuery, "regular expression")
                .add(this::startsWithQuery, "starts with");
    }

    private MapBenchmarkImplementation add(BenchmarkOperation operation, String name) {
        this.add(new MapBenchmark(operation, group, implementation, name));
        return this;
    }

    @Override
    public void initialize(Handler<AsyncResult<Void>> handler) {
        this.vertx = Vertx.vertx();
        System.out.println("initialize " + implementation);

        new StorageLoader<>(new StorageContext<>(vertx))
                .withPlugin(plugin)
                .withClass(StorageObject.class)
                .withDB("", "").build(store -> {
                    this.storage = store.result();
                    handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public void reset(Handler<AsyncResult<Void>> future) {
        System.out.println("reset " + implementation);
        storage.clear(future);
    }

    @Override
    public void shutdown(Handler<AsyncResult<Void>> future) {
        System.out.println("shutdown " + implementation);
        vertx.close(future);
    }

    @Override
    public String name() {
        return implementation;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public BenchmarkImplementation add(Benchmark benchmark) {
        benchmarks.add(benchmark);
        return this;
    }

    @Override
    public List<Benchmark> benchmarks() {
        return this.benchmarks;
    }

    /**
     * Measures time taken to put all entries into the map one by one.
     */
    private Future<Void> putAll(Future<Void> future) {
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries one by one by their primary key.
     */
    private Future<Void> getAll(Future<Void> future) {
        try
        {
            Thread.sleep(32);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries starting with the given string.
     * The query does not target the primary key.
     */
    private Future<Void> startsWithQuery(Future<Void> future) {
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries that equally matches the given string.
     * The query does not target the primary key.
     */
    private Future<Void> equalToQuery(Future<Void> future) {
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries that contains a specified value within
     * a given range. The query does not target the primary key.
     */
    private Future<Void> betweenQuery(Future<Void> future) {
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries that matches the given regular expression.
     * The query does not target the primary key.
     */
    private Future<Void> regexpQuery(Future<Void> future) {
        future.complete();
        return Future.future();
    }

    /**
     * Measures the time taken to get all entries that are equal to the given primary key.
     */
    private Future<Void> equalToPrimaryKey(Future<Void> future) {
        future.complete();
        return Future.future();
    }
}
