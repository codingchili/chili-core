# Logging

This section covers logging, both local and remote logging can be used and preferrably with ElasticSearch for indexing and Kibana for visualization.

- There is a storage implementation for ElasticSearch that can be used for indexing.
- There are two main types of loggers, local and remote.

Javadoc [here](javadoc/com/codingchili/core/logging/package-summary.html)

##### Local

A local logger will write its output to the terminal, or a file if stdout is redirected.

Creating a local logger, does not require a `Context`.

```java
Logger logger = new ConsoleLogger(getClass());
```

##### Remote

A remote logger will publish the logging event on the event bus. At which point it is important that there is a service that is listening for logging events.

The listener should be a cluster based listener that listens on the syslog address. Captured logging events should be indexed for search. It is also recommended to base visualizations on logging data.

It is configurable whether a remote logger should print to the terminal or not.

Grabbing a (remote+console) logger is easy,

```java
public static void main(String[] args) {
    CoreContext core = new SystemContext();
    
    Logger logger = core.logger(getClass());
}
```

### Examples

Logging an event is easy

```java
logger.event("onTimeZoneChanged").send();
```

Logging at a specific level.
```java
logger.event("onTimeZoneFailure", Level.WARNING).send();
```

Logging with a simple message
```java
logger.event("message").send("one message text.");
```

Logging with some additional key-value based data.
```java
logger.event("onMeasurement")
    .put("temperature", "over 9000.")
    .send("periodic reading of nuclear reactor complete.");
```

Logging an error with a stacktrace
```java
logger.onError(throwable);
```

Additionally there are some pre-defined events that can be used.

See javadoc [here](javadoc/com/codingchili/core/logging/Logger.html)

### Events

Logging events are always constructed as `io.vertx.core.JsonObject` and sent to a remote as a JSON object. This makes it possible to specify key-value pairs in the logging messages, to ease indexing and search.

### Levels

Different logging levels for different purposes.

|Level|Description|
|---|---|
|INFO|The default level, good for any info.|
|WARNING|Something that might be an issue.|
|STARTUP|Used during the startup of the application.|
|ERROR|Something went wrong, the application continues.|
|SPECIAL|Just another color, use for whatever.|
|RESERVED|Reserved for something, paws off.|
|NONE|Unspecified, unclassified, mysterious.|
|SEVERE|Something went wrong and it's not temporary.|

Severe is meant to signal that a critical component of the application is malfunctioning, for example failure to connect to a backend database. 

It is recommended to set up log indexing, visualization and alerting based on these levels.

##### Defining custom logging levels

It's possible to customize the available logging levels.

Adding a new logging level

```java
public static LogLevel OVER_9000 = new LogLevel() {
    
    {
        // needs to be registered before use as the ConsoleLogger also supports
        // parsing logging events from json, which only include the level name.
        LogLevel.register(this);
    }
    
    @Override
    public String getName() {
        return "OVER9000!!"
    }
    
    @Override
    public Ansi apply(Ansi ansi) {
        // bright bold red text.
        return ansi.fgBright(Ansi.Color.RED).bold();
    }
}

Logger logger = context.logger(getClass());
logger.event("testing", OVER_9000).send();
```

### Performance metrics

When performance monitoring is enabled a remote logger will be used to send the metrics snapshot to the logging node. The logging node should index this event as any other event. 

In the visualization platform these messages will have the event type of `metrics`.

Enabling performance monitoring
```java
    SystemSettings settings = Configurations.system();
    settings.setMetrics(true);
    
    // defaults is 15s.
    settings.setMetricRate(25000); // 25s
```

Metrics and rate can be modified while the application is running.





