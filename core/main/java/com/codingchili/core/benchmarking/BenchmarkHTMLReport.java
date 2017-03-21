package com.codingchili.core.benchmarking;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.util.List;

/**
 * @author Robin Duda
 */
public class BenchmarkHTMLReport implements BenchmarkReport {
    private static final String version = "$version";
    private static final String param_group = "$group";
    private static final String implementation = "$implementation";
    private static final String time = "$time";
    private static final String maxtime = "$maxtime";
    private static final String operations = "$operations";
    private static final String performance = "$performance";
    private static final String iterations = "$iterations";
    private static final String parallelism = "$parallelism";
    private String report;
    private String group;
    private String benchmark;

    public static void main(String[] args) {
        new BenchmarkHTMLReport(Vertx.vertx());
    }

    public BenchmarkHTMLReport(Vertx vertx) {
        FileSystem fs = vertx.fileSystem();
        report = fs.readFileBlocking("benchmarking/report.html").toString();
        group = fs.readFileBlocking("benchmarking/group.html").toString();
        benchmark = fs.readFileBlocking("benchmarking/benchmark.html").toString();
    }

    @Override
    public BenchmarkReport create(List<BenchmarkResult> results) {

        return this;
    }

    private void parse(List<BenchmarkResult> results) {
        String report = this.report;
        String group = this.group;
        String benchmark = this.benchmark;

        for (BenchmarkResult result : results) {
            // todo replace constants - serialize and get values by fieldname?
        }

        // todo store output
    }

    @Override
    public BenchmarkReport display() {
        // todo save to temp dir and open with browser
        return this;
    }

    @Override
    public BenchmarkReport saveTo(String path) {
        // todo write report to file.
        return this;
    }
}
