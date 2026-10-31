# AGENTS.md

Orientation for AI agents and human contributors working on **chili-core**. For the user-facing
introduction see [README.md](README.md); for full documentation see [docs/](docs/index.md).

This is a Java project.

IMPORTANT:
- This repository is Java, not TypeScript.
- Do NOT create .ts, .tsx, .js, or .jsx files.
- Do NOT introduce Node.js, npm, or TypeScript.
- Before modifying anything, inspect the existing implementation and build configuration.
- Prefer modifying existing Java files.
- Do not invent tools or commands.
- When uncertain, inspect the repository before making changes.
- After code changes run `compileJava`/`compileTestJava` and only the affected tests (see Working efficiently).

## What this project is

chili-core is an opinionated Java framework for building microservices quickly, wrapping
[Vert.x](https://vertx.io/) 5 (event loop, event bus, networking). Published via JitPack as
`com.github.codingchili:chili-core:<version>`. Entry point: `com.codingchili.core.Launcher`.

## Repository layout

- `core/main/java/...` sources and `core/test/java/...` tests (note: NOT `src/main/java`), base package `com.codingchili.core`.
- `core/main/resources/`, `core/test/resources/`: configs, keystores, fixtures.
- `docs/`: GitHub pages markdown + generated javadoc (never edit or read `docs/javadoc/`).
- `CHANGELOG/*.md`: change log, topic files (`upgrades.md`, `performance.md`, `security.md`, `simplify.md`, `future.md`); add a short `## ` section to the matching file for notable changes.

## Architecture (big picture)

- **Boot**: `Launcher` parses commands (`context/*CommandExecutor`) and deploys any `Verticle`, `CoreHandler`, `CoreService`
  or `CoreListener` class by name. `CoreContext`/`ServiceContext`/`SystemContext` wrap the Vert.x instance, timers, blocking
  work and the logger; `CoreVerticle` is the base for deployables.
- **Request path**: a `CoreListener` (transports in `listener/transport`: Rest, Websocket, Tcp, Udp, Quic, Cluster) turns wire
  data into a `Request` and hands it to a `CoreHandler`/`Receiver`. Handlers route via `protocol` annotations/classes
  (`@Address`, `@Api`, `Route`, `RoleMap`, `Authenticator`); `Serializer` (de)serializes. `BusRouter`/`BusForwarder` link
  handlers over the event bus; `ClusterHelper` handles clustering.
- **Storage**: `AsyncStorage<V>` is the single interface; implementations (`IndexedMap*`, `JsonMap`, `HazelMap`, `MongoDBMap`,
  `ElasticMap`, `SharedMap`, `PrivateMap`) are chosen by `StorageLoader` from `StorageContext` settings. Queries go through
  `QueryBuilder`/`Query`; indexed attributes need `AttributeRegistry` registration.
- **Config**: `Configurable`/`ServiceConfigurable` JSON files loaded via `configuration` (strings in `CoreStrings`); security
  (`TokenFactory`, `KeyStoreBuilder`, `HashFactory`) reads its settings from there.

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
- Never read `docs/javadoc/`, `core/build/`, `*.lock`, or large generated files. `CHANGELOG/*.md` are long: read
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

Gradle wrapper, Java toolchain 27. On Windows use `.\gradlew.bat`.

```console
gradlew compileJava                                      # main sources only
gradlew compileTestJava                                  # compile tests without running them
gradlew test --tests "com.codingchili.core.listener.*"   # ALWAYS select tests, see above
gradlew :core:test --tests "com.codingchili.core.storage.IndexedMapPersistedTest" -q   # single class
```

Tests named `*IT` are integration tests; some need external services (MongoDB, Elasticsearch) or clustering, so expect
those to fail without that infrastructure.

## Conventions

- Async APIs return `Future<T>` (Vert.x 5), never callbacks. Blocking work goes through `CoreContext.blocking(..)`.
  Never block the event loop. When changing an API, update all callers, tests and `docs/`.
- Storage failure contract (`MapTestCases`, keep consistent across implementations): `get` missing → `ValueMissingException`,
  `putIfAbsent` existing → `ValueAlreadyPresentException`, `remove` missing → `NothingToRemoveException`,
  `update` missing → `NothingToUpdateException`. Storage plugins have a public constructor
  `(Promise<AsyncStorage<Value>>, StorageContext<Value>)`, instantiated reflectively by `StorageLoader`.
- Tests: JUnit 4 + `vertx-unit`, helpers in `com.codingchili.core.testing`. Close contexts in `@After`.
  `Configurations.reset()` is for tests only.
- Match surrounding code: fluent builders returning `this`, Javadoc on public API, constants in `CoreStrings`.
  Keep `docs/*.md` in sync with public API changes.

## Vert.x 5 / Jackson 3 gotchas

- `CoreVerticle` extends `VerticleBase`; don't implement `Verticle` with a stub `deploy`.
- A non-null `ServerSSLOptions` enables TLS; `ListenerSettings.getSecurity()` is `null` when `secure` is false.
- Jackson 3 (`tools.jackson.*`); Jackson 2 databind is excluded. `JsonObject.mapFrom`/`mapTo` and `Json.encode` of POJOs
  fail at runtime: always use `Serializer` (`json`, `buffer`, `pack`, `unpack`).
- Listener transport settings are `ListenerSettings.config`, typed by `CoreListener.configType()` and parsed strictly.
  Build servers inside `compose` in `start`: an exception in an `onSuccess` callback hangs the deployment.
- QUIC needs the native `netty-codec-native-quic` jar (per platform in `core/build.gradle`), always TLS + ALPN.
  Vert.x QUIC clients allow 0 server-opened streams by default.
- A missing keystore falls back to a JDK-only self-signed certificate (`TestCertificate`); Netty's generator doesn't work on current JDKs.
- In tests keep a reference to every Vert.x `HttpClient` for the length of the test, or a GC'd client fails requests with `Pool closed`.
- `ProtocolTest`, `ListenerTestCases` and `MapTestCases` are abstract bases; run their subclasses, not them.

## Known in-progress work (as of the Vert.x 5 upgrade)

- HTTP/3 and the `*ServerConfig` APIs are adopted (`ListenerSettings.config`, `CoreListener.configType()`). Open: no `Alt-Svc`
  headers from chili-core, QUIC load balancing (`SO_REUSEPORT`) is Linux/macOS only.
