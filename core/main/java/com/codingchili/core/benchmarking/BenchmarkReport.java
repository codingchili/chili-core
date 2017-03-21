package com.codingchili.core.benchmarking;

import java.util.List;

/**
 * @author Robin Duda
 *         <p>
 *         Interface for benchmarking report creation.
 */
public interface BenchmarkReport {

    /**
     * Creates a report from a list of results.
     *
     * @param results the results to create a report from.
     * @return fluent
     */
    BenchmarkReport create(List<BenchmarkResult> results);

    /**
     * displays the results of the report by opening the browser,
     * printing to terminal or any other means specified by the implementation.
     *
     * @return fluent
     */
    BenchmarkReport display();

    /**
     * saves the results to file.
     *
     * @return fluent
     */
    BenchmarkReport saveTo(String path);
}
