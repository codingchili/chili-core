# prototype
The game prototype built using the core and services is based on **Polymer**, **websockets** and **HTML5** with WebGL. 

## Preparing the installation
The website service requires dependencies in **website/** run the following in that directory,
```
bower install
```
To generate the default configuration run,
```
java -jar <filename>.jar --reconfigure
```

Add the services to deploy to **conf/system/launcher.json** using their fully qualified class name. Make sure to configure the services in **conf/service/{name}.json**, see the configuration section in the service package for details.

Before starting the server, new tokens/secrets should be generated using,
```
java -jar <filename>.jar --generate
```
The authentication tokens are stored in **/conf/services/{service}.json**. See **conf/system/security.json** for setting up token/secret dependencies between services. 

## Starting up the server
Start the launcher with, 
```
java -jar <filename>.jar <block or host>
```
This will start up services configured in the given or 'default' block in **conf/system/launcher.json**.

To see all available commands run with --help.

## Configuration
The configuration directory **'conf' must be in the same directory as the jar file** or **on the classpath**.

The configuration structure
```
├── resources/
│   ├── version.json
│   ├── game/
│   ├── gui/
├── website/
│   ├── bower.json
├── conf/
│   ├── system/*
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
│   │   │   ├── afflictions.json
│   │   │   ├── characters.json
│   │   │   ├── spells.json
│   │   ├── instances/
│   │   │   ├── {name}.json
```
**Explanation**
- 'resources/' is used by the patching service to store files.
- 'website/' contains website files used in the prototype.
- 'conf/realm/' contains realm configurations for the realm service.
- 'conf/game/' contains game configuration, may be overriden in 'conf/realm/override'


## Planned features
* Multiplayer enabled
* 2D-movement & spells
* AI enabled for npcs.
* Inventory, trading & looting system
* Crafting system 
* Player classes and spells 
 * Programming knowledge not required to create/edit 
 * Configuration-based using JSON
 * Java programming required to add mechanics
