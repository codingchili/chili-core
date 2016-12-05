# chili-services

A collection of microservices that builds upon chili-core.

## Background
The purpose of the service part of the project is to provide implementations for use with game servers. Each service may be distributed on different hosts. Communication channels is provided by the core, with support for various transports and storage plugins. Breaking down the system into microservices improves maintainability, testability and ultimately, productivity.

## Configuration
Services following the official guidelines should place their configuration files in;
- 'conf/services/' 
where conf is a directory in the same folder as the server jar.
```
├── conf/
│   ├── services/
│   │   ├── authserver.json
│   │   ├── logserver.json
│   │   ├── patchserver.json
│   │   ├── realmserver.json
│   │   ├── realmregistry.json
│   │   ├── webserver.json
│   │   ├── routingserver.json
```
Some services comes with additional resources, these can be bundled within the jar if moved to **src/main/resources**. This also applies to configuration files, it is however recommended that these are easily edited. 

###### Services
* Authentication: Account creation and available realms.
* Routing: Routes client requests in/out of the cluster.
* Realms: Handles incoming connections, instance travel.
 * Instances: Handles game logic.
* Realm registry: keeps track of active realms. 
* Website: Provides an interface for account/character/realmlist.
* Patching: Game updates through sendFile, webseed and BitTorrent.
* Logging: Receives logging data from the other components.
* Social: Achievements, chat, guilds.
* Auction house: Handles asynchronous trading with orders/offers.
* Serverstatus: Provides a quick overview of system uptime.

Communication between services is done over the cluster, other available transports such as websock, tcp, udp and rest is available but not recommended unless a service is not able to join the cluster.

All communication between services uses a text-protocol based on JSON for simplicity.

###### Audience
* Game developers seeking to implement multiplayer from the start with minimal overhead.
* Programmers seeking to create microservices productively in a very specific use case.
* Aspiring game developers with an interest in backend development.
* Players who are into simplistic 2D MMORPG's.
