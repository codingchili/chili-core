package com.codingchili.core.benchmarking;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.codingchili.core.context.*;
import com.codingchili.core.storage.*;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

/**
 * @author Robin Duda
 *         <p>
 *         Contains system benchmarks..
 */
public class BenchmarkSuite {
    private static final String MAP_BENCHMARKS = "Map benchmarks";
    private int iterations = 2000;

    /**
     * Creates a clustered vertx instance on which all registered benchmarks will run.
     */
    public Void execute(Future<Void> future, CommandExecutor executor) {
        executor.getProperty(PARAM_ITERATIONS).ifPresent(iterations ->
                this.iterations = Integer.parseInt(iterations));

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            CoreContext context = new SystemContext(cluster.result());

            maps(context, new BenchmarkConsoleListener()).setHandler(done -> {
                new BenchmarkHTMLReport(context, done.result()).display();
                context.vertx().close(closed -> future.complete());
            });
        });
        return null;
    }

    /**
     * Runs all core map benchmarks.
     *
     * @return a future that is completed with the results of the benchmark.
     */
    public Future<List<BenchmarkGroup>> maps(CoreContext context, BenchmarkListener listener) {
        BenchmarkGroup group = new BaseBenchmarkGroup(MAP_BENCHMARKS, iterations);
        Future<List<BenchmarkGroup>> future = Future.future();

        Consumer<Class> add = (clazz) -> group.add(
                new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        add.accept(IndexedMap.class);
        add.accept(HazelMap.class);
        /*add.accept(ElasticMap.class);
        add.accept(MongoDBMap.class);*/

        new BenchmarkExecutor(context)
                .setListener(listener)
                .start(future, group);

        return future;
    }
}
