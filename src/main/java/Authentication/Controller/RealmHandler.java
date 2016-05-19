package Authentication.Controller;

import Authentication.Model.AsyncAccountStore;
import Authentication.Model.PlayerClassDisabledException;
import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Game.Model.PlayerClass;
import Protocol.Authentication.RealmMetaData;
import Protocol.Game.CharacterRequest;
import Protocol.Game.CharacterResponse;
import Protocol.Packet;
import Configuration.Config;
import Protocol.RegisterRealm;
import Utilities.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * handles the connections to realms.
 */
public class RealmHandler {
    private HashMap<String, RealmPacketHandler> handlers = new HashMap<>();
    private HashMap<String, RealmSettings> realms = new HashMap<>();
    private HashMap<String, String> registrations = new HashMap<>();
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

        handleRegister();
        handleCharacterRequest();
        startServer();
    }

    private void handleRegister() {
        handlers.put(RegisterRealm.ACTION, (connection, data) -> {
            JsonObject json = new JsonObject(data);
            RealmSettings realm = (RealmSettings) Serializer.unpack(json.getJsonObject("realm"), RealmSettings.class);
            Token token = (Token) Serializer.unpack(json.getJsonObject("token"), Token.class);
            realm.getAuthentication().setToken(token);

            if (authorize(realm)) {
                if (realms.containsKey(realm.getName()))
                    logger.onRealmUpdated(realm);
                else
                    logger.onRealmRegistered(realm);

                realm.setTrusted(settings.isPublicRealm(realm.getName()));

                registrations.put(connection.textHandlerID(), realm.getName());
                realms.put(realm.getName(), realm);
            } else {
                logger.onRealmRejected(realm);
            }
        });
    }

    private boolean authorize(RealmSettings realm) {
        Token token = realm.getAuthentication().getToken();

        return tokenFactory.verifyToken(token) && (token.getDomain().equals(realm.getName()));
    }

    private void handleCharacterRequest() {
        handlers.put(CharacterRequest.ACTION, (connection, data) -> {
            CharacterRequest request = (CharacterRequest) Serializer.unpack(data, CharacterRequest.class);
            String realm = registrations.get(connection.textHandlerID());
            Future<PlayerCharacter> find = Future.future();

            find.setHandler(result -> {
                if (result.succeeded()) {
                    send(connection, new CharacterResponse(result.result(), request));
                } else
                    send(connection, new CharacterResponse().setSuccess(false));
            });

            accounts.findCharacter(find, realm, request.getAccount(), request.getName());
        });
    }

    private void send(ServerWebSocket socket, Object data) {
        socket.write(Buffer.buffer(Serializer.pack(data)));
    }


    private void startServer() {
        vertx.createHttpServer().websocketHandler(connection -> {

            connection.handler(event -> {
                Packet packet = (Packet) Serializer.unpack(event.toString(), Packet.class);
                handlers.get(packet.getAction()).handle(connection, event.toString());
            });

            connection.endHandler(event -> {
                RealmSettings realm = realms.get(registrations.get(connection.textHandlerID()));
                registrations.remove(connection.textHandlerID());
                realms.remove(realm.getName());
                logger.onRealmDeregistered(realm);
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

    boolean verifyToken(String realm, Token token) {
        return getTokenFactory(realm).verifyToken(token);
    }

    RealmSettings getRealm(String realm) {
        return realms.get(realm);
    }

    PlayerCharacter createCharacter(String realmName, String name, String className) throws PlayerClassDisabledException {
        RealmSettings realm = realms.get(realmName);
        boolean enabled = false;

        for (PlayerClass pc : realm.getClasses()) {
            if (pc.getName().equals(className))
                enabled = true;
        }

        if (enabled) {
            return new PlayerCharacter(realm.getTemplate(), name, className);
        } else
            throw new PlayerClassDisabledException();
    }
}
