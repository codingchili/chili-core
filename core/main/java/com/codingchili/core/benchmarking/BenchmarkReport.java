package com.codingchili.core.benchmarking;

/**
 * Interface for benchmarking report creation.
 */
public interface BenchmarkReport {

    /**
     * Sets the template to use. May be a path to a file or a template.
     *
     * @param string path or template to use
     * @return fluent
     */
    BenchmarkReport template(String string);

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
     * @param path where the report is written
     * @return fluent
     */
    BenchmarkReport saveTo(String path);

    /**
     * saves the report to file using a generated filename.
     *
     * @return path to the report file that was saved
     */
    String saveToFile();
}
