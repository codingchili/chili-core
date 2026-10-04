# AGENTS.md

Orientation for AI agents and human contributors working on **chili-core**. For the user-facing
introduction see [README.md](README.md); for full documentation see [docs/](docs/index.md).

## What this project is

chili-core is an opinionated Java framework for building microservices quickly. It wraps
[Vert.x](https://vertx.io/) (event loop, event bus, networking) and adds:

- a deployment model (`CoreContext` → services → listeners → handlers),
- transport-independent request handling (REST, WebSocket, TCP, UDP, cluster event bus),
- a routing/protocol layer driven by annotations (`@Address`, `@Api`, `@Role`),
- pluggable, async storage with a common query API (in-memory, CQEngine, JSON file, Hazelcast, MongoDB, Elasticsearch),
- file-backed configuration with hot reload, logging, metrics, security (tokens, argon2 hashing, keystores) and benchmarking.

It is published via JitPack as `com.github.codingchili.chili-core:core:<version>`.

## Repository layout

```
build.gradle              root build: shared config for subprojects, javadoc tasks
core/build.gradle         the only subproject; dependencies live here
core/main/java/...        sources   (note: NOT src/main/java)
core/main/resources/      cluster.xml (hazelcast), logging config, benchmark templates
core/test/java/...        tests     (note: NOT src/test/java)
core/test/resources/      test fixtures (configs, keystores)
docs/                     GitHub pages documentation (markdown) + generated javadoc
```

Packages under `com.codingchili.core`:

| Package | Purpose |
|---|---|
| `context` | `CoreContext`/`SystemContext` (entry point, deploys things, timers, blocking work), `StorageContext`, `FutureHelper`, launcher commands |
| `listener` | `CoreService`, `CoreListener`, `CoreHandler`, `Request`, sessions, `BusRouter` |
| `listener.transport` | `RestListener`, `WebsocketListener`, `TcpListener`, `UdpListener`, `QuicListener`, `ClusterListener` and their `*Request` types |
| `protocol` | `Protocol` (maps routes to handler methods), annotations (`@Api`, `@Address`, `@Role`, `@Description`), `Serializer` (JSON/Kryo), `Response` |
| `configuration` | `Configurable` models, `CoreStrings` (all constants/messages), `system.*` settings (`SystemSettings`, `StorageSettings`, `LauncherSettings`, ...) |
| `files` | `Configurations` (load + cache + hot reload of configs), `ConfigurationFactory`, file watching, cached file stores |
| `storage` | `AsyncStorage` and implementations, `StorageLoader`, `QueryBuilder`/`Query`, `Storable` |
| `security` | `Token`/`TokenFactory`, `HashFactory` (argon2), `KeyStoreBuilder`, `Validator` |
| `logging` | `Logger`, `ConsoleLogger`, `RemoteLogger`, `Level` |
| `metrics` | Dropwizard-based metric collection |
| `benchmarking` | Benchmark framework (also used to benchmark storage implementations) |
| `testing` | Mocks and fixtures usable by downstream projects (`ContextMock`, `RequestMock`, `StorageObject`, ...) |

`com.codingchili.core.Launcher` is the jar main class; it deploys "blocks" of services configured in
`conf/system/launcher.yaml`.

## Working efficiently (read this first)

Context and tokens are the scarce resource: a long session here costs mostly in tool output and file reads, not in
thinking. These rules are mandatory for agents.

**Tests and builds**
- **NEVER run the full test suite** (`gradlew test`, `:core:test` without `--tests`, `build`). It prints thousands of log
  lines, takes 1.5+ minutes and collides with a test run in the IDE. Run only the classes or packages that your change can
  affect: `--tests "com.codingchili.core.listener.*"`. Run a wider set only when the user asks, and then report the counts only.
- **Minimal output, always.** Use `-q` and filter. Never print a raw Gradle or test log. Compile check:
  `gradlew.bat compileJava -q 2>&1 | grep -E "error" -A3 | head -20`. Test result: `... -q 2>&1 | grep -E "FAILED|error:" | head`, and
  then the counts from `core/build/reports/tests/test/index.html` (`<div class="counter">`: tests, failures, ignored).
  Failure details: open only the failing test's html in `core/build/reports/tests/test/<class>/<test>.html` and print its
  first line.
- The framework logs a lot to the console (benchmarks, shutdown, listeners): add `grep` for what you need. When you
  need test output use `-i` and `grep PROBE`, with temporary `System.err.println("PROBE ...")` lines that you remove after.
- Temporary probes and throwaway tests (timing, debugging) are fine, but delete them before you finish, and check
  `git status --short | grep -v "^ M"` for litter.
- Run one Gradle test JVM at a time, and never while the IDE might be running tests: tests share files (the persisted map's
  SQLite file, `core/conf`) and `core/build`. Look for `Gradle Test Executor` java processes if a run fails strangely.
- Do not re-run a passing command to "verify" it, and do not run benchmarks or screenshots unless the change needs them.

**Reading and searching**
- Do not read whole files or directories. `grep -n` for the symbol first, then `sed -n 'a,bp'` or `Read` with `offset`/`limit`
  for just that range. List with `ls` or `grep -l`, not by reading.
- Never read `docs/javadoc/` (generated), `core/build/`, `*.lock`, or large generated files. `CHANGELOG/*.md` are long: read
  the headings (`grep -n "^## " CHANGELOG/upgrades.md`) and only the section you need.
- Use `grep -c` or `wc -l` to size output before printing it, and `head`/`tail` on anything that can be long.
- For a broad question that needs many files ("where is X used"), delegate to a subagent and use its summary.
- Don't re-read a file you just edited, the edit tool reports failures.

**Editing**
- Make targeted edits (`Edit`, or a small script) and don't rewrite files to change a few lines. Keep each file's line endings:
  most of the repo is CRLF, scripts that read and write must preserve them.
- Shell heredocs that contain quotes or backslashes get mangled by the tool layer (`\n` becomes a real newline,
  quotes can end the command). Write files and multi-line scripts with the Write tool, and run them.
- Keep the changelog entries and your answers short and factual: what changed, what was verified, what is still open. Don't
  paste logs, diffs or file contents back to the user.

## Building

Gradle wrapper, Java toolchain 27 (see root `build.gradle`).

```console
gradlew build -x test     # compile + package, skip tests
gradlew compileJava       # main sources only
gradlew compileTestJava   # compile tests without running them
gradlew test --tests "com.codingchili.core.listener.*"   # run tests: ALWAYS select tests, see above
```

On Windows use `.\gradlew.bat`. Tests named `*IT` are integration tests; some need external
services (MongoDB, Elasticsearch) or clustering, so expect those to fail without that infrastructure.

## Core programming model

```java
CoreContext core = new SystemContext();          // owns the Vertx instance

core.listener(() -> new RestListener()           // a listener receives requests from a transport...
        .settings(new ListenerSettings().setPort(8080))
        .handler(new MyHandler()));              // ...and hands them to a handler

@Address("api")                                  // route prefix / event bus address
public class MyHandler implements CoreHandler {
    private final Protocol<Request> protocol = new Protocol<>(this);

    @Api                                         // exposed as route "list"
    public void list(Request request) {
        request.write(List.of("hello", "world"));
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);               // dispatch by request.route()
    }
}
```

- `CoreService`, `CoreListener` and `CoreHandler` all extend `CoreDeployment`
  (`init(CoreContext)`, `start(Promise<Void>)`, `stop(Promise<Void>)`).
- `Request` is transport-agnostic: `data()` (JSON), `route()`, `target()`, `token()`,
  `write(Object)`, `error(Throwable)`, `result(AsyncResult)`.
- Throw/return subclasses of `CoreException`/`CoreRuntimeException` to send a mapped error status to the client.

## Async conventions

The project has been upgraded to **Vert.x 5**. Public APIs **return `Future<T>`
instead of accepting `Handler<AsyncResult<T>>` callbacks** or a `Promise` to complete.

- New and migrated APIs return `Future<T>`. Compose with `compose`, `map`, `transform`,
  `onComplete`/`onSuccess`/`onFailure`. Use `Promise<T>` only where you need to complete a future
  manually (e.g. `CoreDeployment.start(Promise)`, storage plugin constructors).
- `FutureHelper.result(..)` / `FutureHelper.error(..)` create succeeded/failed futures.
- Blocking work goes through `CoreContext.blocking(Callable<T>)` / `blocking(Runnable)`
  (optionally `ordered`), which return a `Future`. The old `blocking(Handler<Promise>, Handler<AsyncResult>)`
  overloads were removed.
- Storage (`AsyncStorage`, `QueryBuilder.execute()`, `StorageLoader.build()`), `CoreContext.close()`,
  `HashFactory.verify`, commands (`Command`, `CommandExecutor.execute`) and benchmarking
  (`BenchmarkImplementation`, `BenchmarkOperation`) are future-based. Don't add new callback-style APIs; when
  changing an API, update all callers, tests and `docs/`.
- Never block the event loop; in-memory storages may complete futures synchronously, but callers must
  always treat results as asynchronous.

## Storage

```java
new StorageLoader<Account>(core)
        .withPlugin(IndexedMapVolatile.class)    // or SharedMap, PrivateMap, JsonMap, IndexedMapPersisted,
        .withValue(Account.class)                //    HazelMap, MongoDBMap, ElasticMap
        .withDB("db", "accounts")
        .build()
        .onSuccess(accounts -> {
            accounts.put(account)
                    .compose(v -> accounts.get(account.getId()))
                    .onSuccess(stored -> { /* ... */ });

            accounts.query("name").startsWith("rob")
                    .orderBy("level").order(SortOrder.DESCENDING)
                    .pageSize(10)
                    .execute()
                    .onSuccess(results -> { /* Collection<Account> */ });
        });
```

`AsyncStorage<Value extends Storable>` (all return `Future`):
`get`, `contains`, `put`, `putIfAbsent`, `remove`, `update`, `values` (`Stream`), `clear`, `size`;
plus `addIndex(field)`, `query()`/`query(attribute)` and `context()`.

Failure contract (tested in `MapTestCases`, keep it consistent across implementations):

| Operation | Fails with |
|---|---|
| `get` on missing key | `ValueMissingException` |
| `putIfAbsent` on existing key | `ValueAlreadyPresentException` |
| `remove` on missing key | `NothingToRemoveException` |
| `update` on missing key | `NothingToUpdateException` |

Implementing a storage plugin: implement `AsyncStorage<Value>` and provide a public constructor
`(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context)` — `StorageLoader` instantiates it
reflectively and waits for the promise. Values must implement `Storable` (`getId()`); array attributes in
queries/indexes use the `[]` suffix (`CoreStrings.STORAGE_ARRAY`). Storage tests extend `MapTestCases`.

## Configuration

Configuration classes implement `Configurable` and are loaded with
`Configurations.get(path, Type.class)`; missing files fall back to the class defaults, and loaded files are
reloaded when they change on disk. System configuration lives in `conf/system/`
(`launcher.yaml`, `system.yaml`, `security.yaml`, `storage.yaml`), service configuration in `conf/service/`.
YAML and JSON are both supported. Shared strings/paths/messages are in `CoreStrings`.

## Testing conventions

- JUnit 4 with `vertx-unit`: `@RunWith(VertxUnitRunner.class)`, `TestContext`, `Async async = test.async()`.
- Use `SystemContext`/`ContextMock`, `RequestMock`, `StorageObject` from `com.codingchili.core.testing`.
- Close contexts in `@After` (`context.close().onComplete(test.asyncAssertSuccess())`).
- `Configurations.reset()` is for tests only.

## Style notes for contributors

- Match the surrounding code: fluent builders returning `this`, Javadoc on public API, constants in `CoreStrings`.
- Keep `docs/*.md` in sync with public API changes (they contain code examples).
- Don't edit `docs/javadoc/` by hand; it is generated (`gradlew copyJavadoc`).

## Vert.x 5 / Jackson 3 gotchas

- Vert.x only calls `Deployable.deploy(Context)`; `CoreVerticle` therefore extends `VerticleBase`
  (whose `deploy` drives `init` → `start()`). Don't implement `Verticle` with a stub `deploy`.
- Passing a non-null `ServerSSLOptions` to `createHttpServer`/`createNetServer` enables TLS.
  `ListenerSettings.getSecurity()` returns `null` when `secure` is false.
- WebSocket clients are created with `vertx.createWebSocketClient().connect(...)`, not `HttpClient.webSocket`.
- Jackson 3 (`tools.jackson.*`) is used; Jackson 2 databind is excluded. Jackson 3's `StdDeserializer` rejects a
  `null` handled type. Vert.x's own databind features (`JsonObject.mapFrom`/`mapTo`, `Json.encode` of POJOs)
  require Jackson 2 and fail at runtime — always go through `Serializer` (`json`, `buffer`, `pack`, `unpack`).
- `Serializer` mappers are configured in `Serializer.configure` (non-null inclusion, no failure on empty beans or
  unknown properties); `SystemSettings.setPrettyEncoding` rebuilds `Serializer.json` with `INDENT_OUTPUT`.
- `ProtocolTest`, `ListenerTestCases` and `MapTestCases` are abstract bases; run their subclasses, not them.
- QUIC (`QuicListener`) needs the native `netty-codec-native-quic` jar (added per platform in `core/build.gradle`)
  and always TLS + ALPN. Vert.x QUIC clients allow 0 server-opened streams by default.
- A missing keystore falls back to a self-signed certificate generated with the JDK only (`TestCertificate`,
  `SelfSignedCertificates`): valid for localhost and the loopback addresses. Netty's own generator doesn't work on current JDKs.
- Listener transport settings are the `config` property of `ListenerSettings`, read as the class a listener declares in
  `CoreListener.configType()` and parsed strictly. Build servers inside `compose` in `start`: an exception thrown in an
  `onSuccess` callback hangs the deployment instead of failing it.
- In tests keep a reference to every Vert.x `HttpClient`/`HttpClientAgent` for the length of the test: a client that is garbage
  collected closes its pool, and a request fails with `Pool closed`.

## Known in-progress work (as of the Vert.x 5 upgrade)

- HTTP/3 and the `*ServerConfig` APIs are adopted (`ListenerSettings.config`, `CoreListener.configType()`). Open: no `Alt-Svc`
  headers from chili-core, QUIC load balancing (`SO_REUSEPORT`) is Linux/macOS only.
