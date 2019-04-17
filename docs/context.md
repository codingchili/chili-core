# Context

The context provides access to core functionality, such as worker pools, service deployments, the event bus and the vertx instance.
Using the context directly is one way of deploying the services, handlers and listeners of the application.

[javadoc](javadoc/com/codingchili/core/context/CoreContext.html)

## Context lifecycle
A context is required to deploy services, create a new context with

```java
CoreContext core = new SystemContext();
```

It is strongly recommended to only use a single context at a time.

The core context can now be used to deploy any of the following

- a service (CoreService interface)
  - a service is a deployable entity.
  - used to deploy multiple listeners/handlers.
  - one service can be considered a microservice.
- a listener (CoreListener interface)
  - a listener provides means of transport for requests.
  - delegates handling of requests to a handler.
  - default implementations include, TCP, UDP, cluster, websocket and REST.
- a handler (CoreHandler interface)
    - handlers receive requests from a listener.
    - implements the business logic of the API.
    
When a deploy operation has succeeded, a deployment ID is returned. If the service needs to be stopped
or restarted later, this ID needs to be stored. 

##### Clustering
A clustered context will attempt to join  the Hazelcast cluster configured in the cluster.xml on the classpath.

It's also possible to programmatically configure the hazelcast cluster using the `VertxOptions` in the `Configurations.system()` settings.

Example of a clustered context
```java
SystemContext.clustered(core -> {
    // core is a clustered context here - deploy some services.
    // the event bus is clustered and the Hazelcast distributed map available.
});
```

##### Deploying
Deploying a service that may in turn issue more deployments.

```java
// deploys a service that may issue more deployments.
core.service(new CoreServiceImpl());
```

A small example that deploys a listener and a handler without any services.
```java
// deploys a HTTP listener on port 8080 that uses the
// BusForwarder to forward requests over the cluster.
core.listener(new RestListener()
    .settings(() -> {
        new ListenerSettings()
            .setPort(8080)
            .setSecure(false)
    })
    .setHandler(new BusForwarder("orders")));

// deploys the handler with a cluster listener on the address
// specified in the handler with the @Address annotation.
core.handler(new CoreHandlerImpl());
```

In this example, any HTTP requests to port 8080 will be published
onto the event bus on the "orders" address. The request will then be
passed to the `CoreHandlerImpl` handler, which is your handler that
implements the `CoreHandler` interface.

If the `route` of the request matches a method in the handlers API it can be invoked. The `BusForwarder`
works both when in clustered and non-clustered mode.

A BusRouter can be used to dynamically determine the target address.

##### Undeploying

Undeploying a service, this operation is asynchronous.
```java
core.stop(deploymentId);
```
The stop method of the CoreDeployable will then be invoked, it is then up to the server to stop
any deployments it has made during it's lifetime. Usually deployments will live for the duration of the application.


Stopping the application, this will invoke the stop method of all core deployments.
```java
core.close(() -> {
    // invoked when the close operation has succeeded.
});
```


##### Listeners
It's possible to use the following listeners to be notified when the context
lifecycle state changes.

```java
ShutdownListener.subscribe((core) -> {
    // core is an optional reference to the core context.
    // it will be missing if the shutdown hook is invoked
    // before there is a context, during launch for example.
    
    // the listener is optionally async.
    return Future.succeededFuture();
});
```

// listener invoked when a context is started.
StartupListener.subscribe(core -> {
    // do something with core here.
});
```

## Context methods
The context contains some additional functionality listed here.


- Access to the asynchronous vert.x [FileSystem](https://vertx.io/docs/apidocs/io/vertx/core/file/FileSystem.html) implementation.

```java
FileSystem fs = core.fileSystem();
```

- Scheduling support

```java
core.periodic(TimerSource.ofMS(100, "10xPoll"), (id) -> {
    // invoked every 100ms, the interval is a supplier that can be modified during runtime.
    
    // the periodic tasks id can be used to cancel it.
    core.cancel(id);
});

long timerTask = core.timer(250, (id) -> {
  // this handler will be invoked once, after 250ms has passed.
});

// may be invoked before the timer task is triggered to cancel it.
core.cancel(timerTask);


// Examples for TimerSource
TimerSource source = TimerSource.of(4, TimeUnit.DAYS);

// when paused the scheduled operation will not be invoked.
source.pause();
source.isPaused();
source.unpause();

// when terminated the task will be removed from the scheduler.
// the task cannot recover from this state.
source.terminate();
source.isTerminated();

// names can be set for the source, logging an info message when the period changes.
source.setName("poll-storage");

// the TimeSource can use a dynamic period provider
source.setProvider(Supplier<String>);

// whenever the provider changes the next time the scheduled task is invoked
// it will be rescheduled at the new interval after executing once.
TimerSource.of(Configurations::someDynamicValueThatChanges);

```

- Running blocking code

```java
// false indicates that scheduled blocking operations does not have to 
// be executed in order. This parameter is optional and defaults to false.
core.blocking((future) -> {
    future.complete("hello");
}, false, done -> {
    if (done.succeeded()) {
        System.out.println(done.result());
    } else {
        // the blocking operation failed, log err.
        logger.error(done.cause());    
    }
});
```

- Retrieving a logger instance

```java
// use the name of the current class, or pass a specific class.
// see the chapter on logging for more information.
Logger logger = context.logger(getClass());
```

- Accessing the Vert.x event bus

```java
EventBus bus = core.bus();

// listens for incoming message on "address".
bus.consumer("address", (message) -> {
    Object object = message.body();
    
    // object can be a Buffer, String or JsonObject.
    // to pass custom types register an eventbus codec.
    
    message.reply(anotherObject);
});

// broadcasts a message to all consumers.
bus.publish("address", message)

// sends a message to a single consumer.
bus.send("address", message);
```

- Retrieving the underlying Vert.x instance

```java
core.vertx();
```

- Get the system settings

```java
SystemSettings system = core.system();
```

### The shutdown hook 
The shutdown hook will be executed when the JVM is shutting down.

All subscribers in the `ShutdownListener` will be invoked before the context will close and undeploy services. 

The stop method in any running `CoreDeployable` will be invoked from the shutdown hook unaffected by the shutdown timeout. 
The hook will wait for the blocking executor to drain for the duration of the specified shutdown hook timeout. 
The shutdown hook timeout can be configured in the system config.