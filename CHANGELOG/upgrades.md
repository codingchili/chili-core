# Vert.x 5 / Jackson 3 upgrade: handlers → futures

Moves chili-core's async APIs from `Handler<AsyncResult<T>>` callbacks to returning `Future<T>`. It also fixes runtime
breakage from the Vert.x 5.2 and Jackson 3 upgrade (commits `a6b18e2a`, `44d6e269`, `d4eb9e44`). None of this is
committed yet.

Written as a handoff: the first section is what **consumers of chili-core** must change. The rest covers internal
fixes, test status and queued work. The `SystemContext` deployment rework is documented separately in
[simplify.md](simplify.md).

## Adapting consumers (breaking API changes)

Every change below is a compile error in downstream code, so the compiler finds them all. The usual fix is
`x(args, cb)` → `x(args).onComplete(cb)`. Prefer `compose`/`map`/`onSuccess`/`onFailure` when rewriting.

### `AsyncStorage<Value>`: all operations return `Future`

| Before | After |
|---|---|
| `void get(String key, Handler<AsyncResult<Value>>)` | `Future<Value> get(String key)` |
| `void contains(String key, Handler<AsyncResult<Boolean>>)` | `Future<Boolean> contains(String key)` |
| `void put(Value, Handler<AsyncResult<Void>>)` | `Future<Void> put(Value)` |
| `void putIfAbsent(Value, Handler<AsyncResult<Void>>)` | `Future<Void> putIfAbsent(Value)` |
| `void remove(String key, Handler<AsyncResult<Void>>)` | `Future<Void> remove(String key)` |
| `void update(Value, Handler<AsyncResult<Void>>)` | `Future<Void> update(Value)` |
| `void values(Handler<AsyncResult<Stream<Value>>>)` | `Future<Stream<Value>> values()` |
| `void clear(Handler<AsyncResult<Void>>)` | `Future<Void> clear()` |
| `void size(Handler<AsyncResult<Integer>>)` | `Future<Integer> size()` |

The failure contract is unchanged: `ValueMissingException`, `ValueAlreadyPresentException`,
`NothingToRemoveException` and `NothingToUpdateException` arrive as the future's failure cause.

Custom `AsyncStorage` implementations must change their signatures to match. The plugin constructor
`(Promise<AsyncStorage<Value>>, StorageContext<Value>)` is unchanged.

```java
// before
storage.get(id, done -> { if (done.succeeded()) use(done.result()); });
// after
storage.get(id).onSuccess(this::use);
storage.put(account).compose(v -> storage.get(account.getId())).onSuccess(...);
```

### Queries: `QueryBuilder.execute()`

- `void execute(Handler<AsyncResult<Collection<Value>>>)` → `Future<Collection<Value>> execute()`.
- This also applies to `Query` (the standalone/DSL query) and to custom `QueryBuilder` / `AbstractQueryBuilder`
  subclasses. `Query`'s mapper still runs on every result before the caller sees it.
- `query.poll(consumer, timer)` (`EntryWatcher`) is unchanged.

```java
storage.query("name").startsWith("rob").pageSize(10).execute().onSuccess(results -> ...);
```

### `StorageLoader.build()`

- `void build(Handler<AsyncResult<AsyncStorage<Value>>>)` → `Future<AsyncStorage<Value>> build()`.

```java
new StorageLoader<Account>(core).withPlugin(IndexedMapVolatile.class).withValue(Account.class)
        .build()
        .onSuccess(accounts -> ...)
        .onFailure(e -> ...);
```

### `CoreContext`

- `close()` now returns `Future<Void>`, and `close(Handler<AsyncResult<Void>>)` is **removed**.
  - `core.close();` as a statement still compiles.
  - `core.close(handler)` becomes `core.close().onComplete(handler)`.
  - In tests: `context.close().onComplete(test.asyncAssertSuccess())`.
  - Closing is best effort: the future always succeeds, as before.
- `stop()` returns `Future<Void>` instead of `Future<CompositeFuture>`. See [simplify.md](simplify.md).
- `SystemContext.clustered(Handler<AsyncResult<CoreContext>>)` → `Future<CoreContext> SystemContext.clustered()`.
  See [simplify.md](simplify.md).

