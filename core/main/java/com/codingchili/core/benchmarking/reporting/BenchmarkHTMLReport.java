package com.codingchili.core.benchmarking.reporting;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.codingchili.core.benchmarking.BenchmarkGroup;
import com.codingchili.core.benchmarking.BenchmarkReport;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.files.Resource;
import com.codingchili.core.files.exception.NoSuchResourceException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Generates a HTML benchmark report with Jade.
 */
public class BenchmarkHTMLReport implements BenchmarkReport {
    private static final String VERSION = "version";
    private static final String BENCHMARKS = "benchmarks";
    private List<BenchmarkGroup> results;
    private String template = "/benchmarking/report.jade";

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
        this.results = results;
    }

    private static List<BenchmarkGroup> toList(BenchmarkGroup result) {
        List<BenchmarkGroup> list = new ArrayList<>();
        list.add(result);
        return list;
    }

    private Buffer render() {
        JsonObject model = new JsonObject()
                .put(BENCHMARKS, baseline(reorder(results)))
                .put(VERSION, new LauncherSettings().getVersion());
        return Buffer.buffer(Jade4J.render(getTemplate(), model.getMap(), true));
    }

    /**
     * Establishes a baseline index per implementation bucket so that relative comparison
     * may be performed.
     *
     * @param groups the results to calculate the baseline for
     * @return the same list with baselines added to every benchmark result.
     */
    private List<ResultGroup> baseline(List<ResultGroup> groups) {
        Function<ResultSet, Integer> max = implementation -> {
            AtomicInteger maxRate = new AtomicInteger(0);
            implementation.getItems().forEach(item -> {
                if (item.getRate() > maxRate.get()) {
                    maxRate.set(item.getRate());
                }
            });
            return maxRate.get();
        };

        groups.forEach(group -> group.getSets().forEach(implementation -> {
            int localMax = max.apply(implementation);
            implementation.getItems().forEach(item -> item.setLocalIndex(((item.getRate()* 1.0f / localMax) * 100)));
        }));
        return groups;
    }

    /**
     * Groups benchmarks together by their name.
     *
     * @param results the results to be grouped.
     * @return a list of reordered groups.
     */
    private List<ResultGroup> reorder(List<BenchmarkGroup> results) {
        List<ResultGroup> result = new ArrayList<>();

        results.forEach(group -> {
            ResultGroup operation = new ResultGroup(group.getName())
                    .setIterations(group.getIterations());

            group.getImplementations().forEach(implementation -> {
                ResultSet resultSet = new ResultSet(implementation.getName());

                implementation.getBenchmarks().forEach(benchmark -> {
                    resultSet.add(new ResultItem(benchmark));
                });

                operation.add(resultSet);
            });

            result.add(operation);
        });
        return result;
    }

    private JadeTemplate getTemplate() throws VertxException {
        JadeConfiguration config = new JadeConfiguration();
        config.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) {
                return System.currentTimeMillis();
            }

            @Override
            public Reader getReader(String name) throws VertxException {
                Optional<Buffer> buffer = new Resource(template).read();

                if (buffer.isPresent()) {
                    return new StringReader(buffer.get().toString());
                } else {
                    throw new NoSuchResourceException(name);
                }
            }

            @Override
            public String getExtension() {
                return "jade";
            }
        });
        try {
            return config.getTemplate(template);
        } catch (IOException e) {
            throw new VertxException(e);
        }
    }

    /**
     * Sets the jade template to use.
     *
     * @param template a path to jade on the classpath or filesystem.
     * @return fluent.
     */
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
        new Resource(path).write(render());
        return this;
    }

    @Override
    public String saveToFile() {
        String fileName = getFileFriendlyDate() + EXT_HTML;
        saveTo(fileName);
        return fileName;
    }
}
