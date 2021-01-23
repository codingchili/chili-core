# Configuration

Contains the server configuration.

## Configuration APIs
This section contains information on using the configuration API's.

```java
import com.codingchili.core.configuration.Configurable;

public class ConfigurationClass implements Configurable {
    // a configuration property with the default value of false.
    // the properties needs to either have get/set methods or be public.
    public boolean fastMode = false;

    @Override
    public String getPath() {
        // this is the path the configuration file is read from or written to.
        // the file extension determines the format of the file.
        return "configs/the_config.json";
    }
}
```

Here is another [example configuration model](https://github.com/codingchili/chili-core/blob/master/core/main/java/com/codingchili/core/configuration/system/LauncherSettings.java).

### Loading configuration
Loading configuration from a file with the model/schema file `ConfigurationClass.class`.

```java
ConfigurationClass config = Configurations.get("path/to/configurable.yml", ConfigurationClass.class);
```

When a configuration has been loaded into memory it will be reloaded whenever the file changes. The contents of the loaded file are cached in memory until the configuration is unloaded. An unload can be forced with `Configurations.reset()`, this should only be used from test cases.

If the file does not exist it will be instantiated with the defaults in ConfigurationClass.class. Additionally, if the system launcher configuration is configured with 'warnOnDefaultsLoaded' a warning will be logged. The default is to not log the warning. By defaulting to the configuration within the model class when the file is missing we can support configuration without having to ship default configuration as separate files.

Configuration can also be loaded directly from memory into the `Configurations` manager. This is done with the following call.

```java
Configurations.put(ConfigurationClass.class);
```

### Reloading configuration
Whenever configuration changes it will be reloaded in memory. Any configuration instances from `Configurations.get` will not be modified. Subsequent calls to `Configurations.get` will return the new configuration file.

If the call to `Configurations.get` is cached, an additional O(1) map lookup can be avoided - with the implication that changes to the configuration on disk will not be visible. To limit the time configuration is cached, or to always retrieve the latest copy from the cache a wrapper is recommended. 

```java
public ConfigurationClass getMyConfiguration() {
    // custom logic to cache the call goes here.
    return Configurations.get("path/to/configurable.yaml", ConfigurationClass.class);
}
```

### Persisting configuration
Persisting configuration is easy, perform any changes to the configuration class in memory and call the save method on the configurable object.

```java
myConfiguration.fastMode = true;

// saves the configuration to disk, location is specified by the 'getPath' method.
myConfiguration.save();
```

Whenever the application is shutdown gracefully, a hook is executed to save all configuration to disk.

## Configuration Factory
The configuration factory allows more granular control of the loading/unloading of configuration. It can be used to add support for new configuration file formats in the `Configurations` implementation.

### Using custom formats
By default the following file extensions are supported

- YAML: the file has the .yml or .yaml file extension.
- JSON: the file has the .json file extension.

To use a custom format a custom `com.codingchili.core.files.FileStore` needs to be implemented. It's pretty simple, it needs a method for reading a buffer of the desired configuration format into a `io.vertx.core.JsonObject`. It also needs a method for writing a JsonObject into the custom format at the specified path. The reason for the JsonObject is that it's what is being used internally to represent the configuration. The configuration format on disk is **not** tied to the JSON format.

[Example of a custom mapper](https://github.com/codingchili/chili-core/blob/master/core/main/java/com/codingchili/core/files/YamlFileStore.java).

The custom mapper needs to be registered on the `com.codingchili.core.files.ConfigurationFactory`.

This can be done with the following sample,
```java
ConfigurationFactory.add(new YamlFileStore());
```

This registers the file extensions provided by the `getExtensions` method on the `FileStore`. The `ConfigurationFactory` can be used directly but does not include support for reloading configuration files on change. Good use cases for the `ConfigurationFactory` are when more control is required when files are being loaded/reloaded or when loading multiple configuration files at once. 

## Reloading files with a configuration factory

The `ConfigurationFactory` may be combined with a `com.codingchili.core.files.FileWatcher` to handle reloading with a `com.codingchili.core.files.FileStoreListener`.

Example of using the ConfigurationFactory with a FileWatcher

```java
// creates a FileWatcher on the given directory that checks the WatchService each 1500ms.
FileWatcher.builder(core)
    .onDirectory('path/to/directory/')
    .rate(() -> 1500)
    .withListener(new FileStoreListener() {
        @Override
        public void onFileModify(Path path) {
            Fruit fruit = Serializer.unpack(
                    ConfigurationFactory.readObject(path.toString()), Fruit.class);

            // a local cache of fruits.
            fruits.put(fruit.getName(), fruit);
        }
   
        @Override
        public void onFileRemove(Path path) {
            // called when a file in the directory is removed.
        }
    }).build();
```

## Framework configuration
This section explains the different ways of configuring the framework. It is recommended to configure these before actually starting the application. This is because some configuration properties are only applicable before the application has started. An example of a property that cannot be set after the application has started is 'clustering' on the launcher.

The configurations that are covered here are retrieved from `Configurations`.
- `Configurations.launcher()`
- `Configurations.system()`
- `Configurations.security()`
- `Configurations.storage()` and `Configurations.storage(StoragePlugin.class)`

### Launcher
The launcher configuration  allows for configuration of the following properties.

|property|description|
|---|---|
| clustered | indicates if we are starting up a Hazelcast cluster with discovery.|
| configurationDirectory| set the base directory where configuration is loaded from.|
| wanOnDefaultsLoaded| warn when configuration defaults are loaded using `Configurations.get(..)`.|
| application| used for logging, the name of the application.|
| version| used for logging, the version of the application.|
| blocks | A list of services to deploy when the Launcher is started, can be referenced by name.|
| hosts | used to determine which block of services to deploy.|

If there is no matching host and a block has not been explicitly specified a block named `default` will be deployed.

Sample configuration file
```yaml
version: CORE-1.0.5-PR
application: prototype
clustered: false
warnOnDefaultsLoaded: true
blocks:
  services:
  - com.codingchili.logging.Service
  - com.codingchili.authentication.Service
  - com.codingchili.realm.Service
  gateway:
  - com.codingchili.router.Service
hosts:
  kraken: services
  raspi: gateway

```

If the `Launcher` is used to deploy the application the hostname will determine which `Services` to deploy. More on the launcher and services in the next chapters. Services may also be deployed directly on the context without the use of the Launcher. 

Note: it is recommended to not set the base directory to the same as the application root, as this will force the WatchService to potentially watch a great number of files. Defaults to storing configuration in `./conf`.

Note: clustering is required for the HazelMap storage implementation.

### System
The system configuration contains properties used for the framework core functions.

|property|description|
|---|---|
|options| a [VertxOption](https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html) object.  |
|metrics| boolean indicates if metrics should be captured or not. |
|metricRate| how often to capture metrics in milliseconds, default 15s. |
|services| the number of services to deploy when deploying a service. |
|handlers| the number of handlers to deploy when deploying a handler. |
|listeners| the number of listeners to deploy when deploying a listener. |
|deployTimeout| time in milliseconds after which a deployment times out and fails. |
|shutdownLogTimeout| time in milliseconds the shutdown hook will wait before terminating the application. |
|configurationPoll| how often the WatchService should be polled for file changes. |
|cachedFilePoll| how often the `CachedFileStore` should poll the WatchService. |
|consoleLogging| boolean determines if logging events are printed to console out. |
|workerPoolSize| the size of the worker pool. |
|clusterTimeout| time in ms until clustered messages are timed out. |
|blockedThreadChecker| how often to check for blocked threads in milliseconds. |
|maxEventLoopExecuteTime| the maximum number of milliseconds the event loop may block until a warning is generated, in milliseconds. |

### Security
Controls internal security configuration.

|property|description|
|---|---|
| dependencies | can be used with the AuthenticationGenerator to generate secrets/tokens.|
| keystores | a list of keystore configurations.|
| argon | configuration for hashing, 'iterations', 'memory', 'parallelism', 'hashLength', saltLength.|
|hmacAlgorithm|algorithm used to sign tokens.|
|secretBytes|the length of generated pre-shared secrets from `SecretFactory`.|
|tokenttl|how long tokens generated from the `TokenFactory` are valid.|

Sample configuration
```yaml
dependencies:
  service/[^/]*:
    preshare:
    - global
    secrets: []
    tokens:
      logging:
        service: logserver
        secret: secret
  service/logserver:
    preshare: []
    secrets:
    - secret
    tokens: {}
keystores:
- password: "flashcards"
  path: "keystore.jks"
  shortName: "keystore.jks"
argon:
  iterations: 1
  memory: 8192
  parallelism: 4
  hashLength: 32
  saltLength: 16
hmacAlgorithm: "HmacSHA512"
signatureAlgorithm: "SHA256withRSA"
secretBytes: 64
tokenttl: 604800
```

Dependencies are used to instantiate configuration in multiple separate files.
- a secret from service A can be used to sign a token for service B.
- a secret from service A can be shared with service B.

Defined secrets and preshares are generated by a CSPRNG using the `SecretFactory`, the values in the sample configuration are just declarations - the generated secrets will be placed in the files that matches the regular expression. Tokens reference a specific service and the name of the secret to use, again this is just for referencing - the secrets will be generated and replaced in the matching files.

### Storage
Contains settings for storage implementations, these can be any of the included or a custom implementation.

|property|description|
|---|---|
|storage|a `RemoteStorage` plugin configuration entry.|
|maxResults|number of max results that may be retrieved from the database at any time.|
|minFeedbackChars|the minumum number of characters required for like/contains queries.|

It's up to the storage implementation to respect the `maxResults` and `minFeedbackChars` properties. The default storage implementations honor them.

Configuration for a specific plugin can be retrieved with
```java
RemoteStorage config = Configurations.storage(Plugin.class);
```

A remote storage configuration contains the following properties

|property|description|
|---|---|
|host|the hostname of the database, if applicable.|
|port|the port of the database, if applicable.|
|database|the name of the storage file on disk, a collection or a database/schema.|
|persisted|some implementations support both in-memory and persisted mode.|
|persistInterval|some implementations that support persistence will persist at this interval.|
|properties|storage configuration per-implementation extra options.|

Sample configuration
```yaml
storage:
  com.codingchili.core.storage.ElasticMap:
    host: 'localhost'
    db_name: 'db'
    port: 9300
    properties:
      mappings:
        properties:
          timestamp:
            type: 'date'
      settings:
        index.mapping.total_fields.limit: 1000
  com.codingchili.core.storage.LocalMap:
    host: 'localhost'
    db_name: 'db'
    port: 27017
  com.codingchili.core.storage.PrivateMap:
    host: 'localhost'
    db_name: 'db'
    port: 27017
  com.codingchili.core.storage.MongoDBMap:
    host: 'localhost'
    db_name: 'db'
    port: 27017
  com.codingchili.core.storage.HazelMap:
    host: 'localhost'
    db_name: 'db'
    port: 27017
  com.codingchili.core.storage.JsonMap:
    host: 'localhost'
    db_name: 'db'
    port: 27017
maxResults: 32
minFeedbackChars: 3
```

It's possible to add custom implementations here. Either in the configuration file or using the API on the `StorageSettings` object.

Adding a custom storage plugin,

```java
// RemoteStorage is the configuration, Class is a class that implements AsyncStorage<Value> where value extends `Storable`.
Configurations.storage().add(RemoteStorage, Class);
```

For more information on storages see the storage section.