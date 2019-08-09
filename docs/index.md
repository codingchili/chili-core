Welcome to the chili-core documentation!

##### Important resources
- Complete javadoc can be found [here](javadoc/).
- A sample project for getting started can be found [here](https://github.com/codingchili/chili-core-examples).
- The quickstart guide [here](https://github.com/codingchili/chili-core).

##### Documentation
The documentation is a good place to start to figure out how things work. During development it's recommended to use the sources jar
for easy access to javadoc in the IDE.

Recommended reading order
1. [Project setup](setup) 
2. [Context](context)
3. [Services](services)
4. [Listeners](listeners)
5. [Handlers](handlers)
6. [Protocol](protocol)
7. [Configuration](configuration)
8. [Launcher](launcher)
9. [Logging](logging)
10. [Security](security)
11. [Storage](storage)
12. [Testing](testing)
13. [Files](files)
14. [Benchmarking](benchmarking)

Other related documentation

- [Vertx](https://vertx.io/docs/vertx-core/java/)
- [Hazelcast](https://docs.hazelcast.org/docs/3.12.2/manual/html-single/index.html)
- [CQEngine](https://github.com/npgall/cqengine/tree/master/documentation)
- [Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)
- [MongoDB](https://docs.mongodb.com/)
 
 
##### Documentation overview

|Content|Chapter|Description|Package|
|---|---|---|---|
|yes|Setup|Using chili-core as a dependency with Maven and Gradle.
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