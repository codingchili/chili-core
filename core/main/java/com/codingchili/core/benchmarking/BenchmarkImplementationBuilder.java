package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

import java.util.*;

import com.codingchili.core.context.CoreContext;

/**
 * "Abstract" benchmark-implementation.
 */
public class BenchmarkImplementationBuilder implements BenchmarkImplementation {
    private Map<String, Object> properties = new HashMap<>();
    private List<Benchmark> benchmarks = new ArrayList<>();
    protected BenchmarkGroup group;
    private String name;

    public BenchmarkImplementationBuilder(String name) {
        this.name = name;
    }

    public BenchmarkImplementationBuilder setGroup(BenchmarkGroup group) {
        this.group = group;
        benchmarks.forEach(bench -> bench.setIterations(group.getIterations()));
        return this;
    }

    @Override
    public BenchmarkImplementation add(String name, BenchmarkOperation operation) {
        Benchmark benchmark = new BenchmarkBuilder(name)
                .setOperation(operation);

        if (group != null) {
            benchmark.setIterations(group.getIterations());
        }

        benchmarks.add(benchmark);
        return this;
    }

    /**
     * Adds a benchmark where each iteration performs a batch of operations, see
     * {@link BenchmarkBuilder#setOperationsPerIteration(int)}.
     *
     * @param name                the name of the operation/benchmark.
     * @param operationsPerIteration the number of operations performed by one call of the operation.
     * @param operation           the operation to execute as a benchmark.
     * @return fluent
     */
    public BenchmarkImplementation add(String name, int operationsPerIteration, BenchmarkOperation operation) {
        Benchmark benchmark = new BenchmarkBuilder(name)
                .setOperationsPerIteration(operationsPerIteration)
                .setOperation(operation);

        if (group != null) {
            benchmark.setIterations(group.getIterations());
        }

        benchmarks.add(benchmark);
        return this;
    }

    @Override
    public Future<Void> initialize(CoreContext core) {
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> next() {
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> reset() {
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> shutdown() {
        return Future.succeededFuture();
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
        return name;
    }

    @Override
    public BenchmarkImplementation setName(String name) {
        this.name = name;
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
