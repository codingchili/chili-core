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
Whenever configuration changes it will be reloaded in memory (hot reload).

Hot reload is enabled by default, which is convenient during development. **In production it is recommended to disable it**:
anything that can write to the configuration directory can otherwise change the behavior of a running application
(security settings, keystores, listeners and so on). Disable it in `conf/system/system.yaml`,

```yaml
configurationReload: false
```

or in code with `Configurations.system().setConfigurationReload(false)`. Configuration files are then read once, and
changes on disk are ignored until the application is restarted. No file watcher is started, and the system
configuration file is not watched either: once disabled, hot reload can't be enabled again without a restart. A
message is logged at startup when hot reload is disabled.

When hot reload is enabled, any configuration instances from `Configurations.get` will not be modified. Subsequent calls to `Configurations.get` will return the new configuration file.

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

### Durations
Time settings are `java.time.Duration`s, written short in the configuration files: a number with a unit, `ms`, `s`,
`m`, `h` or `d`, combinable with the largest unit first: `250ms`, `45s`, `10m`, `1h30m`, `7d`. A plain number is
milliseconds, or the unit the setting held before it became a duration (the token lifetimes read plain numbers as
seconds), so older configuration files keep their meaning. Anything else (`1.5h`, `PT10M`, `-5m`, `7 d`) fails the
load with an error that names the value. Configuration that is written (`save`, the defaults) uses the short form.

`com.codingchili.core.configuration.Durations` parses and formats the form, and has the Jackson (de)serializers for
fields of your own configuration classes; annotations on the field apply to its getter and setter as well.

