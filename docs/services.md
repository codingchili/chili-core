# Services

A service is the notion of a single or multiple services composed into a single unit with a common purpose.

Services must implement the [CoreService](javadoc/com/codingchili/core/listener/CoreService.html) interface.

Services can be deployed using the [Launcher](launcher).

### Creating your service
A service simply implements the `CoreService` interface.

```java

public class MyService implements CoreService {

    // the main manifest can point to any file.
    public static void main(String[] args) {
        SystemContext.clustered((core) -> {
            // when clustering is complete - deploy the service.
            core.service(MyService::new);
        });    
    }

    @Override
    public void start(Future<Void> start) {
        // complete must always be called in the start method!
        
        // perform your deployments here, then complete the future, or fail it.
        // core.deploy(...);
        
        start.complete();
    }
    
    @Override
    public void stop(Future<Void> stop) {
        // optional override.
        stop.complete();
    }
    
    @Override
    public String name() {
        // optional override: uses the classname by default.
        return "myService.1";
    }
}

```

### Deploying your service
Deployment is done like this,

```java
core.service(MyService::new);
```

An application may deploy 0..n services. A service should be self-contained, can be seen as a microservice.
