# Performance sweep

Quick performance wins across the protocol, servers, JSON, reflection, configuration and the build.
Compiled with `gradlew build -x test` (configuration cache on). No tests have run against these changes yet.

## Protocol and routing

- `Protocol.setHandlerRoutes`: the `addOpens` call ran once for every method on the handler class.
  It now runs once per handler.
- `Protocol.wrap`: whether a method takes zero or one argument is decided once at registration, not on every call.
  An `IllegalAccessException` used to be rethrown as its (always null) cause; it is now rethrown itself.
- `SimpleAuthorizationHandler`: fewer map lookups per request (one lookup instead of `containsKey` + `get`, and the
  role scan walks `entrySet()`).

## JSON and serialization

- `Serializer.yaml(JsonObject)` created a new `ObjectMapper` on every call and round-tripped through a JSON string.
  It now writes directly with the YAML mapper, which already serializes `JsonObject`.
- `Serializer.kryo`: the pooled Kryo instance is returned to the pool in a `finally`; exceptions used to lose it.
- `Serializer.describe`: its cache was a plain `HashMap` written on every call (not thread-safe).
  It is now a `ConcurrentHashMap` filled once per class.
- `gzip`/`ungzip` use try-with-resources and `readAllBytes()`.

## Servers and requests

- `RestRequest`: the request path was split twice per request and `basePath` was treated as a regex via `replaceFirst`.
  The path is now split once and the base path is matched literally, which also fixes base paths containing regex
  characters such as `.`.
- `ClusterListener` (bug fix): it split `handler.address()` on commas but registered a consumer for the whole unsplit
  string once per part. It now registers one consumer per address. This changes behaviour for handlers that use
  comma-separated addresses.

## Configuration, regex and logging

- `Configurations.get` (used by every `Configurations.system()` call, including each `ClusterRequest`) no longer
  allocates a lambda and does one map lookup instead of three.
- `Validator` and `RegexComponent`: regexes were compiled on every validation call; they are now compiled once and cached.
- `StreamQuery.matches` compiled the regex once per entry; it now compiles once per query. The clause check stops at
  the first statement that doesn't match.
- `ConsoleLogger`: every log line went through three regex compiles to strip hidden tags; now a single precompiled pattern.

## Gradle (`gradle.properties`)

- Enabled `org.gradle.configuration-cache=true`.
- Daemon raised to `-Xmx2g` with `-XX:+UseParallelGC`.

## Not changed, worth considering

- `prettyEncoding` defaults to `true`, so every JSON response is indented. Turning it off is the biggest remaining
  serialization win, but it changes the output format.
- `Request.token()` deserializes the token on every call, and the authenticator may call it more than once per request.
- Each `Protocol` creates its own `ConsoleLogger`, and each `ConsoleLogger` calls `AnsiConsole.systemInstall()` in its
  constructor.
- `Method.invoke` could be replaced with `LambdaMetafactory`-generated lambdas for faster routing (larger change).

## Tests to run once the timeout issue is fixed

`ProtocolTest` subclasses, the REST/cluster listener tests, `QueryTest`, the validator tests and the `ConsoleLogger` tests.