### `HashFactory.verify`

- `void verify(Handler<AsyncResult<Void>>, String expected, char[] plaintext)` →
  `Future<Void> verify(String expected, char[] plaintext)`. Fails with `HashMismatchException` on a mismatch.

```java
hasher.verify(hashed, password).onSuccess(v -> login()).onFailure(e -> reject());
```

### Commands (`Command`, `BaseCommand`, `CommandExecutor`)

| Before | After |
|---|---|
| `Command.execute(Promise<CommandResult>, CommandExecutor)` | `Future<CommandResult> execute(CommandExecutor)` |
| `CommandExecutor execute(Promise<CommandResult>, String...)` | `Future<CommandResult> execute(String...)` |
| `CommandResult execute(String...)` (sync, threw `CoreRuntimeException`) | removed, use the future above |
| `add(BiFunction<Promise<CommandResult>, CommandExecutor, Void>, name, description)` | `addAsync(Function<CommandExecutor, Future<CommandResult>>, name, description)` |
| `new BaseCommand(BiFunction<Promise<CommandResult>, CommandExecutor, Void>, ...)` | `BaseCommand.async(Function<CommandExecutor, Future<CommandResult>>, ...)` |
| `CoreBenchmarkSuite.execute(Promise<CommandResult>, CommandExecutor)` | `Future<CommandResult> execute(CommandExecutor)` |

- The synchronous `add(Function<CommandExecutor, CommandResult>, ...)` and `BaseCommand` constructor are
  unchanged. The async variant got a new name because a same-named overload would make `e -> RESULT` lambdas
  ambiguous.
- **Behavior change:** `execute(String...)` no longer throws for an unknown command. The future fails with
  `NoSuchCommandException`. Exceptions thrown by synchronous commands now fail the future too.

### Benchmarking (`BenchmarkImplementation`, `BenchmarkOperation`)

| Before | After |
|---|---|
| `void initialize(CoreContext, Handler<AsyncResult<Void>>)` | `Future<Void> initialize(CoreContext)` |
| `void next(Promise<Void>)` | `Future<Void> next()` |
| `void reset(Handler<AsyncResult<Void>>)` | `Future<Void> reset()` |
| `void shutdown(Promise<Void>)` | `Future<Void> shutdown()` |
| `BenchmarkOperation.perform(Promise<Void>)` | `Future<?> perform()` |

```java
// before
group.implementation("fast").add("put", promise -> storage.put(value).onComplete(done -> promise.complete()));
// after
group.implementation("fast").add("put", () -> storage.put(value));
```

- **Behavior change:** if `initialize`, `reset` or `shutdown` fails, `BenchmarkExecutor.start` now fails. It used
  to ignore the failure and carry on (`MapBenchmarkImplementation` then hit a `NullPointerException` on a missing
  storage). A failed `perform()` still counts as a completed iteration.

### Smaller changes

- `AsynchronousSemaphore.acquire(Handler<AsyncResult<Void>>, int)` → `Future<Void> acquire(int timeoutMS)`.
- `Delay.forMS(Promise<Void>, long)` → `Future<Void> forMS(long)`.
- `StorageContext.handle(Handler<AsyncResult<Value>>, Value)` is removed. It had no callers and was left over
  from the callback storage API.

### Behavior changes consumers may notice (no compile error)

- **Deployments now actually start and stop.** In Vert.x 5.2, `CoreVerticle` never had `init`/`start`/`stop` called.
  Every `CoreService`/`CoreListener`/`CoreHandler` reported "deployed" without running its lifecycle, and
  listeners never bound a port. Consumers on the earlier upgrade commits may have code or tests that only worked
  because `stop()` never ran.
  - **Don't block the event loop in `start`/`stop`.** A `stop()` that waits for Vert.x to close will deadlock.
- **Deployment ids:** `listener`/`service`/`handler` now return the real Vert.x deployment id, covering all
  instances. They used to return a synthetic UUID. See [simplify.md](simplify.md).
- **Insecure listeners are plaintext again.** `ListenerSettings.getSecurity()` returns `null` when `secure` is false.
  It used to return an empty `ServerSSLOptions`, which in Vert.x 5 turns TLS on and fails with
  "Key/certificate is mandatory for SSL".
