# Where to take chili-core next

Opinion piece written after the Vert.x 5 / Jackson 3 upgrade, the performance sweep and the security sweep
(see [upgrades.md](upgrades.md), [performance.md](performance.md), [security.md](security.md),
[simplify.md](simplify.md)). It's based on reading the code (~20k lines in `core/main`, 84 test classes), not on running
it in production.

## Short answer

**Good enough to build microservices on? Yes, for your own projects, games, internal tools and prototypes.** The
programming model is small and consistent: one handler serves REST, WebSocket, TCP, UDP and the event bus, and
annotations do the routing and roles. Storage and configuration are pluggable and hot-reloaded, and it sits on Vert.x,
which is very much production grade.

**Production grade for a team or a business? Not yet.** The gaps are about operating it, not about the core idea:

1. **The test suite hasn't been green since the upgrade.** Several known failures and hangs are listed in
   `upgrades.md`/`simplify.md`, and there's no CI.
2. **The release pipeline is broken.** `jitpack.yml` builds with JDK 21 while the toolchain asks for Java 27, so
   JitPack can't build 1.4.3+.
3. **Open security findings:** token canonicalization (High) and keystore alias DoS (High) in `security.md`.
4. **HTTP semantics.** `RestRequest` always answers `200 OK` and puts the real status in the JSON body. Load balancers,
   API gateways, retries, caches and HTTP client libraries can't see errors.
5. **Observability is homegrown.** Custom logger, Dropwizard metrics, no tracing, no health/readiness endpoints, no
   Prometheus/OpenTelemetry.
6. **Container friendliness.** Configuration is file-only (no env var overrides), and the default
   `DEFAULT_MAX_REQUEST_BYTES` was 1 KiB, which surprised people (now 64 KiB).
7. **Maintenance history.** No commits between 2023-05 and 2026-10. Users need to trust that upgrades keep coming.

Fix 1–4 and it's a credible small framework. 5–6 are what separate it from Quarkus/Micronaut/Helidon/plain
Vert.x + vertx-web for anyone picking a stack today.

## Priority 0: make it trustworthy

- **CI.** The suite is green locally (724 tests, 0 failures), but the `*IT` classes that need MongoDB/Elasticsearch
  are skipped and nothing runs it automatically. Add a GitHub Actions workflow that runs unit tests on every push and
  runs `*IT` with MongoDB/Elasticsearch as service containers (or Testcontainers). Make the persisted-map tests
  safe to run in parallel first (see `upgrades.md`).
- **Fix `jitpack.yml`** (JDK 27, or lower the toolchain). Consider also publishing to Maven Central. JitPack is fine for
  hobby use but enterprises often block it.
- **Fix the High security findings** before tagging a release. The token format change breaks issued tokens, so do it
  in a major version.
- **Version it properly.** All the future-based API changes are breaking, so release them as **2.0.0** and turn the
  "Adapting consumers" section of `upgrades.md` into a migration guide in `docs/`.
- **Finish the handler → future migration** (`HashFactory.verify`, `BenchmarkImplementation`, a sweep for the rest),
  so the API has one async style.

## Trim: things that cost more than they give

These add dependencies, attack surface or maintenance without being core to "build a microservice":

| Candidate | Why | Suggestion |
|---|---|---|
| `ElasticMap` (`elasticsearch-rest-high-level-client` 7.17) | The high-level client is deprecated and EOL, drags in a huge dependency tree and isn't compatible with ES 8/9. | Move it to an optional module, rewrite it on `co.elastic.clients:elasticsearch-java`, or drop it. |
| `MongoDBMap`, `HazelMap`, `IndexedMap*` (CQEngine + Kryo) | Every consumer pulls in Mongo, Hazelcast, CQEngine and Kryo even if they only use `PrivateMap`. CQEngine is barely maintained. | **Split into modules**: `core`, `storage-mongo`, `storage-elastic`, `storage-cqengine`, `cluster-hazelcast`. Discover plugins with `ServiceLoader`. |
| Benchmarking framework + jade4j HTML reports | jade4j is unmaintained. Benchmarks are a dev tool, not runtime functionality. JMH exists. | Move to a `benchmarking` module, or replace the HTML report with JSON/Markdown output. |
| `jline` + the interactive launcher commands | FFM/native access and `Enable-Final-Field-Mutation` flags just for a console. Containers don't need a REPL. | Make it optional; keep plain `Launcher` argument parsing. |
| `ThreadGuard`/`GuardedThread`/`GuardMode` | Only used by their own tests. Vert.x already enforces context affinity. | Delete, or move to `testing`. |
| `Delay`, `JsonDatabase`, `OpenAPIGenerator` (not wired anywhere), log4j/log4j2 properties next to JUL config | Dead or half-finished code. | Delete or finish (see OpenAPI below). Ship one logging config. |
| `UdpListener` | A niche transport with no delivery semantics. | Keep only if a real consumer uses it. |
| Kryo `Serializer.pack` paths, `--add-opens`/`--add-exports` for Hazelcast | Opening JDK internals is a production smell and gets harder with every JDK. | Becomes optional automatically once Hazelcast/CQEngine are separate modules. |
| `prettyEncoding: true` by default | Wastes bandwidth and CPU on every response. | Default to `false` in 2.0. |

The goal: the `core` artifact is **Vert.x + Jackson + argon2** and nothing else. Today's dependency footprint is the
biggest barrier to adoption.

## Fix / extend: what's there but rough

- **HTTP done properly.**
  - Map `ResponseStatus` to real HTTP status codes (`NOT_FOUND`→404, `UNAUTHORIZED`→401, `CONFLICT`→409,
    `ERROR`→500). Keep the body envelope if needed, but stop answering 200 to everything.
  - Allow method-aware routes (`@Api(method = GET, path = "/accounts/:id")`). Today everything is target/route
    in the path or body, which doesn't fit REST conventions or OpenAPI.
  - Configurable CORS (see `security.md`), compression, request timeouts that actually cancel, and a raised
    default max body size (64 KiB?).
