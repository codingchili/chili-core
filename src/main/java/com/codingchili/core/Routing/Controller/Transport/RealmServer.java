package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Authentication.Controller.RealmHandler;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.ServerWebSocket;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class RealmServer extends AbstractVerticle {
    private HashMap<String, RealmConnection> connections = new HashMap<>();
    private AuthServerSettings settings;
    private RealmHandler handler;
    private Logger logger;

    public RealmServer(AuthProvider provider) {
        this.settings = provider.getAuthserverSettings();
        this.logger = provider.getLogger();
        this.handler = new RealmHandler(provider);
    }

    @Override
    public void start(Future<Void> future) {
        /*vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                RealmAuthenticationRequest request = new RealmWebsocketRequest(getConnection(socket), event.toJsonObject());

                handler.process(request);
            });

            socket.endHandler(event -> {
                logger.onRealmDisconnect(getConnection(socket).realm());

                handler.process(
                        new RealmWebsocketRequest(connections.remove(socket.textHandlerID()), CLIENT_CLOSE));
            });

            connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }).listen(settings.getRealmPort());

        future.complete();*/
    }

    private RealmConnection getConnection(ServerWebSocket socket) {
        if (connections.containsKey(socket.textHandlerID())) {
            return connections.get(socket.textHandlerID());
        } else {
            return connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }
    }
}
