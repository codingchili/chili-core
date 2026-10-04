# Deployment simplification

Simplifies deploying and undeploying in `SystemContext` using Vert.x 5 APIs, and migrates `SystemContext.clustered`
from a callback to a `Future`. Compiles cleanly. The deploy-related tests pass; failing tests are listed below.

## Deploying (`SystemContext`)

- `listener`, `service` and `handler` now deploy through
  `vertx.deployVerticle(Supplier<Deployable>, new DeploymentOptions().setInstances(n))`. Vert.x creates the N instances
  under a single deployment ID and undeploys them together.
- Removed the hand-rolled bookkeeping: the made-up UUID mapped to a list of real deployment IDs, the `CountDownLatch`,
  the deploy loop and the `Promise` wrappers in each deploy method.
- The instance count still comes from the first supplied instance, and that instance is deployed as the first one,
  so the supplier is called the same number of times as before.
- `getHandlerCount` became `options(Object)`, a pattern-matching `switch` (`DeploymentAware` → listener → service →
  default handlers) that returns the `DeploymentOptions`. Deploying a plain `Verticle` by class name uses it too.
- Bug fixes that came with it:
  - The ID list was a plain `ArrayList` updated from deploy callbacks without locking. Deployments are now tracked in a
    concurrent set.
  - If one of N instances failed, the instances that had already started stayed deployed and were never tracked.
    Vert.x now rolls back the whole deployment.
  - After a failed deploy, the remaining successful instances still counted down the latch.

## Undeploying

- `stop(id)` is a single `vertx.undeploy(id)`, with no separate path for our own IDs versus Vert.x IDs.
- `CoreContext.stop()` now returns `Future<Void>` instead of `Future<CompositeFuture>` (**API change**). The only caller,
  `ShutdownHook`, doesn't use the result.
- `stop()` now also undeploys plain `Verticle`s deployed through `deploy(String)`. Those weren't tracked before.
- Vert.x now sees one deployment with N instances, instead of N separate deployments.

## Clustering

- `SystemContext.clustered(Handler<AsyncResult<CoreContext>>)` became `Future<CoreContext> clustered()`
  (**API change**): `Vertx.clusteredVertx(options).map(SystemContext::new)`.
- Callers updated: `Launcher`, `CoreBenchmarkSuite`, `BenchmarkIT`, `HazelMapIT`, and the examples in
  `docs/context.md` and `docs/services.md`.
- `CoreBenchmarkSuite` (bug fix): it called `cluster.result()` without checking whether joining the cluster failed,
  so a failed join caused an NPE and the command never completed. The failure is now passed to the command's promise.

## Tests

Passing: `SystemContextTest`, `ShutdownHook*`, `LauncherIT`, `listener.transport.*`, `HazelMapIT`, and the
deploy-related tests in `listener.*`.

Failing, and not caused by these changes:

- `RestRequestTest`: the mock `RoutingContext.body()` returns null, causing a `NullPointerException` in `RestRequest.parseData`.
- `MultiHandlerTest.ensureHandlersCallable`: the deploy succeeds, but the response's `status` field is null.
- `BusRouterTest` (3 tests): time out. They don't deploy anything through `SystemContext`.
- `BenchmarkIT.testExecuteSuiteAsCommand`: the test's setup already joins a cluster, and `cluster.xml` uses a fixed
  Hazelcast instance name, so the suite's second `clustered()` fails with
  `HazelcastInstance with name 'core' already exists!`. Previously this hung until the test timed out (see the NPE
  above); now it fails straight away. Fix by giving each instance its own name in `cluster.xml`, or by letting
  `CoreBenchmarkSuite` reuse an existing context.

These failures weren't checked against a clean baseline.

## Follow-ups

- `docs/services.md` and `docs/context.md` still show the old `start(Future<Void>)` signature and
  `core.service(new CoreServiceImpl())` without a supplier.
