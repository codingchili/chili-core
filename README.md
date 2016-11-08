# chili-core [![Build Status](https://travis-ci.org/codingchili/chili-core.svg?branch=master)](https://travis-ci.org/codingchili/chili-core)

The chili core is a lightweight **architecture** for creating online games with focus on **modularity**, **security** and **scalability**. The backend uses **Vert.x** with **Hazelcast**. The frontend prototype is based on **Polymer**, **websockets** and **HTML5**. 

For documentation and tutorials visit [the website](https://codingchili.com/), for a live demo visit [demo](https://beta.codingchili.com).

**Beware!**
Until the first RC master will be unstable.

## Installation
To install chili-core clone this repository with **git**,
```
git clone https://github.com/codingchili/chili-core.git
```

The webserver requires dependencies in **website/** run the following in that directory,
```
bower install
```

Create a JAR in the project root (and run tests)
```
mvn package
```

If you wish to create a fatjar with bundled resources move **conf/**, **resources/** or **website/** to **src/main/resources**.

If you do not have a local **ElasticSearch server** running on the default port log messages will be dropped unless consoleLogging is set in **conf/system/system.json**.

To start the packaged JAR run
```
java -jar <filename>.jar
```
This will start up services configured in the 'default' block of **conf/system/launcher.json**; routing, authentication, gameserver, realms, instances, logserver, webserver and patcher. 

To start only a single component use any of the following,
```
java -jar <filename>.jar <block or host>
```
To see all available commands run with --help.

The authentication tokens are stored in **/conf/services/{service}.json** and certificates for the router in **conf/cert/**.

.

## Background
The purpose of the project is to provide a stable core for game development. There are many aspects of creating games, backend architecture, user interfaces, game resources (graphics, sounds) and then the design which includes the story/quests etc. The core is designed to be easily integrated with and modified in each of these aspects. In order to provide this, the core includes somewhat complete subsystems for each of these points. Additionally the core will be delivered as a "complete" game, to further increase the availability/modability and broaden the audience. As such it is the aim of the project to be complete enough both in documentation and code so that it may be used as a learning platform. 

###### Summary
* Learning through modding
* Creating new content with existing material
* High modularity (MVC & Distributed architecture)
* Availability, Java backend and Browser clients.
* Primarily for top-down 2D MMORPG games, may be modded for other games.
* Low complexity promotes maintenance and growth

###### Audience
* Learning programming, game graphics or design, web development
* Bootstrap package for game development, creating a prototype for a 2D game.

## Features
The complete feature list may change during development. 

###### Realm
* Multiplayer enabled
* 2D-movement & spells
* AI enabled for npcs.
* Inventory, trading & looting system
* Crafting system 
* Achievements
* In-game chat
* Player classes and spells 
 * Programming knowledge not required to create/edit 
 * Configuration-based using JSON
 * Java programming required to add mechanics

###### Core
* Distributed realms/servers
* Centralized authentication server (per region)
* Statistics API and visualizations
* Transport & protocol independent logic
* Authentication server supports third-party realms
  * Keys available and bound only to registered users.
  * Allows players to try out player-modified worlds.
  * Realms may only edit characters bound to them.
* Support for instanced game worlds
 * Support for on-demand deployment
 * Support for matchmaking. 
* Security, non-cheatable server
 * Single-threaded in-memory transactions
 * Server authorized model
* Logging system for data analysis
 * JSON output, ElasticSearch & Kibana ready.
* Basic website with JSON templates for news/changelist
* Highly performant backend
 * Variable tickrate, uses time & vectors for collisions.
 * Concurrency using the actor model
 * Zero thread programming required (!!!) 

###### Services
* Authentication server: Account/character creation & available realms
* Routing server: Routes client requests in/out of the cluster.
* Realm Server: Handles incoming connections, instance travel
 * Instance Server: Handles game logic
* Webserver: Provides an interface for account/character/realmlist
* Resource server: Provides game resources, graphics & logic (scripts)
* Logging server: Receives logging data from the other components

When completed the items will be marked in some way, as no items are done yet the marker is undecided.

The authentication server exposes an API through REST to clients. Communication within the system and with the game servers is handled with websockets. Using websockets within the system reduces overhead and latency compared to REST, complexity is reduced and availability increased compared to UDP or TCP. This could be changed to UDP or TCP if more performance is desired and if the frontend is replaced with a desktop client. The project aims to be transport and protocol independent, replacing these parts of the core is simple.

The resources may be served from the webserver if desired.

All communication within the system uses a text-protocol based on JSON for simplicity.

## Configuration
The configuration directory **'conf' & 'resources' must be in the same directory as the jar file** or **in the classpath**.

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
│   │   ├── system.json
│   ├── services/
│   │   ├── authserver.json
│   │   ├── logserver.json
│   │   ├── patchserver.json
│   │   ├── realmserver.json
│   │   ├── webserver.json
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
│   │   │   ├── {type}.json
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
│   │   ├── world/
│   │   │   ├── {name}.json
```
**resources/patch.json** contains the patch data, subfolders **game** contains the game files and **gui** contains graphics used in character list/create.

**website/** contains the website used for the web-game, could be reused for desktop games for registration/forums etc.

**conf/system/** contains configuration for the core.

**conf/services/** contains configuration for services.

**conf/realm/** each represents a realm/server to be registered to the master authentication server. Each of these must have a valid authentication signed by the authentication servers secret key. The name of the realm file must also correspond to the "name" attribute in the configuration file.

**conf/game** directly relates to game logic, such as player classes, items, npcs, quests and the game world (maps). 

**conf/game/class** must have the same name as the "name" attribute within them. 

**conf/game/items** may have any name, preferrably names describing the type of items within the configuration file, splitting the files in this folder only provides structure.

All configuration files are loaded by their respective component on startup. Minimally the files in **/conf/system** must exist for the component that is to be run. For the gameserver the **/conf/game configuration** files must also exist.

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
