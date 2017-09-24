package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * "Abstract" benchmark-implementation.
 */
public class BaseBenchmarkImplementation implements BenchmarkImplementation {
    protected BenchmarkGroup group;
    private Map<String, Object> properties = new HashMap<>();
    private List<Benchmark> benchmarks = new ArrayList<>();
    private String implementation;

    public BaseBenchmarkImplementation(BenchmarkGroup group, String name) {
        this.implementation = name;
        this.group = group;
    }

    @Override
    public BenchmarkImplementation add(BenchmarkOperation operation, String name) {
        benchmarks.add(new BaseBenchmark(group, this, operation, name));
        return this;
    }

    @Override
    public BenchmarkImplementation add(Benchmark benchmark) {
        benchmarks.add(benchmark);
        return this;
    }

    @Override
    public void initialize(CoreContext core, Handler<AsyncResult<Void>> future) {
        future.handle(Future.succeededFuture());
    }

    @Override
    public void next(Future<Void> future) {
        future.complete();
    }

    @Override
    public void reset(Handler<AsyncResult<Void>> future) {
        future.handle(Future.succeededFuture());
    }

    @Override
    public void shutdown(Future<Void> future) {
        future.complete();
    }

    @Override
    public List<Benchmark> getBenchmarks() {
        return benchmarks;
    }

    @Override
    public BenchmarkImplementation setBenchmarks(List<Benchmark> benchmarks) {
        this.benchmarks = benchmarks;
        return this;
    }

    @Override
    public String getName() {
        return implementation;
    }

    @Override
    public BenchmarkImplementation setName(String name) {
        this.implementation = name;
        return this;
    }

    @Override
    public BenchmarkImplementation setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
