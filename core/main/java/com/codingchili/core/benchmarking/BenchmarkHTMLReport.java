package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.EXT_HTML;
import static com.codingchili.core.configuration.CoreStrings.getFileFriendlyDate;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.CoreContext;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
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
    public static final String LOCAL_INDEX = "localIndex";
    private CoreContext context;
    private String template = "core/main/resources/benchmarking/report.jade";
    private String file;
    private Buffer output;

    /**
     * Parses the benchmark results of a single benchmark group.
     *
     * @param result a benchmark group to create a report for.
     */
    public BenchmarkHTMLReport(CoreContext context, BenchmarkGroup result) {
        this(context, toList(result));
    }

    /**
     * Parses the benchmarking results of a benchmark group.
     *
     * @param results a list of benchmarking groups to create a report for.
     */
    public BenchmarkHTMLReport(CoreContext context, List<BenchmarkGroup> results) {
        this.context = context;

        try {
            JsonObject model = new JsonObject()
                    .put(BENCHMARKS, baseline(reorder(results)))
                    .put(VERSION, new LauncherSettings().getVersion());

            output = Buffer.buffer(Jade4J.render(getTemplate(), model.getMap(), true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Establishes a baseline index per implemenation bucket so that relative comparison
     * may be performed.
     *
     * @param groups the results to calculate the baseline for
     * @return the same list with baselines added to every benchmark result.
     */
    private List<BenchmarkGroup> baseline(List<BenchmarkGroup> groups) {
        Function<BenchmarkImplementation, Integer> max = implementation -> {
            AtomicInteger maxRate = new AtomicInteger(0);
            implementation.getBenchmarks().forEach(benchmark -> {
                if (benchmark.getRate() > maxRate.get()) {
                    maxRate.set(benchmark.getRate());
                }
            });
            return maxRate.get();
        };

        groups.forEach(group -> group.getImplementations().forEach(implementation -> {
            int localMax = max.apply(implementation);

            implementation.getBenchmarks().forEach(benchmark -> {
                benchmark.setProperty(LOCAL_INDEX,
                        ((benchmark.getRate() * 1.0f / localMax) * 100));
            });
        }));
        return groups;
    }

    /**
     * Groups benchmarks together by their name.
     *
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
                list.add(new BaseBenchmarkImplementation(group, key).setBenchmarks(value));
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
                Buffer buffer = context.vertx().fileSystem().readFileBlocking(template);
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
        try {
            Desktop.getDesktop().browse(Paths.get(saveToFile()).toUri());
        } catch (IOException ignored) {
        }
        return this;
    }

    @Override
    public BenchmarkReport saveTo(String path) {
        this.file = path;
        context.vertx().fileSystem().writeFileBlocking(path, output);
        return this;
    }

    @Override
    public String saveToFile() {
        String fileName = getFileFriendlyDate() + EXT_HTML;
        saveTo(fileName);
        return fileName;
    }
}
