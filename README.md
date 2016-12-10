# chili-core [![Build Status](https://travis-ci.org/codingchili/chili-core.svg?branch=master)](https://travis-ci.org/codingchili/chili-core)

The chili core is a lightweight distributed **architecture** for creating microservices with focus on **modularity**, **security** and **scalability**. The backend uses **Vert.x** with **Hazelcast** as its cluster manager. 

For documentation and tutorials visit [the website](https://codingchili.com/), for a live demo visit [demo](https://beta.codingchili.com).

**Beware!**

Until the first pre-release master will be unstable and may not work even if the build is green!

## Building
To build chili-core clone this repository with **git**,
```
git clone https://github.com/codingchili/chili-core.git
```

Create a JAR in **prototype/** with all services bundled, requires gradle to be installed
```
gradle jar
```

For information on how to setup the installation look at the README.md in **prototype/**.

## Background
The project consists of two parts. The core, which is a framework built on top of the vertx toolkit. The purpose of wrapping vertx in a framework is to increase productivity. This is done by providing common functionality that can be used to build microservices on. With all the logic packed into core, it is possible to create distributed microservices capable of handling authentication, request routing and storage in 66 lines of code. If you are interested in vertx, I recommend using it directly instead. This framework is intended to improve productivity in a very specific use case. In order to achieve this it is much more invasive than the vertx toolkit.

Additionally, on top of these services an actual game and server will be implemented.

###### Summary
* Built on the high-performance reactive toolkit vertx
* Clustering improves scalability
* Text based protocols with JSON
* High availability with support for multiple transports
* Adopts the microservice pattern

## Features
The complete feature list may change during development. 

##### Audience
- Programmers seeking to create microservices productively in a very specific use case.
- Aspiring game, web-app or mobile-app developers with an interest in backend development.

###### Core
* Distributed realms/servers
* Statistics API and visualizations
* Transport & protocol independent logic
* Authentication server supports third-party realms
* Support for instanced game worlds
* Support for on-demand deployment
* Support for matchmaking. 
* Security, non-cheatable server
 * Single-threaded in-memory transactions
 * Server authorized model
* Logging system for data analysis
 * JSON output, ElasticSearch & Kibana ready.
* Basic website with JSON templates for news and account management.
* Highly performant backend
 * Variable tickrate, uses time & vectors for collisions.
 * Concurrency using the actor model
 * Zero thread programming required (!!!) 

## Configuration
The configuration directory **'conf' must be in the same directory as the jar file** or **on the classpath**.

The configuration structure
```
├── conf/
│   ├──system/
│   │   ├── launcher.json
│   │   ├── validator.json
│   │   ├── security.json
│   │   ├── storage.json
│   │   ├── system.json
```
**Explanation**
- 'conf/system/' contains framework configuration.

All configuration files are loaded by their respective service with support for reloading changes at runtime. Minimally the framework configuration in **conf/system/** must exist as it is required by the launcher. 

## Mods
Brief introduction on how the core may be modified to fit your needs.

* Server: Alter game mechanics, Protocol, Performance, Security. [Java programming]
* Realm design: Configuration files, Quests, Npcs, Worlds, Items [JSON]
* Resource mods: Modify graphics resources, spells, characters, sounds [Graphics/Sounds]

Realm and resource mods may be applied to systems which runs the authentication server with 3rd party server support. Or to a new system with the bundled game.

Example mod cases
* Mod an existing game you like which uses the chili-core and publish it as a 3rd party server on their server.
* Mod the chili core bundled game and start your own authentication server, or publish on the official server.
* Play the offical bundled game on the official server, request a PR or add an issue to this repository.

## Contributing
Not accepting issues or PR's at this stage.

## License
The MIT License (MIT)
Copyright (c) 2016 Robin Duda

See: LICENSE.md
