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

## Typed routes: a spike, and whether it has legs

`future.md` proposed `public Future<AccountView> get(GetAccount input)` instead of reading the request and writing the response. The
spike is real code, opt-in and additive: `protocol.TypedRoute`, hooked into `Protocol.wrap`, documented in `docs/protocol.md` ("Typed
routes (experimental)"). A method annotated `@Api` whose first parameter isn't a `Request` is a typed route:

- The input is deserialized from `request.data()` (`Serializer.json.convertValue`); an optional second parameter takes the request (the
  wrapper, if the protocol was given one). A record can validate itself in its compact constructor: an `IllegalArgumentException` is
  answered with `BAD` and its message; a value of the wrong type with `BAD` "invalid input: ...".
- A returned `Future<R>` is written when it succeeds and answered as an error when it fails; a returned value is written; `void`,
  `Future<Void>` and null are answered with `ACCEPTED`. Exceptions are answered as for any route (`ValueMissingException` is `MISSING`, `CONFLICT`
  stays `CONFLICT`, anything else `ERROR`). Roles work as before. The class of the input becomes the model of the route in the
  documentation (`Serializer.describe`), `@DataModel` overrides it. Routes that read the request keep working in the same handler.
- A method with other parameters is rejected when the protocol is created, with a message.
- **Tests:** `TypedRouteTest` is the sample (an account handler with a record for each input) and 18 tests: future written / failed
  with its status, validation in the record, wrong type, the envelope fields of the request ignored, request as second
  parameter and as a wrapper, returned value, void, `Future<Void>`, roles, thrown exception, legacy route in the same handler,
  documentation model, query-string style values (`"7"` for an int), an unsupported signature.
- **Cost** (one JVM, steady state, per request through `Protocol.process`, route that only accepts): floor about 38 ns, a route that
  does `Serializer.unpack(request.data(), Input.class)` itself about 190 ns, the typed route about 250 ns. Typed input costs what
  hand-written deserialization costs (about 150 ns for a small record) plus about 60 ns for the reflective call and the
  response handling: negligible next to I/O.

**Verdict: yes, it has legs, build on it.** What it buys, in order of value:

1. *Documentation and OpenAPI for free.* The input type is known from the signature (tested), the result type is too
   (`TypedRoute.result` already extracts it, it isn't wired into `Route` yet), and so are the roles and `@Description`. `OpenAPIGenerator` is
   a stub with hardcoded values, this is the data it needs. Handlers that read the request can't offer this.
2. *Validation and safety by construction.* The input is exactly the declared fields (no accidental mass assignment), a record
   validates itself, and every failure maps to a status. The boilerplate (unpack, try/catch, write, error) disappears: `get` in the
   sample is 4 lines.
3. *Plain functions.* The methods can be unit tested without a request mock: `handler.get(new GetAccount("1"))`.

What the spike found that limits it, and what to do about each:

- **The envelope shares the object with the input.** `route`, `target` and `token` (and for REST the query string and path segments) are
  in the same JSON object, so the input can't be strict (a misspelled field is ignored, unlike the listener configuration), and an input
  field named `route`, `target` or `token` collides. Fix: a nested `data` object for typed routes, or a strict mode that only ignores
  the envelope names.
- **Only `ACCEPTED` on success.** "Created", "no content" and redirects need a way to say so: a return type such as `Created<T>`, or an annotation, or take the
  request. (Also the missing piece for real HTTP status codes in `RestRequest`.)
- **Input is an object.** A list or a single value as the body isn't representable (`RestRequest` can't read arrays either).
- **No compile-time check of routes**, as before: route names are method names and are found by reflection. `Method.invoke` is
  fine: see the dispatch benchmark below, `LambdaMetafactory` isn't needed.
- **Registration errors** for unsupported signatures are `IllegalArgumentException`s thrown from `Protocol.annotated`, which is
  in the constructor of handlers.

Suggested order if it's kept: (1) wire the result type into `Route` and make `OpenAPIGenerator` real, served from the REST listener; (2) the
envelope question; (3) status for success; (4) promote it out of "experimental" and write the migration notes for handlers.

## Listener configuration works as documented (HTTP versions, `config` key, TLS)

Queued items 2, 3 and 4. Documented in `docs/listeners.md` ("HTTP versions and TLS", "Configuring the transport") and
`docs/security.md`. What the docs promised and the code didn't do:

- **HTTP versions.** `ListenerSettings.getHttpOptions()` set a plain listener to **HTTP/2 only** and a secure one to
  **HTTP/3 only**: no ordinary HTTP/1.1 client or probe could talk to a default `RestListener` (HTTP/2 without TLS needs prior
  knowledge), and HTTP/3-only has no TCP socket at all. Now HTTP/1.1 + HTTP/2, and HTTP/3 is added when the listener is secure
  (Vert.x 5.2's `HybridHttpServer` serves TCP and QUIC from one server). Versions that are configured are respected. HTTP/3 on a
  listener that isn't secure fails the deployment with "HTTP/3 requires TLS: ..." (Vert.x threw a bare `NullPointerException`).
  The default is no longer cached in the settings object, `secure` may change after it's read.
- **TLS options and ALPN.** `ListenerSettings.setSecurity(ServerSSLOptions)` was silently ignored, and `alpn` was never
  applied (so HTTP/2 over TLS wasn't negotiated). `setSecurity` is honored (secure listeners only), `useAlpn(alpn)` is
  applied and `alpn` defaults to `true` now (it was `false`).
- **The "one shared listener config key"** (item 3): new `ListenerSettings.config` (`getConfig()/setConfig(JsonObject)`), the
  transport settings, read by each listener as the class it declares in the new `CoreListener.configType()` (default `null`;
  `HttpServerConfig` for `RestListener` and `WebsocketListener`, `TcpServerConfig`, `QuicServerConfig`,
  `DatagramSocketOptions` for `UdpListener`, which didn't have any transport settings before) through
  `ListenerSettings.config(Class, Supplier)`. Parsing is **strict**: a misspelled property fails the deployment with
  "Invalid listener configuration, expected the properties of TcpServerConfig: Unrecognized property "idleTimout"". The
  typed getters (`getTcp()`, `getHttpOptions()`, `getQuic()`, new `getUdp()`) use it, and objects set in code still win.
  Works from YAML (`config:` key); durations are ISO-8601 or a number of seconds. `HttpServerConfig` needed a Jackson mix-in:
  it has two `setVersions` overloads that Jackson can't choose between.
- **Failed listeners hung the deployment.** `RestListener`, `WebsocketListener`, `TcpListener` and `UdpListener` created their
  server inside an `onSuccess` callback, so an exception there (an invalid configuration, a missing keystore) was swallowed and the
  deployment never completed (tests timed out, and the deployment later reported "Verticle un-deployed" without the cause). They build
  the server inside `compose` now, the deployment fails with the cause. `CoreVerticle` also fails the deployment when `start` or
  `stop` throws.
- **The auto-generated test certificate didn't work** (found with the QUIC work, `security.md`): Netty's generator needs JDK internals
  that are gone ("OpenJdkSelfSignedCertGenerator not supported on the used JDK version") or BouncyCastle, and the one test of it was
  `@Ignore`d for that reason. `TestCertificate` now creates the certificate with the JDK only (`SelfSignedCertificates`: the
  ASN.1 structures encoded by hand, RSA 2048, SHA256withRSA, signed with `java.security.Signature`, parsed and verified by the
  JDK's `CertificateFactory` before it's used; no new dependency). Valid for a year, for localhost and the loopback addresses (and for
  the name when it's a host name, so clients that verify host names can connect), PEM files in the temp directory, deleted at exit.
  The generated name is `localhost` instead of the URL of the GitHub profile.
- **Docs fixed:** the custom listener example implemented `settings(Supplier)`/`void handler(..)` (it's `CoreListener
  settings(ListenerSettings)` and `CoreListener handler(CoreHandler)`), and `context.listener(RestListener::new)` in `security.md`
  couldn't work without a handler.
- **Tests:** `ListenerSettingsTest` (17: default versions, HTTP/3 added with TLS and rejected without, configured versions kept,
  YAML, nested config, misspelled properties and invalid values, set objects win, round trip, ALPN, `configType` of every listener),
  `HttpVersionsIT` (9, **real clients**: HTTP/1.1 and HTTP/2 without TLS, HTTP/1.1, HTTP/2 via ALPN and HTTP/3 over QUIC with TLS,
  HTTP/3 without TLS and a misspelled property fail with a clear message, configured versions change what is served) and
  `TestCertificateTest` (structure, alternative names, PEM round trip, TLS handshake with host name verification against both
  `127.0.0.1` and `localhost`, an untrusting client is rejected). The `@Ignore` is removed from
  `SecuritySettingsTest.loadKeysFromSelfSigned`.
- **Test gotchas found:** a Vert.x `HttpClient` that is only referenced from a lambda can be garbage collected, which closes its
  pool mid-request ("Pool closed", intermittent, only when other tests allocated enough). Keep the client for the length of the
  test (`HttpVersionsIT` and `StatusServiceTest` do now). With port 0 the TCP and UDP sockets of a hybrid HTTP server get
  different ports: HTTP/3 tests need a fixed port.
- **Later changes:** `ListenerSettings.type` and the `WireType` enum are removed (no listener used them; the listener class decides
  the transport; a `type:` key in a listener's YAML is now ignored or rejected depending on the parser). `DEFAULT_MAX_REQUEST_BYTES`
  is 64 KiB (was 1 KiB), listeners that set `maxRequestBytes` are unaffected.
- **Not done:** HTTP/3 isn't advertised with `Alt-Svc` headers by chili-core (Vert.x's hybrid server decides); QUIC load balancing
  (`SO_REUSEPORT`) is still Linux/macOS only.

## New: `StatusService` (readiness endpoint, status API and status page)

Documented in `docs/status.md`. A `CoreService` (`core.service(() -> new StatusService()...)`) that serves, on a port of its
own (8081, no TLS by default, `ListenerSettings` for port/TLS, `bind(host)` for the interface):

- `/health/live` (200 while the process responds) and `/health/ready` (**200 or 503**, so that probes and load balancers
  can see it; `RestListener` always answers 200, see `future.md`). Readiness is the result of named
  `ReadinessCheck`s (`Future<Void>`, failed means not ready, a throwing or hanging check, 2 s timeout, also means not ready)
  that run concurrently, plus a built-in `shutdown` check that fails when `CoreContext.isShuttingDown()`.
- `/status`: JSON report (`StatusReport`) with `state`, `ready`, application (version, host, pid, uptime), readiness, JVM (heap, non-heap,
  threads, GC, CPU, OS), vertx (clustered, pool sizes, deployments, running blocking tasks), a whitelist of settings
  (counts and timeouts only), and the metric collector's snapshot when metrics are enabled.
- `/`: a dashboard in one self-contained HTML file (`resources/status/status.html`) that polls `/status`: state, KPIs,
  readiness table, memory bars, GC, vertx, JVM, settings and a filterable metrics table; light and dark themes. Status is
  always shown as an icon and a label, not only a color. Checked in headless Edge in both themes.
- **Security choices:** no CORS headers (`RestHelper.addHeaders` sends `*`, which would let any site read the report from a
  browser on the same network), `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Cache-Control: no-store`, HSTS with TLS. The page is
  served with a CSP that only allows its own script and style through a per-response nonce (`default-src 'none'`,
  `connect-src 'self'`, no `unsafe-inline`), all DOM is built with `textContent` (a check message containing `<b>` is shown
  as text), only GET/HEAD (405 with `Allow` otherwise), `details(false)` serves only the health endpoints. There is no
  authentication: it's an administration interface, see the docs.
- The server uses the Vert.x defaults (HTTP/1.0, 1.1 and 2). (`ListenerSettings.getHttpOptions()` used to narrow listeners to HTTP/2
  only, or HTTP/3 only when secure, which ordinary clients and probes can't speak. Fixed, see "Listener configuration works as
  documented".)
- **New `shutdownDelay` system setting** (default 0, `SystemSettings.setShutdownDelay`): services keep running for this long
  after the context starts shutting down (readiness is failing), before they're stopped. The delay counts towards
  `shutdownHookTimeout`. Without it a load balancer has no time to notice the readiness change.
- New `CoreContext` default methods `isShuttingDown()` and `blockingTasks()`.
- **Tests:** `StatusServiceTest` (17: liveness; ready; failing, throwing and hanging checks; shutting down but alive; HEAD;
  report sections and values; metrics when enabled; page nonce unique per response and CSP without `unsafe-inline`;
  nothing loaded from other sites; security headers and no CORS; 405; 404; details off; server closed on stop) and
  `ShutdownHookTest.servicesKeepRunningDuringTheShutdownDelay`.
- **Not done:** authentication, a drain that waits for in-flight HTTP requests of other listeners, readiness checks provided by
  the storage plugins and cluster manager (`future.md` has the idea), Prometheus/OpenTelemetry export, a launcher option
  to deploy the service from configuration.

## Graceful shutdown fixed; the token of a request is parsed once

Graceful shutdown (`ShutdownHook`, `SystemContext`) had five problems:

- **It always took the full `shutdownHookTimeout` (5 s by default).** The hook polled a counter for the whole timeout and
  only left early when closing vertx failed. It now waits for the shutdown to complete (a latch) and returns as soon as it
  has, bounded by the timeout so that the JVM can't hang on exit. A test shows a hook with a 5 s timeout finishing in
  well under 3 s.
- **Running blocking tasks were interrupted, not awaited.** The code that waited for them was commented out when the
  blocking API was migrated to futures, and Vert.x's `close()` calls `shutdownNow()` on the worker pools, which
  interrupts running tasks. This was the cause of `ShutdownHookTest.blockingPoolAwaited` failing now and then
  ("Task interrupted!"), which is deterministic now. `SystemContext.blocking(..)` now counts the tasks that are running
  (`BlockingTasks`, shared with contexts created from it, such as `StorageContext`). The shutdown order is: publish the
  shutdown event → stop deployments → wait for blocking tasks until the timeout → close vertx. Tasks that are still
  running when the time is up are interrupted, and a warning with their number is logged. Blocking work that doesn't go
  through `CoreContext.blocking` (for example the worker executors of the storage plugins) is not tracked.
- **An explicitly closed context kept its JVM hook.** `unregister` existed but was never called, so at exit the hook ran
  against a closed vertx. That printed a `RejectedExecutionException` stack trace on every exit of the
  `gradlew benchmark` and the launcher, and every context ever created (hundreds in a test run) stayed referenced in the
  static hook map. `SystemContext.close()` now unregisters, and a hook that finds its context already closed does
  nothing.
- **Logging could throw during shutdown.** `RemoteLogger.log` sent over the event bus without a guard. It now logs to the
  console instead when the bus is closed.
- **The shutdown state is visible:** `CoreContext.isShuttingDown()` (default method, `false` for other
  implementations), shared by contexts that share a vertx instance. Used by the readiness endpoint.
- Also: `ShutdownHook`'s static map was cleared without the lock, a failing shutdown listener or service no longer
  stops the rest of the shutdown (it's logged), and the system settings are reset by `ShutdownHookTest.setUp` as
  they are shared by all tests (a test that set a 25 ms timeout used to leak it into the next).
- **Tests** (`ShutdownHookTest`, 14 tests, 3 consecutive runs without a failure): finishes without waiting for the timeout,
  running blocking tasks complete before vertx closes and aren't interrupted, an explicit close unregisters, a closed
  context isn't shut down again, derived contexts see the state, logging after close doesn't fail.
- **Not done:** requests that are in flight when listeners are stopped are drained by Vert.x's undeploy, not by
  chili-core, there's no configurable drain delay (for load balancers to notice the readiness change), and storage
  plugins aren't closed explicitly (the SQLite persistence of `IndexedMapPersisted` is only released on exit).

**`Request.token()` is cached per request.** It deserialized the token on every call, and the authenticator and the handler
both read it. A probe measured 510 ns per call, against about 0 for a cached read: more than 20 times the cost of dispatching
the route itself (see the benchmark below), per extra call.

- New `listener.AbstractRequest` caches the token (`volatile`, parsed on first use). `ClusterRequest`, `RestRequest`,
  `TcpRequest`, `UdpRequest`, `WebsocketRequest` and `QuicRequest` extend it. `RequestWrapper` delegates, so it's cached too.
  The default `Request.token()` is unchanged for custom `Request` implementations (parsed on every call). New static
  `Request.parseToken(JsonObject)`.
- **Behavior changes:** the same `Token` instance is returned for the same request, `Token` is mutable (`TokenFactory`
  sets the key when signing), so copy it before modifying; the token is parsed once, changes to `request.data()` after the
  first call are not seen; a request without a token returns the same random expired token on every call (it was a new random
  one on each).
- Tests: `AbstractRequestTest`.

## Benchmark: how the protocol invokes routes (`Method.invoke` vs `MethodHandle` vs `LambdaMetafactory`)

- **Run it:** `gradlew benchmark -Psuite=protocol -Piterations=300` (new `--suite maps|protocol|all` parameter for the
  `benchmark` launcher command, default `maps` as before; `-Psuite` for the Gradle task). The protocol suite
  needs no cluster. Documented in `docs/benchmarking.md`.
- **New classes:** `ProtocolDispatchStrategy` (the three strategies as an enum: bind a method and a handler once,
  invoke per request, exceptions are rethrown unwrapped like `Protocol.invokeMethod` does) and
  `ProtocolBenchmarkImplementation` (the benchmark, a `DispatchHandler` that counts calls, and
  `group(iterations)`). Besides the three strategies it measures a **direct call** (baseline) and
  **`Protocol.process` end to end**, for context. Operations: route with a request, route without a request, and
  registering the 2 routes of a handler.
- **Method:** each iteration is a batch of 100,000 calls (20 binds for registration). All implementations run through
  the same loops, which are first run with every strategy so that the call site is megamorphic for all of them (as in
  a protocol with many routes), otherwise the first implementation would get an inlined call site and win by order.
  Every implementation verifies after the run that the handler received exactly the calls that were made, and the run
  fails if not.
- **Result** (Windows 11, 24 cores, JDK 27, three runs of 300 iterations, medians. Runs differed by up to 20% for calls and more for registering routes,
  up to 70% for `MethodHandle`, the order of the strategies was the same in every run):

  | | Method.invoke | MethodHandle | LambdaMetafactory | Direct call | Protocol.process |
  |---|---|---|---|---|---|
  | Route with request | 4.1 ns | 3.4 ns | 1.6 ns | 1.5 ns | 23 ns |
  | Route without request | 3.8 ns | 2.6 ns | 1.4 ns | 1.5 ns | 22 ns |
  | Register 2 routes | 154 ns | 1.7 µs | 34 µs | 45 ns | 777 ns |

- **Conclusion: not worth switching `Protocol` away from `Method.invoke`.** `LambdaMetafactory` is 2.5 ns faster per
  call (about 10% of `Protocol.process`, and far less of a real request) at the price of a generated class per route
  (about 17 µs, repaid after about 7,000 calls) and more code. `MethodHandle` is neither faster nor cheaper. If a
  later profile shows dispatch matters, `LambdaMetafactory` works for private routes on package private handlers and for
  request subtypes (see `ProtocolDispatchStrategyTest`), a fallback to `Method.invoke` is still needed for what it
  can't bind. Not measured: startup effect of many generated classes (metaspace), cold calls, JDK 17 and earlier
  (where `Method.invoke` is much slower).
- **Framework changes to support it:** `BenchmarkBuilder.setOperationsPerIteration` and
  `BenchmarkImplementationBuilder.add(name, operationsPerIteration, operation)`: the rate counts the operations in a
  batch. `ResultItem.getOperations()` and the report's "Measured operations" figure (was "Measured calls") now
  count operations, not iterations.
- **Tests:** `ProtocolDispatchStrategyTest` (7 checks × 3 strategies: same method and handler, no-argument routes, private
  routes, request subtypes, checked and unchecked exceptions arrive unwrapped) and `ProtocolBenchmarkTest` (the whole group
  runs, the rate is calls per second, a handler that doesn't receive its calls and a binding that throws both fail the run).

## Configuration hot reload can be disabled

- New system setting `configurationReload` (`SystemSettings.setConfigurationReload`, default `true`, so behavior is
  unchanged). With `configurationReload: false` in `system.yaml`:
  - No file watcher is started at all (also no polling), a `reload.disabled` log event is emitted at startup
    (`Logger.onReloadDisabled`, new method on the `Logger` interface, implemented in `AbstractLogger`).
  - `system.yaml` itself isn't watched either, so once disabled hot reload can't be turned back on at runtime by
    editing files. A restart is needed.
- If it's turned off at runtime (`setConfigurationReload(false)`) while the watcher already runs, file events are
  ignored: all reloads go through the new package-private `Configurations.onChanged(path)`, which checks the setting.
  The watcher keeps polling, it just does nothing.
- Why: with hot reload anything that can write to the configuration directory (security settings, keystores, listener
  config, ...) changes a running application. It's recommended to disable it in production. Documented in
  `docs/configuration.md`, "Reloading configuration".
- Tests (`ConfigurationsIT`): a change on disk is applied when enabled and ignored when disabled, and the setting can be
  read from the configuration file. **Not covered by a test:** that the watcher isn't created at startup when disabled
  (the `StartupListener` in `Configurations` is static and tied to a running context). That path is a single `if`.
- Not changed: `cachedFilePoll`, the polling of files cached by `CachedFileStore`, is a separate feature and always on
  for stores that use it.
- Possible follow-up: also allow `-Dchili.configurationReload=false` or an environment variable, so a read-only
  container image can switch it off without touching the file (see `future.md`, configuration overrides).

## `prettyEncoding` off by default

- `SystemSettings.prettyEncoding` now defaults to `false`: JSON responses, event bus payloads and anything else written
  through `Serializer.json` are compact. `Serializer`'s startup mapper no longer enables `INDENT_OUTPUT` either. Both
  had to change: the setter only runs when a config file contains the key, so changing only the field would have left
  indentation on until then. Set `prettyEncoding: true` in `system.yaml` (or `setPrettyEncoding(true)`) to get the old
  output. New test: `SerializerTest.prettyEncodingIsOffByDefault`.
- **Existing deployments keep their setting.** The framework saves the configuration to `conf/system/system.yaml` on
  shutdown, including `prettyEncoding: true`, so only installs without a saved file see the new default.
- **Token signatures depend on this setting (found, not fixed).** `TokenFactory.canonicalizeTokenWithCrypto` signs
  `Serializer.buffer(token.getProperties())`, so the signed bytes differ between pretty and compact output. A token
  issued by a service with one setting fails verification on a service with the other. Changing the default can
  therefore invalidate tokens between services during a rollout, if some run with a saved `prettyEncoding: true` and
  others don't. Fix together with the High canonicalization finding in `security.md`: sign a canonical form that
  doesn't depend on a global setting (for example length-prefixed parts, with a dedicated compact mapper).
- `ShutdownHookTest.blockingPoolAwaited` failed once ("Task interrupted!") in a full run and passed 3 of 3 when run on
  its own. It's timing based, treat it as flaky.

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
- **`Serializer.json(Buffer)` (bug fix):** passing a `Buffer` fell through to Jackson 3's `convertValue` as a bean and
  failed with `MismatchedInputException` ("Cannot deserialize `LinkedHashMap` from String"). That broke
  `ClusterRequest.write(Buffer)` (`RequestMockTest.testReplyWithBuffer`, `testErrorStatusInBuffer`). A buffer is now read
  as JSON text, matching `Serializer.buffer(Object)`, which already passes a `Buffer` through. New test:
  `SerializerTest.testBufferToJson`.
- **Full suite: green.** 724 tests, 0 failures, 142 skipped (integration tests that need MongoDB/Elasticsearch, and
  `@Ignore`d bases). Earlier in this work: 9 failures, then 2.
- **Don't run two test JVMs in this project at the same time.** `MapTestCases` stores its SQLite database at a fixed
  relative path (`core/IndexedMapPersisted/MapTestCases.sqlite`), so two concurrent runs (for example IDE and terminal)
  corrupt each other: a run overlapping with another one failed 5 `IndexedMapPersistedTest` tests and left the files
  behind. Giving each test JVM its own database directory would fix it.

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
- **Full suite, after the shutdown, token, status service, listener configuration and typed route work:** 834 tests, 0 failures, 141
  skipped (integration tests that need MongoDB/Elasticsearch, and abstract bases), no files left in the working tree. Do not run two
  test JVMs in this project at the same time (see "Tests clean up after themselves").

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
2. ~~**HTTP listeners:** HTTP/3 and the Vert.x 5 `*Config` APIs.~~ Done, see "Listener configuration works as documented".
3. ~~**One shared listener config key.**~~ Done, same section (`ListenerSettings.config`, `CoreListener.configType()`).
4. ~~**New QUIC listener.**~~ Done (raw QUIC), see "New: `QuicListener`". The HTTP/1.1, HTTP/2 and HTTP/3 tests are
   done too (`HttpVersionsIT`).
5. ~~**Benchmark report template.**~~ Done, see "Benchmark report redesign".
