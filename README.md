# chili-core [![Build Status](https://travis-ci.org/codingchili/chili-core.svg?branch=master)](https://travis-ci.org/codingchili/chili-core)

The chili core is a lightweight distributed **architecture** for creating microservices with focus on **modularity**, **security** and **scalability**. The backend uses **Vert.x** with **Hazelcast** as its cluster manager. The game prototype built using the core is based on **Polymer**, **websockets** and **HTML5** with WebGL. 

For documentation and tutorials visit [the website](https://codingchili.com/), for a live demo visit [demo](https://beta.codingchili.com).

**Beware!**

Until the first pre-release master will be unstable!

This project is currently in development!

Things may not work even if the build is green!

## Installation
To install chili-core clone this repository with **git**,
```
git clone https://github.com/codingchili/chili-core.git
```

The website service requires dependencies in **website/** run the following in that directory,
```
bower install
```

Create a JAR in the project root (and run tests)
```
mvn package
```

If you wish to create a fatjar with bundled resources move **conf/**, **resources/** or **website/** to **src/main/resources**.

If you do not have a local **ElasticSearch server** running on the default port log messages will be dropped unless consoleLogging is set in **conf/system/system.json**.

Before starting it up new tokens/secrets should be generated,
```
java -jar <filename>.jar --generate
```
The authentication tokens are stored in **/conf/services/{service}.json**. See **conf/system/security.json** for setting up token/secret dependencies between services. 

Start the launcher with, 
```
java -jar <filename>.jar <block or host>
```
This will start up services configured in the given or 'default' block in **conf/system/launcher.json**.

To see all available commands run with --help.

## Background
The project consists of two parts. The core, which is a framework built on top of the vertx toolkit. The purpose of wrapping vertx in a framework is to increase productivity. This is done by providing common functionality that can be used to build microservices on. With all the logic packed into core, it is possible to create distributed microservices capable of handling authentication, request routing and storage in 66 lines of code. If you are interested in vertx, I recommend using it directly instead. This framework is intended to improve productivity in a very specific use case. In order to achieve this it is much more invasive than the vertx toolkit.
 
The purpose of the service part of the project is to provide implementations for use in (mainly) game servers. Each service may be distributed on different hosts. Communication channels is provided by the core, with support for various transports and storage plugins. Breaking down the system into microservices improves maintainability, testability and ultimately, productivity.

Additionally, on top of these services an actual game and server will be implemented.

###### Summary
* Built on the high-performance reactive toolkit vertx
* Clustering improves scalability
* Text based protocols with JSON
* High availability with support for multiple transports
* Adopts the microservice pattern

###### Audience
* Game developers seeking to implement multiplayer from the start with minimal overhead.
* Programmers seeking to create microservices productively in a very specific use case.
* Aspiring game developers with an interest in backend development.
* Players who are into simplistic 2D MMORPG's.

## Features
The complete feature list may change during development. 

###### Realm
* Multiplayer enabled
* 2D-movement & spells
* AI enabled for npcs.
* Inventory, trading & looting system
* Crafting system 
* Player classes and spells 
 * Programming knowledge not required to create/edit 
 * Configuration-based using JSON
 * Java programming required to add mechanics

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

###### Services
* Authentication: Account creation and available realms.
* Routing: Routes client requests in/out of the cluster.
* Realms: Handles incoming connections, instance travel.
 * Instances: Handles game logic.
* Website: Provides an interface for account/character/realmlist.
* Patching: Game updates through sendFile, webseed and BitTorrent.
* Logging: Receives logging data from the other components.
* Social: Achievements, chat, guilds.
* Auction house: Handles asynchronous trading with orders/offers.
* Serverstatus: Provides a quick overview of system uptime.

Communication between services is done over the cluster, other available transports such as websock, tcp, udp and rest is available but not recommended unless a service is not able to join the cluster.

All communication between services uses a text-protocol based on JSON for simplicity.

## Configuration
The configuration directory **'conf' must be in the same directory as the jar file** or **on the classpath**.

The configuration structure
```
├── resources/
|   ├── version.json
|   ├── game/
|   ├── gui/
├── website/
|   ├── bower.json
├── conf/
│   ├──system/
│   │   ├── launcher.json
│   │   ├── validator.json
│   │   ├── security.json
│   │   ├── storage.json
│   │   ├── system.json
│   ├── services/
│   │   ├── authserver.json
│   │   ├── logserver.json
│   │   ├── patchserver.json
│   │   ├── realmserver.json
│   │   ├── webserver.json
│   │   ├── routingserver.json
│   ├── realm/
│   │   ├── {name}.json
│   │   ├── {name}.json
│   │   ├── disabled/
│   ├── game/
│   │   ├── class/
│   │   │   ├── {name}.json
│   │   │   ├── disabled/
│   │   ├── crafting/
│   │   │   ├── {name}.json
│   │   ├── item/
│   │   │   ├── {name}.json
│   │   ├── npc/
│   │   │   ├── character.json
│   │   │   ├── dialog.json
│   │   │   ├── quest.json
│   │   │   ├── trading.json
│   │   ├── parameters/
│   │   │   ├── targeting.json
│   │   │   ├── weapons.json
│   │   ├── player/
│   │   │   ├── affliction.json
│   │   │   ├── characters.json
│   │   │   ├── spells.json
│   │   ├── instances/
│   │   │   ├── {name}.json
```
**Explanation**
- 'resources/' is used by the patching service to store files.
- 'website/' contains website files used in the prototype.
- 'conf/' contains all configuration files.
- 'conf/system/' contains framework configuration.
- 'conf/services/' contains service configuration if any.
- 'conf/realm/' contains realm configurations for the realm service.
- 'conf/game/' contains game configuration, may be overriden in 'conf/realm/override'

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