- **JSON output matches pre-upgrade behavior** (Jackson 3 mappers in `Serializer`):
  - Null fields are omitted again.
  - Empty beans serialize again, and unknown properties are ignored.
  - JSON comments are allowed again.
  - Pretty printing is on by default.
  - `SystemSettings.setPrettyEncoding(false)` now actually turns it off. It used to always enable it.
- **Never use Vert.x databind for POJOs.** `JsonObject.mapFrom/mapTo` and `Json.encode*` on POJOs need Jackson 2, which
  is excluded, so they fail at runtime. Use `Serializer.json/buffer/pack/unpack`. `Serializer.json(Object)` and
  `Serializer.buffer(Object)` now go through Jackson 3 internally.

## Internal changes and fixes

- **Storage implementations** (`PrivateMap`, `SharedMap`, `JsonMap`, `IndexedMap*`, `HazelMap`, `MongoDBMap`,
  `ElasticMap`) are rewritten to return futures, using `compose`/`map`/`transform` instead of nested callbacks.
  - `JsonMap`'s private helpers were renamed `find`/`store`/`delete` to avoid clashing with the interface methods.
  - `IndexedMap.blocking` returns a `Future`. The old version threw a `NullPointerException` if the worker task itself
    failed.
  - `MongoDBMap`'s constructor no longer risks never completing its promise.
- **`HazelMap` query and `HashFactory.verify`:** both still called the removed
  `blocking(Handler<Promise>, Handler<AsyncResult>)`. They now use `blocking(Callable)`. `verify` also passes on
  a blocking failure instead of throwing a `NullPointerException`.
- **`CoreVerticle`** now extends `VerticleBase`. Vert.x only calls `Deployable.deploy(Context)`, and the old
  class stubbed that out.
- **`ClusteredSessionFactory`** returns the storage futures directly.
- **`MapBenchmarkImplementation`** callers were updated.
- **`JsonObjectDeserializer` / `JsonStorableDeserializer`:** their no-arg constructors now pass the handled type.
  Jackson 3 rejects `null`, which broke `Serializer`'s static init.
- **`WebsocketListenerIT`** uses `vertx.createWebSocketClient().connect(port, host, uri)`.
  `HttpClient.webSocket` was removed in Vert.x 5.
- **Handler → future migration, finished** (queued item 1). Besides the API changes above:
  - `BenchmarkExecutor` is a plain `compose` chain. `start(groups)` used to have no failure path, so its future
    could never complete if a group failed.
  - `BenchmarkExecutor` (bug fix): `onBenchmarkCompleted` fired *before* `benchmark.finish()`, so listeners
    (`BenchmarkConsoleListener`) logged an unset elapsed time. `finish()` now runs first.
  - `AsynchronousSemaphore.release` (bug fix): the loop polled a waiter before checking `permits > 0`, which
    dropped that waiter so its future never completed.
  - `cluster.xml`: removed the fixed `<instance-name>core</instance-name>`. A second clustered context in the
    same JVM failed with "HazelcastInstance with name 'core' already exists!". `HazelMap` picks the running
    instance without using its name. This fixes `BenchmarkIT.testExecuteSuiteAsCommand` (see `simplify.md`).
  - `BenchmarkTests` never waited for the executor, so its assertions ran after the test had passed. They now
    use `asyncAssertSuccess`. That exposed a wrong expectation in `testVerifyNumberOfIterations`: each benchmark
    runs `ITERATIONS` times for warmup and `ITERATIONS` times recorded.
  - Docs: `docs/security.md`, `protocol.md`, `context.md`, `handlers.md`, `listeners.md`, `services.md`,
    `launcher.md` and `storage.md` no longer use the Vert.x 3 `setHandler`, `start(Future<Void>)`,
    callback-style `verify`/`execute`, or non-supplier `core.service(new ...)`. `AGENTS.md` async section
    updated.
  - Left as is: `IndexedMap.blocking(Handler<Promise<T>>)` (protected, internal to the CQEngine plugins) and the
    `CoreDeployment.start/stop(Promise)` / storage plugin constructor promises (Vert.x lifecycle style).
