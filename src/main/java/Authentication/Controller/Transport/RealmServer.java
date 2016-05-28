package Authentication.Controller.Transport;

import Authentication.Controller.PacketHandler;
import Authentication.Controller.Protocol;
import Authentication.Controller.RealmRequest;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Model.AuthorizationHandler;
import Authentication.Model.AuthorizationRequiredException;
import Authentication.Model.HandlerMissingException;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Protocol.Packet;
import Utilities.Serializer;
import Utilities.TokenFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.ServerWebSocket;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class RealmServer extends AbstractVerticle {
    private HashMap<String, RealmConnection> connections = new HashMap<>();
    private Protocol<PacketHandler<RealmRequest>> protocol;
    private AuthServerSettings settings;
    private TokenFactory tokens;

    public RealmServer(Provider provider) {
        this.protocol = provider.realmProtocol();
        this.settings = provider.getAuthserverSettings();
        this.tokens = new TokenFactory(settings.getRealmSecret());

        protocol.use(Protocol.REGISTER, this::register, Access.PUBLIC);
    }

    private void register(RealmRequest request) {
        if (tokens.verifyToken(request.token())) {
            request.connection().authenticate(request.token().getDomain());
            request.accept();
        } else {
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
                handle(Protocol.CLOSE, new RealmWebsocketRequest(connections.remove(socket.textHandlerID())));
            });

        }).listen(settings.getRealmPort());

        future.complete();
    }

    public void handle(String action, RealmRequest request) {
        try {
            protocol.get(action, access(request)).handle(request);
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
        }
    }

    private Access access(RealmRequest request) {
        if (request.connection().authenticated())
            return Access.AUTHORIZE;
        else
            return Access.PUBLIC;
    }

    private RealmConnection getConnection(ServerWebSocket socket) {
        if (connections.containsKey(socket.textHandlerID())) {
            return connections.get(socket.textHandlerID());
        } else {
            return connections.put(socket.textHandlerID(), new RealmConnection(socket));
        }
    }
}
