package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.*;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Contains system benchmarks..
 */
public class BenchmarkSuite {
    private static final String MAP_BENCHMARKS = "Map benchmarks";
    private int iterations = 15;

    /**
     * Creates a clustered vertx instance on which all registered benchmarks will run.
     *
     * @param future   callback on completion
     * @param executor executor to invoke this as a command.
     */
    public Void execute(Future<Boolean> future, CommandExecutor executor) {
        executor.getProperty(PARAM_ITERATIONS).ifPresent(iterations ->
                this.iterations = Integer.parseInt(iterations));

        SystemContext.clustered(cluster -> {
                maps(cluster.result(), new BenchmarkConsoleListener()).setHandler(done -> {
                    if (done.succeeded()) {
                        createReport(future, done.result(), executor);
                    } else {
                        future.fail(done.cause());
                    }
                    cluster.result().close();
                });
        });
        return null;
    }

    private void createReport(Future<Boolean> future, List<BenchmarkGroup> result, CommandExecutor executor) {
        Optional<String> template = executor.getProperty(PARAM_TEMPLATE);
        BenchmarkReport report;

        if (executor.hasProperty(PARAM_HTML)) {
            report = new BenchmarkHTMLReport(result);
        } else {
            report = new BenchmarkConsoleReport(result);
        }
        template.ifPresent(report::template);
        report.display();
        future.complete(true);
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

        Consumer<Class<? extends AsyncStorage>> add = (clazz) -> group.add(
                new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        add.accept(IndexedMapPersisted.class);
        add.accept(IndexedMapVolatile.class);
        add.accept(HazelMap.class);
        /*add.accept(ElasticMap.class); requires external servers.
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
