# Listeners

A listener listens for incoming request, for example over the network.

A list of default listeners

|Name|Description|
|---|---|
|RestListener|Listens for incoming JSON/REST requests.|
|TcpListener|Listens for incoming TCP connections.|
|WebsocketListener|Listens for websocket connections.|
|UdpListener|Listens for UDP datagrams, request size limited to MTU.|
|ClusterListener|Listens for messages over the local / clustered event bus.|

The typical setup for distributed services is to use a gateway listener that listens for
requests and then forward these requests over the cluster to the target service. The target service usually runs the 
ClusterListener.

### Starting a listener
Starting a listener is done using the `CoreContext`,

```java
ListenerSettings settings = new ListenerSettings()
    .setPort(8080) // not applicable to the ClusterListener.
    .setSecure(false);

core.listener(() -> 
    new RestListener()
        .settings(() -> settings)
        .handler(MyHandler::new)
});
```

### Using custom listeners
A custom listener can be implemented with the following
```java
// sample listener that retrieves unread smses from an sms gateway API.
public class SmsListener implements CoreListener {
    // SmsGateway is a third-party implementation of an sms gateway.
    private SmsGateway gateway = new SmsGateway("https://some-gateway-service.com/", "api-key");
    private ListenerSettings settings;
    private CoreHandler handler;
    private CoreContext core;
    private boolean running = false;

    @Override
    public void init(CoreContext context) {
        // optional override: only if we need the context.
        this.core = context;            
    }

    @Override
    public void settings(Supplier<ListenerSettings> settings) {
        this.settings = settings.get();
    }

    @Override
    public void handler(CoreHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void start(Future<Void> start) {
        core.periodic(() -> 1000, "smsGatewayPoll", (id) -> {
            if (running) {
                List<JsonObject> smses = smsGateway.fetchUnread();
                
                for (JsonObject sms: smses) {
                    handler.handle(new SmsRequest(smsGateway, sms));
                }
            } else {
                core.cancel(id);
            }
        });
        
        running = true;
        start.complete();
    }
    
    @Override
    public void stop(Future<Void> stop) {
        // optional override: only if cleanup is required.
        running = false;
        stop.complete();
    }

}
```

A request object is required to handle responses and usage in the handler/protocol classes.
```java
public class Smsrequest implements Request {
    private AtomicBoolean written = new AtomicBoolean(false);
    private SmsGateway gateway;
    private JsonObject body;
    
    // the constructor needs a context and the message body.
    public SmsRequest(SmsGateway gateway, JsonObject body) {
        this.gateway = gateway;
        this.body = body;
    }
    
    @Override
    public JsonObject data() {
        // the body can use any format, as long as we can use the JsonObject
        // API to interact with it.
        return body;
    }
    
    @Override
    public void write(Object object) {
        // the object can be an error or a response message.
        // serialize it to whichever format is used by the API.
        gateway.send(Serializer.json(object));
        
        written.set(true);    
    }
    
    @Override
    public void connection() { 
        // there is no connection - our fictive API is using REST.
        // we can create a connection-like object anyways before a response is written.
        return new Connection((body) -> {
            
            if (written.get()) {
                throw new CoreRuntimeException("Connection closed: response already written.");
            } else {
                write(body);            
                written.set(true);
            }
            
        }, UUID.randomUUID().toString());    
    }
    
    @Override
    public int size() {
        // should return the number of bytes in the message body.
        return body.size();
    }
}
```

**Other examples that may be usable**
- an email listener
- mqtt listener
- console listener
- queue listeners; Hazelcast, Rabbit, Kafka etc.