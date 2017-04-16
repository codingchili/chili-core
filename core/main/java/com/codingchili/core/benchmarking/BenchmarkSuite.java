package com.codingchili.core.benchmarking;

import io.vertx.core.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.codingchili.core.context.*;
import com.codingchili.core.storage.*;

import static com.codingchili.core.configuration.CoreStrings.*;

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
                    createReport(future, done.result(), executor);
                } else {
                    future.fail(done.cause());
                }
                context.vertx().close();
            });
        });
        return null;
    }

    private void createReport(Future<Void> future, List<BenchmarkGroup> result, CommandExecutor executor) {
        Optional<String> template = executor.getProperty(PARAM_TEMPLATE);
        BenchmarkReport report;

        if (executor.hasProperty(PARAM_HTML)) {
            report = new BenchmarkHTMLReport(result);
        } else {
            report = new BenchmarkConsoleReport(result);
        }
        template.ifPresent(report::template);
        report.display();
        future.complete();
    }

    /**
     * Runs all core map benchmarks.
     *
     * @param context  the core context to run benchmark on
     * @param listener benchmark listener to use
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
