# Project setup

A short introduction on how to setup a project using the chili-core.

### Dependencies

The distributions are provided through jitpack, this means an additional
repository needs to be specified in the build setup.


As a dependency with Gradle
```groovy
allprojects {
    repositories {
        // mavenLocal, mavenCentral etc.
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
dependencies {
    implementation 'com.github.codingchili.chili-core:chili-core:${project.version}'
}
```

As a dependency with Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.codingchili.chili-core</groupId>
    <artifactId>chili-core</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Optional dependencies

Some dependencies are not required unless specific features are accessed. Excluding
optional dependencies will greatly reduce the amount of transitive dependencies used. When
all optionals are included the distribution lands at 50+ MB, mostly thanks to ElasticSearch,
Hazelcast and SQLite. 

The following dependencies are optional in some cases,

|Dependency|Size|Required when
|---|---|---|
|ElasticSearch|15+ MB|ElasticMap is used.
|CQEngine|7.5 MB|IndexedMap* is used.
|Hazelcast|10 MB|Clustering/HazelMap is used.
|MongoDB|1 MB|MongoDBMap is used.

These are rough estimates.

##### Excluding optional dependencies 
An example on how to exclude all optionals

```groovy
dependencies {
    compile("com.github.codingchili.chili-core:core:$project.version") {
        exclude group: 'org.elasticsearch.client', module: 'elasticsearch-rest-high-level-client'
        exclude group: 'com.googlecode.cqengine', module: 'cqengine'
        exclude group: 'io.vertx', module: 'vertx-hazelcast'
        exclude group: 'io.vertx', module: 'vertx-mongo-client'
    }
}
```

When excluding all of the optional dependencies the jar size is 12MB.