- **`ShutdownHookTest`** had a deadlock: `serviceStopOverridesTimeout` blocked the event loop inside `stop()` while
  waiting for Vert.x to close. Polling now runs on its own thread, and the class has a 10s `Timeout` rule.
- **`SystemContext.stop()`** clears tracked deployments, so later stops don't re-undeploy ("Already undeployed").
- **Docs:** `docs/storage.md` and `docs/context.md` examples now use the future API. `docs/storage.md` is back to
  CRLF line endings.
- **Repo docs:** `AGENTS.md` was added (agent/human orientation, Vert.x 5 gotchas), plus `CLAUDE.md` (`@AGENTS.md`).
- **Tests** were mechanically converted (`x(args, cb)` → `x(args).onComplete(cb)`), with assertions unchanged.

## New: `QuicListener` (raw QUIC, not HTTP/3)

Queued item 4. Uses the Vert.x 5.2 QUIC API (`vertx.createQuicServer`, `QuicConnection`, `QuicStream`).
Documented in `docs/listeners.md` ("QUIC").

- **Wire model: one request per bidirectional stream.** The client writes a JSON request and ends its side of the
  stream. The server collects the body until the end, handles it, and `QuicRequest.write` ends the stream with the
  response. This needs no framing (unlike `TcpListener`, which parses each TCP chunk as one JSON object) and
  lets one connection carry many concurrent requests without head-of-line blocking.
- **Server push:** `request.connection().write(..)` opens a new unidirectional stream per message. Vert.x clients
  allow 0 peer-initiated unidirectional streams by default, so clients have to opt in
  (`QuicConfig.setInitialMaxStreamsUni` / `setInitialMaxStreamDataUni`). Until they do, pushes queue on the server
  up to `maxStreamUniRequests` (1024) and then fail with a logged error.
- **TLS is mandatory:** with `secure: false` the deployment fails with "QUIC requires TLS ...". ALPN uses
  `ListenerSettings.quicProtocol` (default `chili`, `CoreStrings.DEFAULT_QUIC_PROTOCOL`), and clients offering
  another protocol are rejected during the handshake.
- **Limits:** a body over `maxRequestBytes` gets `BAD` (`RequestPayloadSizeException`) plus `STOP_SENDING` with
  `QuicListener.ERROR_REQUEST_TOO_LARGE`. The body is never buffered past the limit. Malformed JSON gets `BAD`
  (`RequestValidationException`). Client-opened unidirectional streams are aborted
  (`ERROR_UNIDIRECTIONAL_STREAM`), since there's no way to answer on them.
- **Config:** `ListenerSettings.getQuic()/setQuic(QuicServerConfig)` (idle timeouts, flow control, congestion
  control, ...), `WireType.QUIC`. `QuicListener` is `DeploymentAware`: it deploys 1 instance, or
  `system.listeners` instances when `QuicServerConfig.loadBalanced` is on. Load balancing relies on
  `SO_REUSEPORT`, which works on Linux/macOS but not Windows (NIO).
- **Native library:** Vert.x declares `netty-codec-native-quic` optional. `core/build.gradle` adds it as `runtimeOnly`
  for `linux-x86_64`, `linux-aarch_64`, `osx-x86_64`, `osx-aarch_64` and `windows-x86_64` (about 12 MB in total,
  version pinned to the Netty version Vert.x uses, 4.2.18.Final). Consumers who want a smaller footprint can
  exclude the classifiers they don't need.
- **Tests:** `QuicListenerIT` (7 tests: the shared `ListenerTestCases` ping, several concurrent streams on one
  connection, request too large, malformed JSON, ALPN mismatch, insecure deploy fails, server push). All pass on
  Windows x86_64. `ListenerTestCases` gained a `configure(ListenerSettings)` hook.
