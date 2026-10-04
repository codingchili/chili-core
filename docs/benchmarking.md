[back](index)

Section to include
- BenchmarkBuilder / Benchmark
- BenchmarkConsoleListener / BenchmarkListener
- BenchmarkExecutor
- BenchmarkGroupBuilder / BenchmarkGroup
- BenchmarkImplementationBuilder / BenchmarkImplementation
- BenchmarkOperation / BenchmarkListener / BenchmarkReport / BenchmarkResult
- CoreBehcnmarkSuite
- MapBenchmarkImplementation
- Guidelines on writing benchmarks
- Running benchmarks from the terminal

### Running the core benchmarks

```console
gradlew benchmark -Piterations=5000                  # from the chili-core repository, report in core/build/benchmarks
java -jar <file.jar> benchmark --iterations 5000 --html  # from a packaged jar, opens the report in a browser
```

Without `--html` the results are printed as a table in the console. The HTML report is a single self-contained
file. It compares each operation across implementations: a heatmap table with operations per second, and a bar
chart per operation highlighting the fastest implementation.

### Writing a benchmark

Operations return a `Future`, lifecycle methods of a `BenchmarkImplementation` return `Future<Void>`.

```java
BenchmarkGroup group = new BenchmarkGroupBuilder("maps", 5000);

group.implementation("PrivateMap")
        .add("put", () -> storage.put(next()))
        .add("get", () -> storage.get(nextId()));

new BenchmarkExecutor(core)
        .setListener(new BenchmarkConsoleListener())
        .start(group)
        .onSuccess(results -> new BenchmarkHTMLReport(results).saveTo("report.html"));
```

Every implementation runs each benchmark once as warmup, `reset()` is called, and then the measured run starts.
Iterations of one benchmark run concurrently, benchmarks and implementations run one at a time.