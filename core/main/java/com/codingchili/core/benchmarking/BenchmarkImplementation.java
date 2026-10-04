package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

import com.codingchili.core.context.CoreContext;

/**
 * Groups benchmarks of the same implementation together.
 */
public interface BenchmarkImplementation {

    /**
     * Prepares an implementation for testing.
     *
     * @param core the context to use for the benchmark
     * @return future completed when the setup is complete.
     */
    Future<Void> initialize(CoreContext core);

    /**
     * Called before each benchmark is executed.
     *
     * @return future completed when the next benchmark may start.
     */
    Future<Void> next();

    /**
     * Called after the warmup phase has completed.
     *
     * @return future completed when the implementation is reset.
     */
    Future<Void> reset();

    /**
     * Called after the benchmarking has completed.
     *
     * @return future completed when the implementation is shut down.
     */
    Future<Void> shutdown();

    /**
     * Adds a new benchmark created from an operation and name.
     *
     * @param name      the name of the operation/benchmark.
     * @param operation the operation to execute as a benchmark.
     * @return fluent
     */
    BenchmarkImplementation add(String name, BenchmarkOperation operation);

    /**
     * @return a listof benchmarks that should be executed for the given implementation.
     */
    List<Benchmark> getBenchmarks();

    /**
     * Sets the benchmarks to a list of benchmarks, may be used when creating reports.
     *
     * @param benchmarks the benchmarks to set
     * @return fluent
     */
    BenchmarkImplementation setBenchmarks(List<Benchmark> benchmarks);

    /**
     * Name of the implementation that is used within a benchmark.
     * A good choice is {@link Class#getSimpleName()}.
     *
     * @return a string that identifies the implementation tested.
     */
    String getName();

    /**
     * May be used in the reporting phase to restructure results.
     *
     * @param name the name of the implementation
     * @return fluent.
     */
    BenchmarkImplementation setName(String name);

    /**
     * Set a property of the benchmark implementation.
     *
     * @param key   the key to identify the value.
     * @param value the value to be inserted.
     * @return fluent
     */
    BenchmarkImplementation setProperty(String key, Object value);

    /**
     * get all properties added to the implementation.
     *
     * @return a map of all the properties that has been set.
     */
    Map<String, Object> getProperties();

    /**
     * @param group the group the implementation is part of.
     * @return fluent
     */
    BenchmarkImplementation setGroup(BenchmarkGroup group);
}
