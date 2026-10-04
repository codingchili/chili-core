# Launcher

The launcher is an alternative to using `CoreContext` directly for deployments. The Launcher can be combined with dynamically deployed services.

It adds the following functionality
- a CommandExecutor that parses the commandline and executes an operation.
- deployment of service blocks, ie a block of services configured for a specific host.

See: `com.codingchili.core.Launcher` and `com.codingchili.core.context`.

### Deployment configuration

A deployment block contains a set of deployable services. When the `com.codingchili.core.Launcher` is set as the main class
these blocks can be deployed by passing the block name for the launcher option `--deploy <blockName>`. Using hosts mapping we can specify
the block using the hostname of the deploying machine, no block name should then be specified for the deploy command.

Example launcher configuration in YAML.
```yaml
version: CORE-1.0.5-PR
application: prototype
clustered: false
warnOnDefaultsLoaded: true
blocks:
  default:
  - com.codingchili.logging.Service
  - com.codingchili.authentication.Service
  - com.codingchili.realm.Service
  - com.codingchili.router.Service
  - com.codingchili.social.controller.SocialHandler
  - com.codingchili.website.Service
  - com.codingchili.realmregistry.Service
  router:
  - com.codingchili.router.Service
  website:
  - com.codingchili.website.Service
  social:
  - com.codingchili.social.Service
  logger:
  - com.codingchili.logging.Service
  realm:
  - com.codingchili.realm.Service
  authentication:
  - com.codingchili.authentication.Service
hosts:
  laptop4: website
```

Breakdown

|Name|description|
|---|---|
|version|The running version of the application, used for logging etc.|
|application|The name of the running application, used for logging etc.|
|clustered|Indicates if a Hazelcast cluster should be joined.|
|warnOnDefaultsLoaded|Warns whenever configuration is missing on disk and defaults are loaded from the java class.|
|blocks|a list of name:services pair to be deployed when passing the --deploy option.|
|hosts|a list of hosts which has a specific default block to deploy.|

Example using the `com.codingchili.core.Launcher` with another main-class.

```java
public static void main(String[] args) {
    Launcher.main(args);
}
```

Another example with programmatic configuration
```java
public static void main(String[] args) {
    LaunchContext context = new LaunchContext(args);
    
    context.settings()
        .setApplication("sample app")
        .setVersion("1.0.0")
        .setClustered(true);
    
    Launcher.start(context);    
}
```

### Commandline parser

The commandline parser can be used to implement command-line functionality.

This is an example that adds a single command to the default command executor. When invoked with the `--print-cat` option the program
writes cat a few times to the console as specified by the `-upper` option.
To write multiple cats, specify the `-times` option.

The command returns `SHUTDOWN` to indicate that the application should shut down after the command has completed. 

```java
    public static void main(String[] args) {
        LaunchContext context = new LaunchContext(args);
        CommandExecutor executor = context.getExecutor();

        executor.add(command -> {
            int times = Integer.parseInt(command.getProperty("times").orElse("1"));

            for (int i = 0; i < times; i++) {
                System.out.println(command.hasProperty("-upper") ? "CAT" : "cat");
            }
            return LauncherCommandResult.SHUTDOWN;
        }, "--print-cat", "prints the word cat n times.");

        Launcher.start(context);
    }
```

The command receives the executor, which gives access to the parsed properties. Commands that
need to do asynchronous work are added with `addAsync` and return a `Future<CommandResult>`.

```java
executor.addAsync(command -> storage.clear()
        .map(LauncherCommandResult.SHUTDOWN), "--clear", "clears the storage.");
```

`CommandExecutor.execute(String...)` returns a `Future<CommandResult>` that fails with a
`NoSuchCommandException` when the command doesn't exist.

A sample invocation would look like
```console
java -jar <file.jar> --print-cat -upper -times 100
```

The order of the parameters 

Useful interfaces

|Name|Description|
|---|---|
|CommandExecutor|Parses the commandline and executes a matching command.|
|Command|Implementation of an executable command.|
|CommandResult|Indicates if the Launcher should continue with deployment or shut down.|

The types that can be returned from a `Command`

|||
|---|---|
|CONTINUE|After the command is invoked the Launcher should continue with deployment.|
|STARTED|THe command has handled the deployment and the launcher will not.|
|SHUTDOWN|The application will be terminated by the Launcher.|

Note that it is possible to use the `CommandExecutor` implementations without the use of a `Launcher`. In this case it is up to the caller to interpret the result of the command execution.
### JVM options

Some dependencies require module access that is disabled by default on recent JDKs. The chili-core jar
manifest enables this when started with `java -jar`. When chili-core is used as a library on the classpath,
pass the options to the JVM of the application instead,

```console
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.management/sun.management=ALL-UNNAMED
--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
--add-exports java.base/jdk.internal.ref=ALL-UNNAMED
--enable-native-access=ALL-UNNAMED
--enable-final-field-mutation=ALL-UNNAMED
--sun-misc-unsafe-memory-access=allow
```

`java.util` is required by `IndexedMapPersisted` (Kryo serialization), the rest silences warnings from Hazelcast
and jline. Dependencies that log with SLF4J are routed to the `ConsoleLogger`; if the application provides its own
SLF4J backend, SLF4J will warn about multiple providers.
