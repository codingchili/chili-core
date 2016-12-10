# prototype
The game prototype built using the core and services is based on **Polymer**, **websockets** and **HTML5** with WebGL. 

The website service requires dependencies in **website/** run the following in that directory,
```
bower install
```

If you do not have a local **ElasticSearch server** running on the default port log messages will be dropped unless consoleLogging is set in **conf/system/system.json**.

Before starting the server, new tokens/secrets should be generated using,
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
