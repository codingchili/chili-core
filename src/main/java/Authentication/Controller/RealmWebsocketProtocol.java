package Authentication.Controller;

import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Protocol.Packet;
import Utilities.Logger;
import Utilities.Serializer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class RealmWebsocketProtocol extends AbstractVerticle implements RealmProtocol {
    private HashMap<String, RealmConnection> connections = new HashMap<>();
    private HashMap<String, RealmPacketHandler> authorized = new HashMap<>();
    private HashMap<String, RealmPacketHandler> unauthorized = new HashMap<>();
    private Access accessLevel;
    private AuthServerSettings settings;
    private Logger logger;
    private Vertx vertx;


    public RealmWebsocketProtocol(Provider provider, Access access) {
        this.settings = provider.getAuthserverSettings();
        this.logger = provider.getLogger();
        this.accessLevel = access;

        this.use(RealmProtocol.AUTHENTICATE, this::authenticate, Access.PUBLIC);
    }

    private void authenticate(RealmRequest request) {
        RealmConnection connection = connections.get(request.connection());

    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public RealmProtocol use(String action, RealmPacketHandler handler) {
        return use(action, handler, accessLevel);
    }

    @Override
    public RealmProtocol use(String action, RealmPacketHandler handler, Access level) {
        switch (level) {
            case PUBLIC:
                unauthorized.put(action, handler);
                break;
            case AUTHORIZE:
                authorized.put(action, handler);
        }
        return this;
    }

    @Override
    public void start(Future<Void> future) {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                String action = ((Packet) Serializer.unpack(event.toString(), Packet.class)).getHeader().getAction();
                RealmConnection connection = connections.get(socket.textHandlerID());
                RealmRequest request = new RealmWebsocketRequest(connection, event.toJsonObject());

                if (unauthorized.containsKey(action)) {
                    unauthorized.get(action).handle(request);
                } else {
                    if (connection.isAuthenticated()) {
                        authorized.get(action).handle(request);
                    }
                }
            });

            socket.endHandler(event -> {
                authorized.get(RealmProtocol.CLOSE).handle(
                        new RealmWebsocketRequest(connections.remove(socket.textHandlerID()), null));
            });

        }).listen(settings.getRealmPort());
    }
}
