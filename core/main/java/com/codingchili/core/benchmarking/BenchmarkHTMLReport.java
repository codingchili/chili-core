package com.codingchili.core.benchmarking;

import java.io.IOException;
import java.util.*;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.protocol.Serializer;

import de.neuland.jade4j.Jade4J;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         <p>
 *         Generates a HTML benchmark report with Jade.
 */
public class BenchmarkHTMLReport implements BenchmarkReport {
    private static final String VERSION = "version";
    private static final String BENCHMARKS = "benchmarks";
    private static final String OUTPUT = "report.jade";
    private String template = "./report.jade";
    private Buffer output;

    /**
     * Parses the benchmarking results of a benchmark group.
     *
     * @param results a list of benchmarking groups to create a report for.
     */
    public BenchmarkHTMLReport(List<BenchmarkGroup> results) {
        try {
            JsonObject model = new JsonObject()
                    .put(BENCHMARKS, serialize(results))
                    .put(VERSION, new LauncherSettings().getVersion());

            output = Buffer.buffer(Jade4J.render(template, model.getMap(), true));
            System.err.println(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JsonObject> serialize(List<BenchmarkGroup> list) {
        List<JsonObject> benchmarks = new ArrayList<>();
        list.forEach(entry -> {
            JsonObject group = new JsonObject()
                    .put("group", Serializer.json(entry)
                            .put("benchmarks", implementations(entry)));
            benchmarks.add(group);
        });
        return benchmarks;
    }

    private JsonObject implementations(BenchmarkGroup group) {
        Map<String, List<Benchmark>> results = new HashMap<>();

        group.implementations().forEach(implementation -> {
            implementation.benchmarks().forEach(benchmark -> {
                List<Benchmark> list = results.get(benchmark.getName());

                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(benchmark);
                results.put(benchmark.getName(), list);
            });
        });

        return Serializer.json(results);
    }

    /**
     * Parses the benchmark results of a single benchmark group.
     *
     * @param result a benchmark group to create a report for.
     */
    public BenchmarkHTMLReport(BenchmarkGroup result) {
        this(toList(result));
    }

    private static List<BenchmarkGroup> toList(BenchmarkGroup result) {
        List<BenchmarkGroup> list = new ArrayList<>();
        list.add(result);
        return list;
    }

    @Override
    public BenchmarkReport template(String string) {
        this.template = string;
        return this;
    }

    @Override
    public BenchmarkReport display() {
        saveTo(OUTPUT);
        return this;
    }

    @Override
    public BenchmarkReport saveTo(String path) {
        Vertx vertx = Vertx.vertx();
        vertx.fileSystem().writeFileBlocking(path, output);
        vertx.close();
        return this;
    }
}
