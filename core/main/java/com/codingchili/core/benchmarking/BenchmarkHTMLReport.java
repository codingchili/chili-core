package com.codingchili.core.benchmarking;

import java.io.*;
import java.util.*;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.protocol.Serializer;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
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
    private String template = "core/main/resources/benchmarking/report.jade";
    private Buffer output;

    /**
     * Parses the benchmark results of a single benchmark group.
     *
     * @param result a benchmark group to create a report for.
     */
    public BenchmarkHTMLReport(BenchmarkGroup result) {
        this(toList(result));
    }

    /**
     * Parses the benchmarking results of a benchmark group.
     *
     * @param results a list of benchmarking groups to create a report for.
     */
    public BenchmarkHTMLReport(List<BenchmarkGroup> results) {
        try {
            JsonObject model = new JsonObject()
                    .put(BENCHMARKS, reorder(results))
                    .put(VERSION, new LauncherSettings().getVersion());

            output = Buffer.buffer(Jade4J.render(getTemplate(), model.getMap(), true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Groups benchmarks together by their name.
     * @param results the results to be grouped.
     * @return a list of reordered groups.
     */
    private List<BenchmarkGroup> reorder(List<BenchmarkGroup> results) {
        results.forEach(group -> {
            List<BenchmarkImplementation> implementations = group.getImplementations();
            Map<String, List<Benchmark>> buckets = new HashMap<>();

            implementations.forEach(implementation -> {
                implementation.getBenchmarks().forEach(benchmark -> {
                    List<Benchmark> list = buckets.get(benchmark.getName());
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(benchmark);
                    buckets.put(benchmark.getName(), list);
                });
            });

            List<BenchmarkImplementation> list = new ArrayList<>();
            buckets.forEach((key, value) -> {
                list.add(new AbstractBenchmarkImplementation(group, key).setBenchmarks(value));
            });

            group.setImplementations(list);
        });
        return results;
    }

    private JadeTemplate getTemplate() throws IOException {
        JadeConfiguration config = new JadeConfiguration();
        config.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) throws IOException {
                return System.currentTimeMillis();
            }

            @Override
            public Reader getReader(String name) throws IOException {
                Vertx vertx = Vertx.vertx();
                Buffer buffer = vertx.fileSystem().readFileBlocking(template);
                vertx.close();
                return new StringReader(buffer.toString());
            }
        });
        return config.getTemplate(template);
    }

    private static List<BenchmarkGroup> toList(BenchmarkGroup result) {
        List<BenchmarkGroup> list = new ArrayList<>();
        list.add(result);
        return list;
    }

    @Override
    public BenchmarkReport template(String template) {
        this.template = template;
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
