package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

import java.util.List;
import java.util.function.Consumer;

import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.HazelMap;
import com.codingchili.core.storage.IndexedMap;
import com.codingchili.core.storage.JsonMap;
import com.codingchili.core.storage.PrivateMap;
import com.codingchili.core.storage.SharedMap;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

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
     *
     * @param future   callback on completion
     * @param executor executor to invoke this as a command.
     */
    public Void execute(Future<Void> future, CommandExecutor executor) {
        executor.getProperty(PARAM_ITERATIONS).ifPresent(iterations ->
                this.iterations = Integer.parseInt(iterations));

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            CoreContext context = new SystemContext(cluster.result());

            maps(context, new BenchmarkConsoleListener()).setHandler(done -> {
                if (done.succeeded()) {
                    new BenchmarkHTMLReport(future, context, done.result()).display();
                } else {
                    future.fail(done.cause());
                }
                context.vertx().close();
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

    /**
     * Set the number of iterations to perform.
     *
     * @param iterations iterations to perform
     * @return fluent
     */
    public BenchmarkSuite setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
}
