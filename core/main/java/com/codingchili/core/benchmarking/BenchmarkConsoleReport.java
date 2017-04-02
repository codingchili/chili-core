package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.EXT_TXT;
import static com.codingchili.core.configuration.CoreStrings.getFileFriendlyDate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import com.codingchili.core.logging.ConsoleLogger;

/**
 * @author Robin Duda
 *         <p>
 *         Crates benchmark reports in the terminal.
 */
public class BenchmarkConsoleReport implements BenchmarkReport {
    private static final String[] HEADER = {"\n[GROUP]", " [IMPLEMENTATION]", " [BENCHMARK]", " [OP/s]", " [TIME]"};
    private static final int PARAM_COUNT = 5;
    private static final char TOKEN = '%';
    private List<BenchmarkGroup> groups = new ArrayList<>();
    private ConsoleLogger logger = new ConsoleLogger();
    private String template = "%-16s%-18s%-23s%-12s%-14s";

    public BenchmarkConsoleReport(List<BenchmarkGroup> groups) {
        this.groups = groups;
    }

    @Override
    public BenchmarkReport template(String string) {
        if (paramCount(string) == PARAM_COUNT) {
            this.template = string;
        } else {
            throw new IllegalArgumentException("template must accept " + PARAM_COUNT +
                    " tokens ('" + TOKEN + "s').");
        }
        return this;
    }

    private int paramCount(String string) {
        return (int) string.chars().filter(a -> a == TOKEN).count();
    }

    @Override
    public BenchmarkReport display() {
        logger.log(String.format(template, HEADER));
        lines().forEach(logger::log);
        return this;
    }

    @Override
    public BenchmarkReport saveTo(String path) {
        try {
            Files.write(Paths.get(path), lines());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public String saveToFile() {
        String fileName = getFileFriendlyDate() + EXT_TXT;
        saveTo(fileName);
        return fileName;
    }

    private List<String> lines() {
        List<String> list = new ArrayList<>();
        groups.forEach(group -> group.getImplementations().forEach(implementation -> {
            implementation.getBenchmarks().forEach(benchmark -> list.add(String.format(template,
                    group.getName(), implementation.getName(),
                    benchmark.getName(), benchmark.getRateFormatted(),
                    benchmark.getTimeFormatted())));
        }));
        return list;
    }
}