- **Found along the way, not fixed:** the self-signed fallback for missing keystores
  (`SecuritySettings.generateSelfSigned` → `TestCertificate` → Netty `SelfSignedCertificate`) is broken on this JDK.
  It throws "No provider succeeded to generate a self-signed certificate". Netty needs BouncyCastle, or JDK internals
  that recent JDKs removed (`--add-exports java.base/sun.security.x509` doesn't help). So **every** secure
  listener, and RSA token verification with an unknown alias, fails without a configured keystore. `QuicListenerIT`
  uses the `test_key.jks` fixture instead. See `security.md`.

## Benchmark report redesign + first benchmark run

Queued item 5.

- **New report** (`core/main/resources/benchmarking/report.jade`), self-contained HTML. The old one loaded
  Bootstrap 3 and jQuery from CDNs.
  - Header with version, timestamp and environment (JVM, OS, cores, heap), plus a KPI row (groups, implementations,
    operations, measured calls).
  - Per group:
    - A "throughput by operation" heatmap table (operations × implementations, ops/s, shaded by % of the fastest
      implementation for that operation).
    - One small bar chart per operation: fixed implementation order, fastest in the accent color and the rest
      gray, values at the bar end, hover/focus tooltip with time and % of fastest.
    - A collapsible "All results" table.
  - Light and dark themes via `prefers-color-scheme`, plus `forced-colors` support. Colors come from the dataviz
    reference palette and were run through its validator: the emphasis pair passes separation and contrast (its
    chroma-floor FAIL is expected, since gray is the de-emphasis color, not a series), and both 5-step heat ramps
    pass the ordinal checks.
  - The old bars compared each operation with the *same implementation's* fastest operation, which says nothing
    useful. Bars and shades now compare implementations within an operation.
- **Report model** (`reporting.ResultGroup`, `ResultItem`, new `ResultOperation`): results are indexed by operation,
  with `percentOfFastest`, `fastest` and `heat` computed in Java so the template stays simple.
  `ResultItem(Benchmark)` → `ResultItem(String implementation, Benchmark)` and `setLocalIndex` was removed
  (**API change**, unlikely to have outside users).
- **Timing fixes** (`BenchmarkBuilder`, `BenchmarkResult`):
  - Timing used millisecond `Instant`s, so fast operations measured 0 ms and the rate was reported as
    `iterations × 1000` with a "+". It now uses `System.nanoTime()`. `Benchmark.getElapsedNanos()` was added
    (**API change** for custom `Benchmark` implementations).
  - `getTimeFormatted()` formatted elapsed ms as a `Date` with a hardcoded `EPOCH_BASE = 3600000`, which was only
    correct in UTC+1. It now prints a duration with a unit ("641 µs", "26.5 ms", "1.52 s",
    `BenchmarkResult.formatNanos`). `EPOCH_BASE`/`DATE_FORMAT` were removed. Note: "µ" is garbled in the Windows
    PowerShell 5.1 console. The HTML is UTF-8 and fine.
- **Bug fixes found while running it:**
  - `benchmark --iterations N` from the command line was ignored. The parser stores `--iterations` but
    `PARAM_ITERATIONS` was `"iterations"`. It is now `getParam("iterations")`, like `PARAM_HTML`.
  - `BenchmarkGroupBuilder` kept implementations in a `HashMap`, so report and console order was arbitrary. It's
    now a `LinkedHashMap` (insertion order).
  - `BenchmarkHTMLReport.display()` threw in headless environments. It now prints the report path when no desktop
    browser is available.
  - The version shows as empty instead of "n/a" when running from classes rather than the jar.
- **`gradlew benchmark [-Piterations=N]`** (new `core/build.gradle` task): runs `Launcher benchmark --html`
  headless with the report in `core/build/benchmarks/`. Default 1000 iterations.
- **First run** (2000 iterations, Windows 11, 24 cores, JDK 27, clustered Hazelcast on localhost; report
  `core/build/benchmarks/2026-10-04 12.23.44.html`, a second run after an identical one at 12.20.09):
  - `PrivateMap`/`SharedMap` lead plain put/get (about 3–4M ops/s). `JsonMap` reaches roughly a third of that.
  - `IndexedMapVolatile` (CQEngine) wins primary-key lookups (751k ops/s vs about 7k for the stream-query maps)
    and regex queries. Its put is slow (75k ops/s).
  - `HazelMap` is the slowest for put/get/values (`values` 249 ops/s, since every call copies the distributed
    map), but its indexed queries are the fastest for `between`/`equal to`/`starts with`.
  - **Large run-to-run variance:** the first run measured `IndexedMapVolatile` put at 495k and `JsonMap` put at
    2.05M ops/s. That's 2000 iterations, one measured pass, with JIT and GC noise. Use `-Piterations=20000`+ and
    several runs before drawing conclusions. A repeat/median option would be a good next step for the executor.
- Noise seen in every run, not fixed: at exit `ShutdownHook` logs through `RemoteLogger` over the event bus after
  Vert.x has closed, which prints a `RejectedExecutionException` stack trace.
- Test litter, not fixed: `BenchmarkHTMLReportTest`/`BenchmarkIT` write `*.html` reports into `core/` on every
  run (untracked, not gitignored). I deleted the ones my runs created.

## Tests clean up after themselves; `RestRequestTest` mock fixed

- **Litter the suite left in the working tree** (all untracked, none gitignored), now removed by the tests:
  - `core/<timestamp>.html` / `.txt`: `BenchmarkConsoleReportTest`, `BenchmarkHTMLReportTest` (also through `display()`).
    `BenchmarkReportTestCases` lists the report files before each test and deletes the new ones afterwards.
  - `core/wowza.html`: `BenchmarkIT.testBenchmarkBuilders` now writes to a `TemporaryFolder` and asserts the file
    exists.
  - `core/IndexedMapPersisted/MapTestCases.sqlite` (+ `-wal`, `-shm`): `MapTestCases` with `IndexedMapPersisted`.
    A new `@AfterClass` closes the database and deletes the files (new test helper `storage/PersistedFiles`). It has to
    be once per class, not per test: `IndexedMap` keeps one shared instance, and so one SQLite connection, per
    database/collection for the whole JVM.
  - `core/test/<uuid>.sqlite`: `StorageLoaderIT` (database name `test`, which resolves to `core/test`). It now uses
    its own database name and releases the files right after loading. An absolute temp directory can't be used,
    because `JsonMap` resolves the database name relative to the working directory.
- **Windows can't delete an open SQLite file**, so the helper closes the CQEngine `DiskPersistence` first. Finding
  the connection that stayed open took a probe test per operation: only one pattern leaked, see below.
- **Found, not fixed (production code): `IndexedMapPersisted.values()` + `Stream.count()` leaks a SQLite connection.**
  `values()` returns CQEngine's plain `Collection.stream()`. `count()` makes the spliterator open an iterator to
  estimate the size, nothing ever exhausts or closes that iterator, and closing the stream doesn't either. Reading
  the stream to the end (`forEach`, `toList()`) releases it. Callers that use `count()`, `findFirst()` or `limit()` on
  a disk-backed `values()` leak a connection until the JVM exits. Fix idea: build the stream from the
  `CloseableIterator` with `onClose(iterator::close)`. `MapTestCases.testGetValues` now reads the whole stream.
- **`gradlew test` runs headless** (`java.awt.headless=true` in `core/build.gradle`). `BenchmarkHTMLReportTest`
  called `display()`, which opened a real browser window on every test run. It now takes the same path as a headless
  server and prints the report location.
- **`RestRequestTest` (6 tests) passes.** The mock `RoutingContext.body()` returned `null`, so
  `RestRequest.parseData` threw a `NullPointerException`. It now returns a `RequestBody` backed by the mock's body
  buffer. The mock's `HttpServerRequest.body()` returns `Future.succeededFuture(body)`: the working copy had a
  half-finished `return null//return body;` that didn't compile.
- **Not litter, left alone:** `core/conf/` (runtime configuration saved at shutdown, gitignored on purpose) and the
  tracked fixtures under `core/test/resources`, which tests rewrite with identical content.
- **Concurrent Gradle builds in this project can fail with `java.io.EOFException` in `:core:test`.** Gradle daemons
  on two JDKs (IDE and terminal) writing `core/build` at the same time. A re-run passes.
- Full suite after this change: 723 tests, 2 failures (`RequestMockTest.testErrorStatusInBuffer` and
  `testReplyWithBuffer`: Jackson 3 `MismatchedInputException` deserializing a `LinkedHashMap` from a string,
  unrelated to this work), 142 skipped. Before: 9 failures. One of them, `WebsocketListenerIT.testAccepted`,
  failed in the first full run and passed in the second.

## Build: Gradle 9.4.0 → 9.8.0

- **Wrapper upgraded to 9.8.0** (`-all` distribution), which adds Java 27 support to match the toolchain. This
  regenerated `gradle-wrapper.properties`, `gradle-wrapper.jar`, `gradlew` and `gradlew.bat`.
  - Gets rid of the `java.lang.System::load ... --enable-native-access=ALL-UNNAMED` warning that 9.4.0's
    native-platform printed on JDK 24+.
  - If the upgrade is run with `gradlew.bat wrapper`, cmd prints "The system cannot find the path specified" at the end,
    because the script is replaced while it's still running. This is harmless.
- **`gradle.properties`:** removed `org.gradle.configureondemand=true`. It's incubating, printed a notice on every build,
  and doesn't help with a single subproject.
- **`build.gradle`:** `exceptionFormat "full"` → `exceptionFormat = "full"`. Space-assignment syntax is deprecated
  and will be removed in Gradle 10. `--warning-mode all` now reports no deprecations.
- Verified: `gradlew --version`, `gradlew help` and `compileTestJava` all pass. The test suite hasn't been run against
  9.8.0.

## Verification status

Latest runs (handler → future migration and QUIC listener), all passing:

- `BenchmarkTests`, `BenchmarkIT` (including `testExecuteSuiteAsCommand`), `BenchmarkConsoleReportTest`,
  `BenchmarkHTMLReportTest`, `HashFactoryTest`, `ByteComparatorTest`, `HazelMapIT`, `CommandExecutorTest`,
  `CommandParserTest`, `LauncherCommandExecutorTest`, `LaunchContextCommandLine`, `DelayTest`, `LauncherIT`.
- `RestListenerIT`, `TcpListenerIT`, `UdpListenerIT`, `WebsocketListenerIT`, `QuicListenerIT`, `ClusterListenerTest`.
- The full suite hasn't been run yet.

Earlier status:

- `gradlew build -x test` and `compileTestJava` were clean before the final `ShutdownHookTest` / `SystemContext` edits
  (and the user's `simplify.md` changes). **Re-run both first.**
- Passing when last run:
  - `SerializerTest`
  - `ShutdownHookTest`
  - `WebsocketListenerIT`, `RestListenerIT`, `TcpListenerIT`, `UdpListenerIT`
  - `ProtocolAnnotationTest`, `ProtocolBuilderTest`, `ProtocolDescriptionTest`
- **Not yet verified:** the wider run over storage, context and listener tests. The last attempt hung (cause above,
  since fixed), then failed with "Build failed with an exception" before the cause was captured. A stale test JVM
  holding a lock is suspected; run `gradlew --stop` first.
- Abstract bases can't be run directly: `ProtocolTest`, `ListenerTestCases`, `MapTestCases`.
- `MongoDBMapIT` and `ElasticMapIT` need running MongoDB/Elasticsearch.

## Queued work

1. ~~**Finish the handler → future migration.**~~ Done, see "Adapting consumers" and "Internal changes".
2. **HTTP listeners:** add HTTP/3 and adopt the Vert.x 5 `*Config` APIs (`HttpServerConfig`, `TcpServerConfig`, ...).
   `ListenerSettings.getHttpOptions()` already picks HTTP/3 when secure and HTTP/2 otherwise. Verify and complete
   that.
3. **One shared listener config key:**
   - Each listener's settings live under one JSON key. `ListenerSettings` deserializes that key into the right Vert.x
     config (`TcpServerConfig`, UDP, `HttpServerConfig`, ...).
   - Idea: each `CoreListener` provides the config class it expects, e.g. `Class<?> configType()`.
4. ~~**New QUIC listener.**~~ Done (raw QUIC), see "New: `QuicListener`". Still open for HTTP/3 (item 2): test that
   HTTP/1.1 and HTTP/2 work without SSL, and that HTTP/3 fails clearly without it.
5. ~~**Benchmark report template.**~~ Done, see "Benchmark report redesign".
