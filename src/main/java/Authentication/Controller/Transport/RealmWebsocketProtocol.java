package Authentication.Controller.Transport;

import Authentication.Controller.RealmPacketHandler;
import Authentication.Controller.RealmProtocol;
import Authentication.Controller.RealmRequest;
import Authentication.Model.AuthorizationHandler;
import Authentication.Model.AuthorizationRequired;
import Authentication.Model.HandlerMissingException;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Protocol.Packet;
import Utilities.Serializer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

import java.util.HashMap;

import Authentication.Model.AuthorizationHandler.Access;

/**
 * @author Robin Duda
 */
public class RealmWebsocketProtocol extends AbstractVerticle implements RealmProtocol {
    private AuthorizationHandler<RealmPacketHandler> handlers;
    private HashMap<String, RealmConnection> connections = new HashMap<>();
    private AuthServerSettings settings;
    private Vertx vertx;


    public RealmWebsocketProtocol(Provider provider, Access access) {
        this.settings = provider.getAuthserverSettings();
        this.settings = provider.getAuthserverSettings();
        this.handlers = new AuthorizationHandler<>(access);
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public RealmProtocol use(String action, RealmPacketHandler handler) {
        handlers.use(action, handler);
        return this;
    }

    @Override
    public RealmProtocol use(String action, RealmPacketHandler handler, Access access) {
        handlers.use(action, handler, access);
        return this;
    }

    @Override
    public void handle(String action, RealmRequest request) {
        try {
            Access access = (request.authorized()) ? Access.AUTHORIZE : Access.PUBLIC;
            handlers.get(action, access).handle(request);

        } catch (AuthorizationRequired authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
        }
    }

    @Override
    public void start(Future<Void> future) {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                String action = ((Packet) Serializer.unpack(event.toString(), Packet.class)).getHeader().getAction();
                RealmRequest request = new RealmWebsocketRequest(getConnection(socket), event.toJsonObject());

                handle(action, request);
            });

            socket.endHandler(event -> {
                handle(RealmProtocol.CLOSE, new RealmWebsocketRequest(connections.remove(socket.textHandlerID())));
            });

        }).listen(settings.getRealmPort());
    }

    private RealmConnection getConnection(ServerWebSocket socket) {
        if (connections.containsKey(socket.textHandlerID())) {
            return connections.get(socket.textHandlerID());
        } else {
            return connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }
    }
}
