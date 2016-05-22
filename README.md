# chili-core [![Build Status](https://travis-ci.org/codingchili/chili-core.svg?branch=master)](https://travis-ci.org/codingchili/chili-core)

**ChiliCore** is a **lightweight architecture** for creating online games with focus on **modularity**, **security** and **scalability**. The backend uses **Vert.x** and the current frontend is based on **HTML5**, **websockets** and **Polymer**. 

For full documentation and tutorials visit [the website](https://codingchili.com/), for a live demo visit [demo](https://beta.codingchili.com/).



## Installation
To install chili-core clone this repository with **git**,
```
git clone https://github.com/codingchili/chili-core.git
```

The webserver requires dependencies in **src/main/resources/webroot** run the following in that directory,
```
bower install
```

Create a standalone JAR in the project root (also runs the tests)
```
mvn package
```
If the tests do not pass, either fix them yourself or checkout the (if exists) stable branch. The prepackaged jar file in the repository may also be used for testing out the project.

If you do not have a local **MongoDB server** running on the default port, localhost:27017 install latest version of MongoDB from [Website](https://www.mongodb.com/).

To start the packaged JAR run
```
java -jar <filename>.jar run Launcher
```
This will start up all the components; authentication, gameserver, realms, instances, logserver, webserver (+resources). The configuration in the **/conf** directory will be used to setup the connections and load the game data. 

To start only **a single component** use any of the following,
```
java -jar <filename>.jar run Authentication.Server
java -jar <filename>.jar run Game.Server
java -jar <filename>.jar run Logging.Server
java -jar <filename>.jar run Website.Server
```

To run the application in production mode the following commands must be executed,
```
java -cp <filename>.jar run Utilites.GenerateTokens regenerate
java -cp <filename>.jar run Utilites.GeneratePatch
```
The authentication tokens are stored in **/conf/system/{component}.json**. It is highly recommended to use a reverse proxy with TLS, see **/conf/system/proxy** for example configurations using NGINX and LetsEncrypt.

When components are started they read the configuration files in conf/system/{authserver,gameserver,logserer,webserver} if the port numbers or addresses are to be changed, check these example configuration files.

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
* Bootstrap package for game development, creating a prototype for a 2D web game.

## Features
The complete feature list may change during development. 

###### Game
* Multiplayer enabled
* 2D-movement & spells
* AI enabled for npcs.
* Inventory, trading & looting system
* Crafting system (to be continued)
* In-game chat
* Player classes and spells 
 * Programming knowledge not required to create/edit 
 * Configuration-based using JSON
 * Java programming required to add mechanics

###### Core
* Distributed realms/servers
* Centralized authentication server (per region)
* Authentication server supports third-party realms
  * Keys available and bound only to registered users.
  * Allows players to try out player-modified worlds.
  * Realms may only edit characters bound to them.
* Support for instanced game worlds
 * Support for on-demand deployment
* Security, non-cheatable server
 * Single-threaded in memory transactions
 * Server authorized model
* Logging system for data analysis
 * JSON output, ElasticSearch & Kibana ready.
* Basic website with JSON templates for news/changelist
* Highly performant backend
 * Variable tickrate, uses time & vectors for collisions.
 * Concurrency using the actor model
 * Zero thread programming required (!!!) 

###### Components
* Authentication server: Account/character creation & available realms
* Game server: Provides ping service for the clients browser
 * Realm Server: Handles incoming connections, instance travel
 * Instance Server: Handles game logic
* Webserver: Provides an interface for account/character/realmlist
* Resource server: Provides game resources, graphics & logic (scripts)
* Logging server: Receives logging data from the other components

The authentication server exposes an API through REST. Communication within the system and with the game servers is handled with websockets. Using websockets within the system reduces overhead and latency compared to REST, complexity is reduced and availability increased compared to UDP or TCP. This could be changed to UDP or TCP if more performance is desired and if the frontend is replaced with a desktop client.

The resources may be server from the webserver if desired.

All communication within the system uses a text-protocol based on JSON for simplicity.

## Configuration
The configuration directory **'conf' must be in the same directory as the jar file**.

The configuration structure
```
├── config/
│   ├── system/
│   │   ├── authserver.json
│   │   ├── gameserver.json
│   │   ├── logserver.json
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
Files in **conf/system/** sets up the bindings between the components with host/port numbers, logging and authentication tokens between the components. 

Configuration files in **conf/realm/** each represents a realm/server to be registered to the master authentication server. Each of these must have a valid authentication signed by the authentication servers secret key. The name of the realm file must also correspond to the "name" attribute in the configuration file.

Files in **conf/game** directly relates to game logic, such as player classes, items, npcs, quests and the game world (maps). 

Files in **conf/game/class** must have the same name as the "name" attribute within them. 

Files in **conf/game/items** may have any name, preferrably names describing the type of items within the configuration file, splitting the files in this folder only provides structure.

All configuration files are loaded by their respective component on startup. Minimally the files in **/conf/system** must exist for the component that is to be run. For the gameserver the **/conf/game configuration** files must also exist.

## Mods
Brief introduction on how the core may be modified to fit your needs.

* Server: Alter game mechanics, Protocol, Performance, Security. [Java programming]
* Game design: Configuration files, Quests, Npcs, Worlds, Items [JSON]
* Resource mods: Modify graphics resources, spells, characters, sounds [Graphics/Sounds]

Game and resource mods may be applied to systems which runs the authentication server with 3rd party server support. Or to a new system with the bundled game.

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
