package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

/**
 * Benchmark operation called when benchmarking.
 */
@FunctionalInterface
public interface BenchmarkOperation {
    /**
     * Performs the operation once.
     *
     * @return a future completed when the operation is done, the result is ignored.
     */
    Future<?> perform();
}