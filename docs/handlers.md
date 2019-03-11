# Handlers

A handler implements the business logic of the API. Listeners forwarads requests to a handler for processing. A 
handler may register the API's of other handlers on it's protocol, as a proxy-handler or simply define its own API.

### Implementing a handler
The dpeloyment of a handler requires at least a `CoreService`, see [listeners](listeners) for a set of listeners that
are included in the framework.

```java
// The address is used by the listener to determine where to mount the handler.
// the REST handler would mount this at "/cats" for example.
@Role(PUBLIC)
@Address("cats")
public class CatHandler implements CoreHandler {
    // this example uses the annotated protocol, see the Protocol chapter for more.
    private Protocol<Request> protocol = new Protocol<>(this);

    // defines an API endpoint reachable at "/cats/meow" when using the RestListener.
    @Api
    public void meow(CatRequest request) {
        MeowModel meow = request.getMeow();
        
        if (meow.getLoudness() < 100) {
            request.write("meow complete.");
        } else {
            request.fail(new MeowLoudnessTooHighException());
        }
    }

    @Override
    public void init(CoreContext core) {
        // optional override: if context is required.    
    }
    
    @Override
    public void start(Future<Void> future) {
        // optional override: if async setup is required.    
    }

    @Override
    public void stop(Future<Void> future) {
        // optional override: if cleanup is required.    
    }
    
    @Override
    public void handle(Request request) {
        protocol.process(new CatRequest(request));
    }
}
```

To simplify dealing with request data we can wrap the request body in a model class. If we use
the `request.data()` only a JsonObject API will be available.

```java
public class CatRequest implements RequestWrapper() {
    private Request request;
    
    // the original request is required for wrapping.
    public CatRequest(Request request) {
        this.request = request;
    }
    
    public MeowModel getMeow() {
        // this can also be done in the handler without a wrapper class.
        return Serializer.unpack(request.data(), MeowModel.class);    
    }
    
    @Override
    public Request request() {
        return request;
    }
}
```

**Another example of a minimal handler without request wrapping,**
```java
@Role(PUBLIC)
@Address("cats")
public class Cathandler implements CoreHandler {
    private Protocol<Request> protcol = new Protocol<>(this);
    
    @Api
    public void meow(Request request) {
        int loudness = request.data().getInteger("loudness");
        request.write("meow in progress at " + loudness + " decibels.");
    }
    
    @Override
    public void handle(Request request) {
        protocol.process(request);
    }
}
```

### Deploying a handler
Deploying a handler requires a running `SystemContext`, example
```java
core.handler(Cathandler::new);
```

When a handler is deployed using the `handler` method on the context the listener will always default to `ClusterListener`.

To deploy the handler on a specific listener the following can be used,

```java
core.listener(() -> new RestListener().handler(new CatHandler()));
```

This will use the default `ListenerSettings` for the `RestListener`. To change the listening port
settings can be set on the listener instance.