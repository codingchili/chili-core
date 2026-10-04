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

### Benchmarking how a protocol invokes routes

```console
gradlew benchmark -Psuite=protocol -Piterations=300
```

Compares the ways a `Protocol` can invoke the method of a route: `Method.invoke` (what the protocol does today),
a `MethodHandle` and a `LambdaMetafactory`-generated class. A direct call is included as a baseline, and
`Protocol.process` end to end (authentication, route mapping, authorization and the call) to show how large a
part of the work the invocation is. Each iteration is a batch of 100,000 calls, so 300 iterations measure 30 million
calls per implementation and route. A third operation measures registering the routes of a handler, which is where
`LambdaMetafactory` pays for its faster calls. See `ProtocolBenchmarkImplementation` for details.

| Median of 3 runs | Method.invoke | MethodHandle | LambdaMetafactory | Direct call | Protocol.process |
|---|---|---|---|---|---|
| Route with request | 4.1 ns | 3.4 ns | 1.6 ns | 1.5 ns | 23 ns |
| Route without request | 3.8 ns | 2.6 ns | 1.4 ns | 1.5 ns | 22 ns |
| Register 2 routes | 154 ns | 1.7 µs | 34 µs | 45 ns | 777 ns |

Measured on Windows 11, 24 cores, JDK 27, one JVM, using the framework's own benchmarks and not JMH. Run-to-run
differences were up to 20% for calls and larger for registering routes (up to 70% for `MethodHandle`), but the order of the
strategies was the same in every run. All implementations are measured behind the same call site, which has seen every
strategy: as in a protocol with many routes the JVM cannot inline the invoked route.

What it means:

- `Method.invoke` costs about 2.5 ns more per call than a generated class. Since JDK 18 reflection is implemented with
  method handles, which is why it's no longer an order of magnitude slower.
- A `MethodHandle` stored in a field is only a little faster than `Method.invoke`, and slower to create. There is no
  reason to choose it.
- `LambdaMetafactory` is as fast as a direct call, but generates a class per route: about 17 µs per route. That's repaid
  after about 7,000 requests to the route.
- `Protocol.process` takes about 23 ns per request, of which `Method.invoke` is about 4 ns. Switching would make
  the protocol about 10% faster, and a real request (reading and parsing JSON, the network, the work of the handler)
  takes microseconds or more, so this isn't noticeable in a service.

### Writing a benchmark

Operations return a `Future`, lifecycle methods of a `BenchmarkImplementation` return `Future<Void>`.
Operations that take nanoseconds should perform a batch of calls per iteration, and say so with
`BenchmarkImplementationBuilder.add(name, operationsPerIteration, operation)`, as the cost of the benchmark itself
(a future and a callback per iteration) is otherwise larger than the operation. The rate and the report count the
operations in the batches.

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