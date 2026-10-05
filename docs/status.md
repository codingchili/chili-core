[back](index)

# Status and readiness

The `StatusService` reports the status of an application over HTTP: a readiness endpoint for load balancers and
orchestrators, a JSON report with details about the JVM, vertx and the metrics, and a web page that shows the report.

Covered in this chapter
- deploying the status service
- liveness and readiness endpoints
- readiness checks
- the status report and the web page
- graceful shutdown and readiness
- securing the service

### Deploying the service

```java
core.service(() -> new StatusService()
        .settings(new ListenerSettings().setPort(8081).setSecure(false))
        .check("accounts", () -> accounts.size().mapEmpty()));
```

The service needs a port of its own, `8081` without TLS by default. Set `setSecure(true)` and a keystore in the
listener settings to use TLS, see [security](security). The server accepts HTTP/1.1 and HTTP/2.

|Endpoint|Response|
|---|---|
|`/health/live`|`200` for as long as the application responds. For liveness probes.|
|`/health/ready`|`200` when the application is ready to serve requests, `503` when it is not. For readiness probes and load balancers.|
|`/status`|The status report as JSON.|
|`/`|A web page that shows the report and refreshes it, with a light and a dark theme.|

All endpoints support `GET` and `HEAD`, other methods are answered with `405`. The responses are never cached.

### Readiness

The response of `/health/ready` lists the checks and the result of each one,

```json
{
  "ready": false,
  "checks": {
    "accounts": {"status": "UP", "ms": 0.03},
    "payments": {"status": "DOWN", "ms": 1.7, "message": "gateway unreachable"},
    "shutdown": {"status": "UP", "ms": 0.01}
  }
}
```

The application is ready when all checks are `UP`. A check is added with `check(name, ReadinessCheck)`, a function that
returns a future: succeeded when the dependency is ready, failed (with a message that is included in the response) when it is not.
A check that throws, or that does not complete within the check timeout (2 seconds, see `checkTimeout`) is `DOWN`.
Checks run concurrently, whenever readiness is requested: they should be cheap, and must not block the event loop.
The messages of failed checks are shown to everyone that can reach the service, they must not contain secrets.

The built-in `shutdown` check fails when the context is shutting down.

### Graceful shutdown

When the JVM exits the context is marked as shutting down, `CoreContext.isShuttingDown()`, and the `shutdown` check fails: the
application is no longer ready, but still alive (`/health/live` stays `200`) and keeps serving requests. After the
**shutdown delay** the services are stopped, running blocking tasks are awaited and vertx is closed.
Configure the delay in `system.yaml`, so that a load balancer has time to notice that the application is not ready before
it stops serving requests,

```yaml
shutdownDelay: 5000         # services keep running for 5 seconds after readiness fails, default 0.
shutdownHookTimeout: 30000  # the whole shutdown, including the delay. Running blocking tasks are interrupted when it's up.
```

### The status report

`/status` returns a JSON object with these sections: `state` (`RUNNING` or `SHUTTING_DOWN`), `ready`, `application`
(version, host, process id, uptime), `readiness` (as above), `jvm` (heap and non-heap memory, threads, garbage
collection, CPU, operating system), `vertx` (clustered, thread pools, deployments, running blocking tasks), `settings` (counts and
timeouts, no secrets) and `metrics`. The metrics are the snapshot of the metric collector, see
[configuration](configuration): they are empty unless `metrics` are enabled in `system.yaml`.

The web page loads the report from the same server, and shows it. It consists of a single document, nothing is loaded from other
sites. The refresh interval can be changed on the page, and the metrics can be filtered.

### Securing the service

The report and the page reveal information about the application and the machine that it runs on, treat the service as an
administration interface.

- Deploy it on an internal port, and do not expose that port to the internet. `bind("127.0.0.1")` accepts connections
  from the machine itself only, which is enough for a sidecar or a probe that runs on the host.
- `details(false)` serves the health endpoints only, `/status` and the page are then `404`. Use it where only probes
  need to reach the service.
- No CORS headers are sent: pages from other sites cannot read the responses in a browser. The page is served with a
  content security policy that only allows its own script, and the responses set `X-Content-Type-Options`, `X-Frame-Options`
  and `Referrer-Policy`.
- There is no authentication. Use TLS and network policies, or run the service behind a proxy that authenticates.
