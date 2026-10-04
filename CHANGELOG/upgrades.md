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
- **`ShutdownHookTest`** had a deadlock: `serviceStopOverridesTimeout` blocked the event loop inside `stop()` while
  waiting for Vert.x to close. Polling now runs on its own thread, and the class has a 10s `Timeout` rule.
- **`SystemContext.stop()`** clears tracked deployments, so later stops don't re-undeploy ("Already undeployed").
- **Docs:** `docs/storage.md` and `docs/context.md` examples now use the future API. `docs/storage.md` is back to
  CRLF line endings.
- **Repo docs:** `AGENTS.md` was added (agent/human orientation, Vert.x 5 gotchas), plus `CLAUDE.md` (`@AGENTS.md`).
- **Tests** were mechanically converted (`x(args, cb)` → `x(args).onComplete(cb)`), with assertions unchanged.

## Verification status

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

## Queued work (not started)

1. **Finish the handler → future migration:**
   - `HashFactory.verify(Handler<AsyncResult<Void>>, ...)` → `Future<Void> verify(String expected, char[] plaintext)`.
   - `BenchmarkImplementation` (`initialize(CoreContext, Handler<AsyncResult<Void>>)`, `reset(Handler<...>)` etc.)
     → futures.
   - Then sweep for any remaining `Handler<AsyncResult` in public APIs.
   - Update callers, tests, `docs/` and `AGENTS.md`.
2. **HTTP listeners:** add HTTP/3 and adopt the Vert.x 5 `*Config` APIs (`HttpServerConfig`, `TcpServerConfig`, ...).
   `ListenerSettings.getHttpOptions()` already picks HTTP/3 when secure and HTTP/2 otherwise. Verify and complete
   that.
3. **One shared listener config key:**
   - Each listener's settings live under one JSON key. `ListenerSettings` deserializes that key into the right Vert.x
     config (`TcpServerConfig`, UDP, `HttpServerConfig`, ...).
   - Idea: each `CoreListener` provides the config class it expects, e.g. `Class<?> configType()`.
4. **New QUIC listener.**
   - Tests: HTTP/1.1 and HTTP/2 work **without** SSL, while HTTP/3 and QUIC require `secure: true` with SSL config.
     Also cover the failure mode when that's missing.
5. **Benchmark report template:** redesign so it looks modern and professional, fit for a backend framework.
   Templates are in `core/main/resources/benchmarking`, rendered with jade4j.