```java
public class MySettings implements Configurable {
    @JsonDeserialize(using = Durations.Read.class)          // plain numbers are ms
    @JsonSerialize(using = Durations.Write.class)
    private Duration expiry = Duration.ofDays(7);

    @JsonDeserialize(using = Durations.ReadSeconds.class)   // plain numbers are seconds (ReadDays: days)
    @JsonSerialize(using = Durations.Write.class)
    private Duration lifetime = Duration.ofHours(1);
    // getters and setters of Duration ...
}

Durations.parse("1h30m");                  // PT1H30M
Durations.format(Duration.ofMillis(1500)); // 1s500ms
Durations.millis(settings.getTimeout());   // an int of milliseconds (capped), for TimerSource and Request.timeout()
```

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
// creates a FileWatcher on the given directory, changes are collected for up to 500ms before the listener is called.
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
| cluster | clustering: an object, see [Cluster](#cluster). `clustered: true` (deprecated) is read as `cluster.enabled`.|
| configurationDirectory| set the base directory where configuration is loaded from.|
| wanOnDefaultsLoaded| warn when configuration defaults are loaded using `Configurations.get(..)`.|
| application| used for logging, the name of the application.|
| version| used for logging, the version of the application.|
| blocks | A list of services to deploy when the Launcher is started, can be referenced by name.|
| hosts | used to determine which block of services to deploy.|

**Configuration files by name.** Each deployable reads the configuration files named after it (`ServiceFiles`): a
deployable in the package `<...>.<name>` (e.g. `com.example.realm.Service`, name `realm`) reads `service/<name>.yaml`,
the files in `service/<name>/` and in `<name>/` (relative to `configurationDirectory`; e.g. `conf/realm/` with a file
per realm). `generate --encrypt` (who may open a file's secrets) and `bake` (the files of each block) use it, see
[Secrets store, encryption, bake](#secrets-store-encryption-bake); a service that loads a file with secrets outside
of the files of the services in its JVM is reported (`ConfigurationGuard`, see [System](#system) `strict`). The
`configs` map of before 1.4.9 is ignored: name the file as the service instead.

Without a block on the command line (`deploy`), the block of the host is deployed: a `hosts` entry for the hostname, a
block named as the hostname (a container with `hostname: realm` deploys the block `realm`), or a `hosts` entry for an
address of the host or `localhost`. When `hosts` is configured and none of them matches, the launcher fails and names
the hostname and the configured hosts and blocks; only without `hosts` the block `default` is deployed. A block that is
not configured always fails (`BlockNotConfiguredException`), it never deploys `default`.

Sample configuration file
```yaml
version: CORE-1.0.5-PR
application: prototype
cluster:
  enabled: false
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

#### Cluster
`cluster` in the launcher configuration: whether the launcher starts a clustered Vert.x, and how members find each
other. Applied to the Hazelcast config (cluster.xml) and the event bus options when chili-core starts a clustered
Vert.x (`SystemContext.clustered()`).

|property|description|
|---|---|
| enabled | start a clustered Vert.x, default false. |
| name | the Hazelcast cluster name: members only join a cluster with the same name. Default: the launcher's `application`. |
| multicast | find members by multicast (a LAN), default true. |
| cloud | Hazelcast's auto-detection: the discovery of a detected cloud (AWS, GCP, Azure, Kubernetes), default true. |
| members | hosts to join by TCP/IP (`host` or `host:port`, an entry may hold `a,b,c`), for networks without multicast. Set: TCP/IP only. |
| host | the address other members reach this one at (NAT, separate hosts): Hazelcast's public address and the event bus's advertised host. |
| bind | the address the event bus listens on. Default: every interface when `host` is set, otherwise Hazelcast's member address. |
| port | the event bus port, default 0: random. Fixed to open it in a firewall or publish it from a container. |

Discovery: with `members`, TCP/IP to those hosts. Otherwise with `cloud` on, Hazelcast's auto-detection: a cloud's
discovery when it detects one, else multicast (Hazelcast's fallback), so a LAN works with `multicast` either way.
With `cloud` off and `multicast` on: multicast only, no cloud probes. Both off: the join of cluster.xml as written.
Hazelcast does not use auto-detection while multicast is enabled in its config, so chili-core enables only one.

Every property is overridden by a system property `chili.cluster.<property>`, then an environment variable
`CHILI_CLUSTER_<PROPERTY>` (`chili.cluster.members` / `CHILI_CLUSTER_MEMBERS`, ...); blank values are ignored.
`enabled` takes on/off, true/false, yes/no, 1/0. Members given by an override also enable clustering, unless
`enabled` is overridden too.

```yaml
cluster:
  enabled: true
  name: my-game
  # members: [10.0.0.1, 10.0.0.2]   # TCP/IP instead of discovery
```

Security: open-source Hazelcast has no join authentication, anything that reaches the members with the cluster
name can join. Keep clustered nodes on a private network (LAN, VPN).

### System
The system configuration contains properties used for the framework core functions. Times are
[durations](#durations), a plain number is milliseconds.

|property|description|
|---|---|
|options| a [VertxOption](https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html) object.  |
|metrics| metric settings: `enabled` (boolean, if metrics should be captured), `rate` (how often to capture metrics, default `15s`), filters. |
|services| the number of services to deploy when deploying a service. |
|handlers| the number of handlers to deploy when deploying a handler. |
|listeners| the number of listeners to deploy when deploying a listener. |
|deployTimeout| time after which a deployment times out and fails, default `3s`. |
|shutdownHookTimeout| time that the whole shutdown may take, running blocking tasks are interrupted when it's up, default `5s`. |
|shutdownDelay| time that services keep running after a shutdown has started and the context reports that it is shutting down (readiness fails), default `0s`. Must be less than the timeout. |
|configurationPoll| how often the WatchService should be polled for file changes, default `1s500ms`. |
|configurationReload| boolean, default true. Set to false to disable reloading of configuration when files change. |
|cachedFilePoll| how often the `CachedFileStore` should poll the WatchService, default `1s500ms`. |
|consoleLogging| boolean determines if logging events are printed to console out. |
|eventLoops| the number of event loop threads, defaults to half of the logical processors. |
|workerPoolSize| the size of the worker pool. |
|clusterTimeout| time until clustered messages are timed out, default `3s`. |
|blockedThreadChecker| how often to check for blocked threads, default `1s`. |
|maxEventLoopExecuteTime| the time the event loop may block until a warning is generated, default `2s`. |
|strict| boolean, default false (development). Once the launcher deployed its block, a loaded configuration file with generated secrets (security dependencies) that is not a file of the services deployed in the JVM ([by name](#launcher)) is a warning naming the service that loaded it, the file and the fix; strict (production) fails the start instead, and a later load is logged as an error. Overridden by `-Dchili.strict` or `CHILI_STRICT` (true/false), e.g. set in an image. |

The event bus host and port of a cluster are in the launcher's [cluster](#cluster) settings.

### Security
Controls internal security configuration.

|property|description|
|---|---|
| dependencies | can be used with the AuthenticationGenerator to generate secrets/tokens.|
| keystores | a list of keystore configurations.|
| argon | configuration for hashing, 'iterations', 'memory', 'parallelism', 'hashLength', saltLength.|
|hmacAlgorithm|algorithm used to sign tokens.|
|keyPairAlgorithm|algorithm of the key pairs that `generate` creates and key pair token factories sign and verify with, default `Ed25519`.|
|secretBytes|the length of generated pre-shared secrets from `SecretFactory`.|
|tokenttl|how long a `Token` without an expiry is valid (the tokens that services issue at runtime, such as user sessions), a [duration](#durations), default `7d`; a plain number is seconds.|
|renames|renamed configuration files, old path to new, relative to the configuration directory without the extension (e.g. `service/routing: service/router`): `generate --store` and `bake --store` move the saved values of the old file in the store (plain, `encrypted/` and its record) to the new name, once, with a log line.|
|serviceTokenttl|how long the tokens issued by `generate` (the `tokens` of the dependencies) are valid, a [duration](#durations) (a plain number is seconds), default `tokenttl`; `generate` reissues them in their last quarter. `0s`: they never expire (`Token.NEVER`, 9999-12-31) and are revoked by `--rotate`; `generate` reissues them only when missing or no longer verifying (or once, when issued with a lifetime).|

Sample configuration
```yaml
dependencies:
  service/[^/]*:
    preshare:
    - global
    secrets: []
    publickeys:
    - client
    tokens:
      logging:
        service: logserver
        secret: secret
  service/authentication:
    keypairs:
    - client
  service/logserver:
    preshare: []
    secrets:
    - secret
    tokens: {}
keystores:
- password: "flashcards"
  path: "keystore.jks"
  alias: "keystore.jks"
argon:
  iterations: 1
  memory: 8192
  parallelism: 4
  hashLength: 32
  saltLength: 16
hmacAlgorithm: "HmacSHA512"
signatureAlgorithm: "SHA256withRSA"
secretBytes: 64
tokenttl: 7d
serviceTokenttl: 0s
```

Dependencies are used to instantiate configuration in multiple separate files.
- a secret from service A can be used to sign a token for service B.
- a secret from service A can be shared with service B.
- a key pair: service A signs with the private key (`keypairs`), services B, C verify with the public key
  (`publickeys`) and cannot sign. The keys are written as `<name>PrivateKey` and `<name>PublicKey` (base64, PKCS#8 and
  X.509), see `TokenFactory.signer` and `TokenFactory.verifier`.

Defined secrets and preshares are generated by a CSPRNG using the `SecretFactory`, the values in the sample configuration are just declarations - the generated secrets will be placed in the files that matches the regular expression. Tokens reference a specific service and the name of the secret to use, again this is just for referencing - the secrets will be generated and replaced in the matching files.

The launcher command `generate` (and `generate-secrets`, `generate-preshared`, `generate-tokens`) runs the
`AuthenticationGenerator`:

|command|what it does|
|---|---|
|`generate`| fills in what is missing and keeps the rest: existing secrets, preshared keys and key pairs are kept; a preshared key that some files lack gets the value of the files that have it (when files disagree the most common value wins, a warning names the replaced files); the files that verify with a key pair get the public key of the files that sign with it; a token is kept while it verifies with the current secret of its issuer, belongs to the node and has more than a quarter of `serviceTokenttl` left (and not more than `serviceTokenttl`), otherwise it is reissued for `serviceTokenttl` (with `serviceTokenttl: 0s` tokens never expire and are not reissued for age). Safe to run on every deploy: a new service or realm config gets its keys, nobody else is affected. |
|`generate --rotate`| new values for every secret, preshared key, key pair and token, nothing old is kept: every token issued before is rejected once the services have the new files. Restart the services (a running service may keep a factory with the old key). Use it when a key leaked. |

Each run reads every configuration file once, computes everything in memory and writes each changed file once at the
end; a file is replaced atomically (written to a hidden temporary file next to it, then moved over it), so a hot
reload never reads half a file. A failure writes nothing, closes the generator's context and makes the launcher exit
with status 1.

#### Secrets store, encryption, bake

For a release that carries its secrets (an image built on a build machine), the generated values live outside the
configuration, in a secrets store, and are merged in at build time:

|command|what it does|
|---|---|
|`generate --store <dir> [--rotate]`| reads the generated keys from `<dir>/<path in conf>` (e.g. `<dir>/service/auth.yaml`, plain, only the generated keys) instead of the configuration files, whose own values (development secrets) are never used; fills (or rotates) and writes them back to the store (files 600, folders 700). The configuration files are not written. |
|`generate ... --encrypt [--nodes <dir>]`| also writes every generated value but the public keys as an encrypted value (below) to the nodes that read the file. With `--store` they go to `<dir>/encrypted/` (the store keeps the plain values the next run needs; a run without `--encrypt` removes `encrypted/`), without a store into the configuration files themselves. The public keys of the nodes: `<nodes>/<host>.pub` (default `<store>/nodes`, or `conf/system/nodes`), the text `keygen` printed. A host runs its `hosts` block, or the block named as the host; a file is encrypted to every node whose block has a deployable that reads the file ([by name](#launcher)). A file no node reads gets no encrypted values (a warning). |
|`bake [--store <dir>] [--out <dir>] [--blocks a,b]`| writes every configuration file that has security dependencies to `<out>/<path in conf>` without its generated keys and with the values of the store (`encrypted/` when present, else the plain ones; no store: no secrets at all), and the files each block reads to `<out>/blocks/<block>/<path in conf>` (an image per block). Fails when the store misses a value (run `generate --store` again) or when the nodes changed since `generate --encrypt` (recorded in `encrypted/nodes.txt`). |
|`keygen [--force]`| writes a new X25519 key for this node to `conf/system/node.key` (or the file named by `CHILI_NODE_KEY` / `-Dchili.node.key`), readable by its owner only, and prints its public key (`x25519:<base64>`). Refuses to replace an existing key without `--force`. |

**Encrypted values (opt-in per value).** A configuration value written as `enc:v1:<base64>` is decrypted when
`Configurations` loads the file, with the key of this node; every other value is used as it is. A file with a value
this node can not open (no key file, the key is not a recipient, or the value was changed) fails to load with an
`EncryptedValueException` that names the file and the key (`... can't open the encrypted value of 'bankSecret': this
node's key can't open it ...`); files are loaded by the services that use them, so the files of services that run
elsewhere do not stop a node from starting. A file loaded with encrypted values is never saved back (that would write
the decrypted values). An empty key file (e.g. `/dev/null` mounted in its place) means no key.

Format v1 (`EncryptedValue`, JDK algorithms only): the value as JSON (`{"v": value}`, so tokens and other objects
work too) is encrypted with a random 32 byte data key (ChaCha20-Poly1305, random nonce, the header as AAD); the data key
is wrapped once per recipient with a key from HKDF-SHA256 over the X25519 agreement of a key pair made for this value
with the recipient's public key (salt: both public keys). Layout: version (1), ephemeral public key (32), recipient
count (1), per recipient key id (8, the start of the SHA-256 of its public key) and wrapped key (48), nonce (12),
ciphertext and tag (16). Any changed byte fails the authentication; another version prefix (`enc:v2:`) fails with the
version named.

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
|persistInterval|some implementations that support persistence will persist at this interval, a [duration](#durations), default `3s`.|
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