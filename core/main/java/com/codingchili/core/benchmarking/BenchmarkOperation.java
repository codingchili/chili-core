package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

/**
 * Benchmark operation called when benchmarking.
 */
@FunctionalInterface
public interface BenchmarkOperation {
    /**
     * Returns a future so that benchmark operations may be composed in order.
     *
     * @param future a future to be completed when the operation is done.
     */
    void perform(Future<Void> future);
}