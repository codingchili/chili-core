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
| `listener.transport` | `RestListener`, `WebsocketListener`, `TcpListener`, `UdpListener`, `ClusterListener` and their `*Request` types |
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

## Building

Gradle wrapper, Java toolchain 27 (see root `build.gradle`).

```console
gradlew build -x test     # compile + package, skip tests
gradlew compileJava       # main sources only
gradlew compileTestJava   # compile tests without running them
gradlew test              # run tests
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

## Async conventions (important — the codebase is mid-migration)

The project has been upgraded to **Vert.x 5**. The direction is to **return `Future<T>`
instead of accepting `Handler<AsyncResult<T>>` callbacks**.

- New and migrated APIs return `Future<T>`. Compose with `compose`, `map`, `transform`,
  `onComplete`/`onSuccess`/`onFailure`. Use `Promise<T>` only where you need to complete a future
  manually (e.g. `CoreDeployment.start(Promise)`, storage plugin constructors).
- `FutureHelper.result(..)` / `FutureHelper.error(..)` create succeeded/failed futures.
- Blocking work goes through `CoreContext.blocking(Callable<T>)` / `blocking(Runnable)`
  (optionally `ordered`), which return a `Future`. The old `blocking(Handler<Promise>, Handler<AsyncResult>)`
  overloads were removed.
- Storage (`AsyncStorage`, `QueryBuilder.execute()`, `StorageLoader.build()`) and `CoreContext.close()` are
  fully future-based. Other handler-taking APIs may remain; when touching them, prefer migrating to `Future`
  and update all callers, tests and `docs/`.
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

## Known in-progress work (as of the Vert.x 5 upgrade)

- Remaining `Handler<AsyncResult>` based APIs outside storage (e.g. `BenchmarkImplementation`,
  `HashFactory.verify`) are candidates for migration to `Future`.
- HTTP/3 and the new `HttpConfig`/`TcpConfig` style APIs are not adopted yet.
