package Authentication.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Controller.RealmHandler;
import Authentication.Controller.RealmRequest;
import Logging.Model.Logger;
import Protocols.Access;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
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
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                RealmRequest request = new RealmWebsocketRequest(getConnection(socket), event.toJsonObject());

                handle(request);
            });

            socket.endHandler(event -> {
                logger.onRealmDisconnect(getConnection(socket).realm());
                handle(new RealmWebsocketRequest(connections.remove(socket.textHandlerID())));
            });

            connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }).listen(settings.getRealmPort());

        future.complete();
    }

    public void handle(RealmRequest request) {
        try {
            handler.process(request);
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            e.printStackTrace();
        }
    }

    private RealmConnection getConnection(ServerWebSocket socket) {
        if (connections.containsKey(socket.textHandlerID())) {
            return connections.get(socket.textHandlerID());
        } else {
            return connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }
    }
}
