package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.codingchili.core.benchmarking.reporting.BenchmarkConsoleReport;
import com.codingchili.core.benchmarking.reporting.BenchmarkHTMLReport;
import com.codingchili.core.context.*;
import com.codingchili.core.storage.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Contains system benchmarks..
 */
public class CoreBenchmarkSuite {
    private static final String MAP_BENCHMARKS = "Map benchmarks";
    private int iterations = 15;

    /**
     * Runs the benchmarks selected with the suite parameter ({@code --suite maps|protocol|all}, default maps)
     * and creates a report. The map benchmarks run on a clustered vertx instance, the protocol benchmarks
     * do not need one.
     *
     * @param executor executor to invoke this as a command.
     * @return future completed when the benchmarks are done and the report is created.
     */
    public Future<CommandResult> execute(CommandExecutor executor) {
        executor.getProperty(PARAM_ITERATIONS).ifPresent(iterations ->
                this.iterations = Integer.parseInt(iterations));

        String suite = executor.getProperty(PARAM_SUITE).orElse(SUITE_MAPS);
        BenchmarkListener listener = new BenchmarkConsoleListener();

        switch (suite) {
            case SUITE_MAPS:
                return SystemContext.clustered().compose(cluster ->
                        maps(cluster, listener)
                                .map(result -> createReport(result, executor))
                                .onComplete(done -> cluster.close()));
            case SUITE_PROTOCOL:
                CoreContext core = new SystemContext();
                return protocol(core, listener)
                        .map(result -> createReport(result, executor))
                        .onComplete(done -> core.close());
            case SUITE_ALL:
                return SystemContext.clustered().compose(cluster ->
                        maps(cluster, listener)
                                .compose(maps -> protocol(cluster, listener).map(protocol -> {
                                    List<BenchmarkGroup> all = new ArrayList<>(maps);
                                    all.addAll(protocol);
                                    return all;
                                }))
                                .map(result -> createReport(result, executor))
                                .onComplete(done -> cluster.close()));
            default:
                return Future.failedFuture(new IllegalArgumentException(getUnknownBenchmarkSuite(suite)));
        }
    }

    private CommandResult createReport(List<BenchmarkGroup> result, CommandExecutor executor) {
        Optional<String> template = executor.getProperty(PARAM_TEMPLATE);
        BenchmarkReport report;

        if (executor.hasProperty(PARAM_HTML)) {
            report = new BenchmarkHTMLReport(result);
        } else {
            report = new BenchmarkConsoleReport(result);
        }
        template.ifPresent(report::template);
        report.display();
        return LauncherCommandResult.SHUTDOWN;
    }

    /**
     * Runs the benchmarks that compare how the protocol invokes the methods of a handler, see
     * {@link ProtocolBenchmarkImplementation}. One iteration is {@link ProtocolBenchmarkImplementation#CALLS_PER_ITERATION}
     * calls.
     *
     * @param context  the core context to run the benchmark on.
     * @param listener benchmark listener to use.
     * @return a future that is completed with the results of the benchmark.
     */
    public Future<List<BenchmarkGroup>> protocol(CoreContext context, BenchmarkListener listener) {
        try {
            return new BenchmarkExecutor(context)
                    .setListener(listener)
                    .start(ProtocolBenchmarkImplementation.group(iterations));
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Runs all core map benchmarks.
     *
     * @param context  the core context to run benchmark on
     * @param listener benchmark listener to use
     * @return a future that is completed with the results of the benchmark.
     */
    public Future<List<BenchmarkGroup>> maps(CoreContext context, BenchmarkListener listener) {
        BenchmarkGroup group = new BenchmarkGroupBuilder(MAP_BENCHMARKS, iterations);

        Consumer<Class<? extends AsyncStorage>> add = (clazz) -> {
            group.add(new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));
        };

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        //add.accept(IndexedMapPersisted.class);
        add.accept(IndexedMapVolatile.class);
        add.accept(HazelMap.class);
        /*add.accept(ElasticMap.class); requires external servers.
        add.accept(MongoDBMap.class);*/

        return new BenchmarkExecutor(context)
                .setListener(listener)
                .start(group);
    }

    /**
     * Set the number of iterations to perform.
     *
     * @param iterations iterations to perform
     * @return fluent
     */
    public CoreBenchmarkSuite setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
}
