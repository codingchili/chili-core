package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.JsonMap;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *
 * Benchmarks for map implementations.
 */
public class MapBenchmarkGroup implements BenchmarkGroup {
    private List<BenchmarkImplementation> implementations = new ArrayList<>();

    public static void main(String[] args) {
        Future<List<BenchmarkResult>> future = Future.future();
        new BenchmarkExecutor(future, new MapBenchmarkGroup());
        future.setHandler(benchmarks -> benchmarks.result().forEach(System.out::println));
    }

    public MapBenchmarkGroup() {
        add(JsonMap.class, "Json Map");
        // todo add synchronization to allow multiple implementations to execute serially
          /*  .add(PrivateMap.class, "Private map")
            .add(SharedMap.class, "Shared map")
            .add(IndexedMap.class, "Indexed map");*/
    }

    private MapBenchmarkGroup add(Class plugin, String name) {
        add(new MapBenchmarkImplementation(plugin, name));
        return this;
    }

    @Override
    public String groupName() {
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
