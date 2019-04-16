Welcome to the chili-core documentation!

##### Important resources
- Complete javadoc can be found [here](javadoc/).
- A sample project for getting started can be found [here](https://github.com/codingchili/chili-core-examples).

##### Documentation
The documentation is a work in progress, most chapters are incomplete or not started.

Recommended reading order
1. [Context](context)
2. [Services](services)
3. [Listeners](listeners)
4. [Handlers](handlers)
5. [Protocol](protocol)
6. [Configuration](configuration)
7. [Launcher](launcher)
8. [Logging](logging)
9. [Security](security)
10. [Storage](storage)
11. [Testing](testing)
12. [Files](files)
13. [Benchmarking](benchmarking)
 
##### Documentation overview

|Content|Chapter|Description|Package|
|---|---|---|---|
|yes|Context|Documentation on the `CoreContext` and the `SystemContext`.|[com.codingchili.core.context](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/context)
|yes|Services|A deployable unit, deploys listeners and assigns handlers to them.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
|yes|Listeners|Deployed from a service, listens for requests and delegates to a handler.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
|yes|Handlers|Contains business logic and authentication.|[com.codingchili.core.listener](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/listener)
|yes|Protocol|Provides the mapping from routes in a handler to methods invoked by the listener.|[com.codingchili.core.protocol](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/protocol)
|yes|Configuration|The configuration subsystem and framework configuration.|[com.codingchili.core.configuration](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/configuration)
|yes|Launcher|Provides additional features for deploying applications, optional.|[com.codingchili.core.context](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/context)
|yes|Logging|Contains features for logging locally and remotely over the cluster.|[com.codingchili.core.logging](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/logging)
|yes|Security|Security functionality such as keystores, hashing and token verification/signing.|[com.codingchili.core.security](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/security)
|yes|Storage|The storage and query API's.|[com.codingchili.core.storage](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/storage)
||Benchmarking|The benchmarking API's.|[com.codingchili.core.benchmarking](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/benchmarking)
||Files|The file API's.|[com.codingchili.core.files](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/files)
||Testing|Helpers and mocks that can be used to write test when using the chili-core.|[com.codingchili.core.testing](https://github.com/codingchili/chili-core/tree/master/core/main/java/com/codingchili/core/testing)