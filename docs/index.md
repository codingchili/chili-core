Welcome to the chili-core wiki!

The documentation is a work in progress.

Recommended reading order
1. [Context](Context)
2. [Services](Services)
3. [Listeners](Listeners)
4. [Handlers](Handlers)
5. [Protocol](Protocol)
6. [Configuration](Configuration)
7. [Launcher](Launcher)
8. [Logging](Logging)
9. [Security](Security)
10. [Storage](Storage)
11. [Testing](Testing)
12. [Files](Files)
13. [Benchmarking](Benchmarking)

|Content|Chapter|Description|Package|
|---|---|---|---|
||Context|Documentation on the `CoreContext` and the `SystemContext`.|[com.codingchili.core.context](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/context)
||Services|A deployable unit, deploys listeners and assigns handlers to them.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
||Listeners|Deployed from a service, listens for requests and delegates to a handler.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
||Handlers|Contains business logic and authentication.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
||Protocol|Provides the mapping from routes in a handler to methods invoked by the listener.|[com.codingchili.core.protocol](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/protocol)
|yes|Configuration|The configuration subsystem and framework configuration.|[com.codingchili.core.configuration](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/configuration)
||Launcher|Provides additional features for deploying applications, optional.|[com.codingchili.core.context](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/context)
||Logging|Contains features for logging locally and remotely over the cluster.|[com.codingchili.core.logging](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/logging)
||Security|Security functionality such as keystores, hashing and token verification/signing.|[com.codingchili.core.security](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/security)
||Storage|The storage and query API's.|[com.codingchili.core.storage](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/storage)
||Benchmarking|The benchmarking API's.|[com.codingchili.core.benchmarking](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/benchmarking)
||Files|The file API's.|[com.codingchili.core.files](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/files)
||Testing|Helpers and mocks that can be used to write test when using the chili-core.|[com.codingchili.core.testing](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/testing)