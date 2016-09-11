package Authentication.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.RealmRequest;
import Authentication.Configuration.AuthServerSettings;
import Configuration.Strings;
import Logging.Model.Logger;
import Protocols.Authentication.RealmRegister;
import Protocols.AuthorizationHandler.Access;
import Protocols.*;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.Serializer;
import Protocols.Authorization.TokenFactory;
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
    private Logger logger;

    public RealmServer(AuthProvider provider) {
        this.protocol = provider.realmProtocol();
        this.settings = provider.getAuthserverSettings();
        this.logger = provider.getLogger();
        this.tokens = new TokenFactory(settings.getRealmSecret());

        protocol.use(RealmRegister.ACTION, this::register, Access.PUBLIC);
    }

    private void register(RealmRequest request) {
        if (tokens.verifyToken(request.token())) {
            request.connection().authenticate(request.token().getDomain());
            try {
                protocol.get(Strings.REALM_AUTHENTICATE, Access.AUTHORIZE).handle(request);
            } catch (AuthorizationRequiredException | HandlerMissingException ignored) {
            }
        } else {
            request.unauthorized();
        }
    }

    @Override
    public void start(Future<Void> future) {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                Packet packet = Serializer.unpack(event.toString(), Packet.class);
                RealmRequest request = new RealmWebsocketRequest(getConnection(socket), event.toJsonObject());

                handle(packet.getAction(), request);
            });

            socket.endHandler(event -> {
                logger.onRealmDisconnect(getConnection(socket).realm());
                handle(Strings.CLIENT_CLOSE, new RealmWebsocketRequest(connections.remove(socket.textHandlerID())));
            });

            connections.put(socket.textHandlerID(), new RealmConnection(socket));
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
