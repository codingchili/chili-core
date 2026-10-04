package com.codingchili.core.benchmarking.reporting;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;

import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import com.codingchili.core.benchmarking.BenchmarkGroup;
import com.codingchili.core.benchmarking.BenchmarkReport;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.Resource;
import com.codingchili.core.files.exception.NoSuchResourceException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Generates a HTML benchmark report with Jade.
 */
public class BenchmarkHTMLReport implements BenchmarkReport {
    private static final String VERSION = "version";
    private static final String BENCHMARKS = "benchmarks";
    private static final String GENERATED = "generated";
    private static final String ENVIRONMENT = "environment";
    private static final String SUMMARY = "summary";
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
        List<ResultGroup> groups = reorder(results);
        Map<String, Object> model = new HashMap<>();
        model.put(BENCHMARKS, groups);
        model.put(SUMMARY, summary(groups));
        model.put(ENVIRONMENT, environment());
        // the version is unknown when running from classes instead of a packaged jar.
        String version = new LauncherSettings().getVersion();
        model.put(VERSION, (version == null || version.equals("n/a")) ? "" : version);
        model.put(GENERATED, ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")));
        return Buffer.buffer(Jade4J.render(getTemplate(), model, true));
    }

    private static Map<String, String> summary(List<ResultGroup> groups) {
        Set<String> implementations = new LinkedHashSet<>();
        int operations = 0;
        long measured = 0;

        for (ResultGroup group : groups) {
            implementations.addAll(group.getImplementations());
            operations += group.getOperations().size();
            for (ResultSet set : group.getSets()) {
                measured += (long) set.getItems().size() * group.getIterations();
            }
        }
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Groups", String.valueOf(groups.size()));
        summary.put("Implementations", String.valueOf(implementations.size()));
        summary.put("Operations", String.valueOf(operations));
        summary.put("Measured calls", String.format("%,d", measured));
        return summary;
    }

    private static Map<String, String> environment() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put("Java", System.getProperty("java.vm.name") + " " + Runtime.version());
        environment.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.version") +
                " (" + System.getProperty("os.arch") + ")");
        environment.put("CPU cores", String.valueOf(runtime.availableProcessors()));
        environment.put("Max heap", (runtime.maxMemory() / (1024 * 1024)) + " MB");
        return environment;
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
                    resultSet.add(new ResultItem(implementation.getName(), benchmark));
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
        String file = saveToFile();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(Paths.get(file).toUri());
            } catch (IOException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            System.out.println(getBenchmarkReportSaved(Paths.get(file).toAbsolutePath().toString()));
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
