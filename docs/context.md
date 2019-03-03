#Context
The context provides access to core functionality, such as worker pools, service deployments, the event bus and the vertx instance.
Using the context directly is one way of deploying the services, handlers and listeners of the application.

[javadoc](http://localhost:8081/com/codingchili/core/context/CoreContext.html)

## Context lifecycle
A context is required to deploy services, create a new context with

```$java
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
```$java
SystemContext.clustered(core -> {
    // core is a clustered context here - deploy some services.
    // the event bus is clustered and the Hazelcast distributed map available.
});
```

##### Undeploying

Undeploying a service, this operation is asynchronous.
```$java
core.stop(deploymentId);
```
The stop method of the CoreDeployable will then be invoked, it is then up to the server to stop
any deployments it has made during it's lifetime. Usually deployments will live for the duration of the application.


Stopping the application, this will invoke the stop method of all core deployments.
```$java
core.close(() -> {
    // invoked when the close operation has succeeded.
});
```


##### Listeners
It's possible to use the following listeners to be notified when the context
lifecycle state changes.

```$java
// listener invoked when the context shuts down.
ShutdownListener.subscribe(() -> {
    // do something here.
});

// listener invoked when a context is started.
StartupListener.subscribe(core -> {
    // do something with core here.
});
```

## Context methods
The context contains some additional functionality listed here.


- Access to the asynchronous vert.x [FileSystem](https://vertx.io/docs/apidocs/io/vertx/core/file/FileSystem.html) implementation.
```$java
FileSystem fs = core.fileSystem();
```

- Scheduling support
```$java
core.periodic(() -> 100, "10xPoll", (id) -> {
    // invoked every 100ms, the interval is a supplier that can be modified during runtime.
    
    // the periodic tasks id can be used to cancel it.
    core.cancel(id);
});

long timerTask = core.timer(250, (id) -> {
  // this handler will be invoked once, after 250ms has passed.
});

// may be invoked before the timer task is triggered to cancel it.
core.cancel(timerTask);
```

- Running blocking code
```$java
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
```$java
// use the name of the current class, or pass a specific class.
// see the chapter on logging for more information.
Logger logger = context.logger(getClass());
```

- Accessing the Vert.x event bus
```$java
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
```$java
core.vertx();
```

- Get the system settings
```$java
SystemSettings system = core.system();
```