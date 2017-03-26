package com.codingchili.core.benchmarking;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.List;
import java.util.function.Consumer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.*;

/**
 * @author Robin Duda
 *         <p>
 *         Contains system benchmarks..
 */
public class BenchmarkSuite {

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            CoreContext context = new SystemContext(cluster.result());
            maps(context, new BenchmarkConsoleListener()).setHandler(done -> {
                new BenchmarkHTMLReport(context, done.result()).saveTo("report-nn.html");
                context.vertx().close();
            });
        });
    }

    /**
     * Runs all core map benchmarks.
     *
     * @return a future that is completed with the results of the benchmark.
     */
    public static Future<List<BenchmarkGroup>> maps(CoreContext context, BenchmarkListener listener) {
        BenchmarkGroup group = new AbstractBenchmarkGroup("Map benchmarks", 25000);
        Future<List<BenchmarkGroup>> future = Future.future();

        Consumer<Class> add = (clazz) -> group.add(
                new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        add.accept(IndexedMap.class);
        add.accept(HazelMap.class);

        new BenchmarkExecutor(context)
                .setListener(listener)
                .start(future, group);

        return future;
    }
}
