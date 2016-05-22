package Authentication.Controller;

import Authentication.Model.AsyncAccountStore;
import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Protocol.Authentication.RealmMetaData;
import Protocol.Game.CharacterRequest;
import Protocol.Game.CharacterResponse;
import Protocol.Packet;
import Configuration.Config;
import Protocol.RealmRegister;
import Protocol.RealmUpdate;
import Utilities.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         Router used to authenticate realms and generate realm lists.
 */
public class RealmHandler {
    private HashMap<String, RealmPacketHandler> handlers = new HashMap<>();
    private HashMap<String, RealmSettings> realms = new HashMap<>();
    private HashMap<String, RealmConnection> connections = new HashMap<>();
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;
    private TokenFactory tokenFactory;
    private Vertx vertx;
    private Logger logger;

    public RealmHandler(Vertx vertx, Logger logger, AsyncAccountStore accounts) {
        this.accounts = accounts;
        this.settings = Config.instance().getAuthSettings();
        this.tokenFactory = new TokenFactory(settings.getRealmSecret());
        this.vertx = vertx;
        this.logger = logger;

        handlers.put(RealmUpdate.ACTION, this.realmUpdate);
        handlers.put(CharacterRequest.ACTION, this.characterRequest);

        startServer();
    }


    private RealmPacketHandler realmRegister = (RealmConnection connection, String data) -> {
        RealmRegister register = (RealmRegister) Serializer.unpack(new JsonObject(data), RealmRegister.class);
        RealmSettings realm = register.getRealm();

        if (authorize(realm)) {
            realm.setTrusted(settings.isPublicRealm(realm.getName()));

            connections.put(connection.getId(), new RealmConnection(connection, realm.getName()));
            realms.put(realm.getName(), realm);

            connection.write(Buffer.buffer(Serializer.pack(new RealmRegister(true))));
        } else {
            connection.write(Buffer.buffer(Serializer.pack(new RealmRegister(false))));
        }
    };

    private boolean authorize(RealmSettings realm) {
        Token token = realm.getAuthentication().getToken();
        return tokenFactory.verifyToken(token) && (token.getDomain().equals(realm.getName()));
    }

    private RealmPacketHandler realmUpdate = (RealmConnection connection, String data) -> {
        RealmUpdate update = (RealmUpdate) Serializer.unpack(new JsonObject(data), RealmUpdate.class);
        realms.get(connection.realm).setPlayers(update.getPlayers());
    };


    private RealmPacketHandler characterRequest = (connection, data) -> {
        CharacterRequest request = (CharacterRequest) Serializer.unpack(data, CharacterRequest.class);
        Future<PlayerCharacter> find = Future.future();

        find.setHandler(result -> {
            if (result.succeeded()) {
                connection.write(new CharacterResponse(result.result(), request));
            } else
                connection.write(new CharacterResponse().setSuccess(false));
        });

        accounts.findCharacter(find, connection.realm, request.getAccount(), request.getName());

    };


    private void startServer() {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(event -> {
                Packet packet = (Packet) Serializer.unpack(event.toString(), Packet.class);

                // All websocket connections not in connection map are not authenticated.
                if (connections.get(socket.textHandlerID()) == null) {
                    realmRegister.handle(new RealmConnection(socket), event.toString());
                } else
                    handlers.get(packet.getAction()).handle(connections.get(socket.textHandlerID()), event.toString());
            });

            socket.endHandler(event -> {
                RealmConnection connection = connections.get(socket.textHandlerID());
                connections.remove(connection.id);
                RealmSettings removed = realms.remove(connection.realm);
                logger.onRealmDeregistered(removed);
            });

        }).listen(settings.getRealmPort());
    }

    ArrayList<RealmMetaData> getMetadataList() {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (RealmSettings realm : realms.values()) {
            list.add(new RealmMetaData(realm));
        }

        return list;
    }

    Token signToken(String realm, String domain) {
        return new Token(getTokenFactory(realm), domain);
    }

    private TokenFactory getTokenFactory(String realm) {
        return new TokenFactory(realms.get(realm).getAuthentication().getToken().getKey().getBytes());
    }

    RealmSettings getRealm(String realm) {
        return realms.get(realm).removeAuthentication();
    }
}
