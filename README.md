# chili-core [![Build Status](https://travis-ci.org/codingchili/chili-core.svg?branch=master)](https://travis-ci.org/codingchili/chili-core) [![](https://jitpack.io/v/codingchili/chili-core.svg)](https://jitpack.io/#codingchili/chili-core)

The chili core is an opinionated framework for creating microservices with focus on speed of development and time to market. It's based on the Vert.x toolkit for maximum power and uses Hazelcast for plug-and-play clustering. 

Find the official documentation [here](https://codingchili.github.io/chili-core/).

## Building
To build chili-core clone this repository with **git**,
```
git clone https://github.com/codingchili/chili-core.git
```

Builds project jars and run tests
```
gradlew build
```

Note: when targeting java 9+ the following hacks are needed for Netty/Vert.x
```
--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.net.dns=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
```


## Background 
The purpose of wrapping vertx in a framework is to increase productivity. This is done by providing common 
functionality that can be used to build microservices on as a framework. With all the logic packed into core, it is 
possible to create distributed microservices capable of handling authentication, request routing and storage 
in 66 lines of code. If you are interested in vertx, I recommend using it directly instead. 
This framework is intended to improve productivity for a single developer, aka the author. 
In order to achieve this it is much more invasive than the vertx toolkit.

###### Summary
* Built on the high-performance reactive toolkit vertx
* Uses clustering to improve scalability
* Support for a variety of storage options, query API's and a DSL.
* A distributed logging system and logging aggregator
* Highly adaptable and flexible with a configuration system
* High availability with support for multiple transports
* Text based protocols with JSON
* Adopts the microservice pattern

## Features
The complete feature list may change during development. 

##### Core
* Transport & protocol independent implementation
* Logging system for data analysis and real time monitoring
* Authentication based on hmac tokens.
* A router for routing external connections into the clustered event bus.
* Statistics API that builds on top of the logging system
* Interchangeable storage options with indexing and query support

##### Audience
- Programmers seeking to create microservices productively in a very specific use case.
- Game, web-app or mobile-app developers with an interest in backend development.

## Makes use of
The core uses some great software, such as

* [eclipse/vert.x](https://github.com/eclipse/vert.x) - reactive: eventbus, clustering and networking
* [hazelcast/hazelcast](https://github.com/hazelcast/hazelcast) - cluster management, data store
* [npgall/cqengine](https://github.com/npgall/cqengine) - in-vm indexed collections with query support
* [elastic/elasticsearch](https://github.com/elastic/elasticsearch) - distributed data indexing
* [mongodb/mongo](https://github.com/mongodb/mongo) - document database
* [EsotericSoftware/kryo](https://github.com/EsotericSoftware/kryo) - serialization library
* [EsotericSoftware/reflectasm](https://github.com/EsotericSoftware/reflectasm) - fast reflections library

Applications currently using chili-core

* [flashcards](https://flashcardsalligator.com/) - Progressive web app for studying  with flashcards.
* [ethereum-ingest](https://github.com/codingchili/ethereum-ingest) - Ethereum block/transaction import utility.
* [zapperfly-asm](https://github.com/codingchili/zapperfly-asm) - Extra simple clustered build servers.
* [chili-game-ext](https://github.com/codingchili/chili-game-ext) - 2D MMORPG game in development.

## Contributing
Issues and PR's are welcome with :blue_heart:.

## License
The MIT License (MIT)
Copyright (c) 2019 Robin Duda

See: [License](./LICENSE.md)