- **Typed handlers.** Spiked, and it has legs: see `upgrades.md` ("Typed routes: a spike, and whether it has legs") for the verdict,
  numbers and the open questions. `@Api` methods take a `Request` and call `request.data()`/`write()`. Allow
  `public Future<AccountView> get(GetAccount input)`. The protocol deserializes the input (and validates it, there's
  already `Validator`) and writes the returned future. This removes most of the boilerplate in handlers and makes
  OpenAPI generation possible. (`LambdaMetafactory`/`MethodHandle` instead of `Method.invoke` isn't worth it on its own: measured in `upgrades.md`,
  "Benchmark: how the protocol invokes routes".)
- **Finish `OpenAPIGenerator`** using `@Description`, `@DataModel` and typed handlers, and serve it from the REST
  listener (`/openapi.json`). Free documentation is a big selling point.
- **Configuration.**
  - Environment variables and system properties should override file values
    (`CHILI_SYSTEM_SERVICES=4`, `${ENV:default}` placeholders). This is a must for Docker/Kubernetes and secrets.
  - Validate configuration on load (fail fast, with a clear message).
  - Hot reload is nice in development but risky in production; let it be turned off.
- **Listener configuration** (already queued in `upgrades.md`): one config key per listener, adopt Vert.x 5
  `HttpServerConfig`/`TcpServerConfig`, add HTTP/3 and QUIC.
- **Token handling.** Cache the parsed `Token` per request, fix canonicalization, and consider supporting standard JWT
  (or at least verifying JWTs from an external IdP) alongside the custom token format. Interop with OAuth2/OIDC is
  what most real deployments need.
- **Storage.**
  - Escape helpers / documented safe usage for `like`/`matches` (ReDoS, injection).
  - Bring the `MapTestCases` contract to all plugins in CI.
  - Add pagination cursors (not only page/size) and a transaction-free "compare and set" (`update` with version).
  - A **SQL plugin** (Postgres via `vertx-pg-client`) is the most common missing backend.
- **Graceful shutdown.** Stop accepting traffic → drain in-flight requests → undeploy → close storage, with readiness
  flipping to "not ready" first. Partly done: see `upgrades.md` ("Graceful shutdown fixed"). Still open: a drain delay for
  load balancers, closing storage plugins, tracking blocking work outside `CoreContext.blocking`.

## Add: what production microservices expect

- **Health and readiness**: `/health/live` and `/health/ready` on the REST listener (or a separate admin port), with
  checks contributed by storage plugins and the cluster manager. Consider `vertx-health-check`. Started: `StatusService`
  (separate port, readiness checks, JSON report and web page), see `upgrades.md`. Still open: checks from the storage
  plugins and the cluster manager, and the REST listener answering with real HTTP status codes.
- **Metrics**: export to Prometheus (Micrometer via `vertx-micrometer-metrics` instead of Dropwizard). Add per-route
  latency/error counters in `Protocol.process` for free RED metrics.
- **Tracing**: OpenTelemetry (`vertx-opentelemetry`), propagating trace context over REST headers *and* the event bus
  `ClusterRequest`, plus trace IDs in log lines.
- **Logging**: either adopt SLF4J/JUL as the backend and keep `Logger` as a facade, or at least make JSON logging to
  stdout the default in containers. `RemoteLogger` over the event bus is clever but couples logging to cluster health.
- **Resilience**: timeouts and circuit breakers for outgoing calls (`vertx-circuit-breaker`), rate limiting per
  listener/route, and backpressure on WebSocket/TCP writes.
- **Service-to-service calls**: a small typed client for calling another chili service over the bus or HTTP
  (`core.client("account").call("get", req)` → `Future`). This is the piece that makes it a *microservice* framework
  rather than a server framework.
- **Packaging**: a sample `Dockerfile`/jlink image, an example Kubernetes manifest (probes wired to the health
  endpoints), and a project template / archetype (`gradle init`-style) with one service, one handler, one test.
- **Testing support**: Testcontainers helpers for the storage plugins, and an in-process `TestServer` that deploys a
  handler behind a listener on a random port and returns a client. `ContextMock`/`RequestMock` are a good start.
- **Virtual threads**: Vert.x 5 supports virtual-thread verticles (`ThreadingModel.VIRTUAL_THREAD`). Offer it as a
  `DeploymentAware` option so handlers can use `Future.await()` and write sequential code. This would be a big
  ergonomics win and fits Java 27 well.

## Things that are already good, keep them

- Transport-independent `Request` + `Protocol`: the defining feature. Protect it while adding HTTP-specific
  features.
- Future-based storage with a shared query DSL and an enforced failure contract (`MapTestCases`).
- Argon2 hashing, constant-time HMAC comparison, no Jackson default typing (see `security.md`, "Reviewed and fine").
- `Configurations` with YAML/JSON and defaults from classes: just add env overrides.
- Documentation in `docs/` with real examples, plus `AGENTS.md`.

## Suggested order

1. 2.0.0-RC: green tests + CI, JitPack fix, security fixes, finish the future migration, migration guide.
2. 2.0.0: real HTTP status codes, env var config, health/readiness, Prometheus metrics, `prettyEncoding` off.
3. 2.1: module split (storage plugins, Hazelcast, benchmarking, jline), drop/replace `ElasticMap`, remove dead code.
4. 2.2: typed handlers + OpenAPI, OpenTelemetry, service client, virtual threads.
5. Later: SQL storage plugin, HTTP/3/QUIC, resilience helpers, project template.
