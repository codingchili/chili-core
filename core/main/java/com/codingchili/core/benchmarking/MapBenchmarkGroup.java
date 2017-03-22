package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.IndexedMap;
import com.codingchili.core.storage.JsonMap;
import com.codingchili.core.storage.PrivateMap;
import com.codingchili.core.storage.SharedMap;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         Benchmarks for map implementations.
 */
public class MapBenchmarkGroup implements BenchmarkGroup {
    private List<BenchmarkImplementation> implementations = new ArrayList<>();

    public static void main(String[] args) {
        Future<List<BenchmarkResult>> future = Future.future();
        new BenchmarkExecutor(future, new MapBenchmarkGroup());
        future.setHandler(benchmarks -> new BenchmarkHTMLReport(benchmarks.result()).saveTo("report-zz.html"));
    }

    public MapBenchmarkGroup() {
        add(JsonMap.class, "Json Map")
                .add(PrivateMap.class, "Private map")
                .add(SharedMap.class, "Shared map")
                .add(IndexedMap.class, "Indexed map");
    }

    private MapBenchmarkGroup add(Class plugin, String implementation) {
        add(new MapBenchmarkImplementation(plugin, name(), implementation));
        return this;
    }

    @Override
    public String name() {
        return "Map Benchmarks";
    }

    @Override
    public List<BenchmarkImplementation> implementations() {
        return this.implementations;
    }

    @Override
    public BenchmarkGroup add(BenchmarkImplementation benchmark) {
        implementations.add(benchmark);
        return this;
    }
}
