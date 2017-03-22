package com.codingchili.core.benchmarking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.protocol.Serializer;

import de.neuland.jade4j.Jade4J;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * @author Robin Duda
 */
public class BenchmarkHTMLReport implements BenchmarkReport {
    private static final String VERSION = "version";
    private static final String OUTPUT = "report.jade";
    private String template = "./report.jade";
    private Buffer output;

    public BenchmarkHTMLReport(List<BenchmarkResult> results) {
        try {
            Map<String, Object> model = Serializer.json(group(results)).getMap();
            model.put(VERSION, new LauncherSettings().getVersion());
            output = Buffer.buffer(Jade4J.render(template, model, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * groups a list of benchmarking results by: group, test and implementation
     *
     * @param results a list of results to group together
     * @return results grouped within a map.
     */
    private Map<String, Map<String, List<BenchmarkResult>>> group(List<BenchmarkResult> results) {
        Map<String, Map<String, List<BenchmarkResult>>> all = new HashMap<>();
        for (BenchmarkResult bench : results) {
            all.computeIfAbsent(bench.group(), k -> new HashMap<>())
                    .computeIfAbsent(bench.name(), k -> new ArrayList<>())
                    .add(bench);
        }
        return all;
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
