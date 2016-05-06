package Authentication.Controller;

import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Game.Model.PlayerClass;
import Protocol.Packet;
import Protocol.RealmMetaData;
import Protocol.RegisterRealm;
import Configuration.Config;
import Utilities.*;
import io.vertx.core.Vertx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * handles the connections to realms.
 */
public class RealmHandler {
    private HashMap<String, RealmMethod> handlers = new HashMap<>();
    private HashMap<String, RealmSettings> realms = new HashMap<>();
    private HashMap<String, String> connections = new HashMap<>();
    private AuthServerSettings settings;
    private TokenFactory tokenFactory;
    private Vertx vertx;
    private Logger logger;

    public RealmHandler(Vertx vertx, Logger logger) {
        this.settings = Config.instance().getAuthSettings();
        this.tokenFactory = new TokenFactory(settings.getRealmSecret());
        this.vertx = vertx;
        this.logger = logger;

        registerHandlers();
        startServer();
    }

    private void registerHandlers() {
        handlers.put(RegisterRealm.ACTION, (data, connection) -> {
            RealmSettings realm = (RealmSettings) Serializer.unpack(data.getJsonObject("realm"), RealmSettings.class);
            Token token = (Token) Serializer.unpack(data.getJsonObject("token"), Token.class);
            realm.getAuthentication().setToken(token);

            if (authorize(realm, token)) {
                if (realms.containsKey(connection))
                    logger.onRealmUpdated(realm);
                else
                    logger.onRealmRegistered(realm);

                realm.setTrusted(settings.isPublicRealm(realm.getName()));

                connections.put(connection, realm.getName());
                realms.put(realm.getName(), realm);
            } else {
                logger.onRealmRejected(realm);
            }
        });
    }

    private boolean authorize(RealmSettings realm, Token token) {
        return tokenFactory.verifyToken(token) && (token.getDomain().equals(realm.getName()));
    }

    private void startServer() {
        vertx.createHttpServer().websocketHandler(connection -> {

            connection.handler(event -> {
                Packet packet = (Packet) Serializer.unpack(event.toJsonObject(), Packet.class);
                handlers.get(packet.getAction()).handle(event.toJsonObject(), connection.textHandlerID());
            });

            connection.endHandler(event -> {
                unregister(connection.textHandlerID());
            });

        }).listen(settings.getRealmPort());
    }

    private void unregister(String connection) {
        RealmSettings realm = realms.get(connections.get(connection));
        connections.remove(connection);
        realms.remove(realm.getName());
        logger.onRealmDeregistered(realm);
    }

    public ArrayList<RealmMetaData> getMetadataList() {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (RealmSettings realm : realms.values()) {
            list.add(new RealmMetaData(realm));
        }

        return list;
    }

    public Token signToken(String realm, String domain) {
        return new Token(getTokenFactory(realm), domain);
    }

    private TokenFactory getTokenFactory(String realm) {
        return new TokenFactory(realms.get(realm).getAuthentication().getToken().getKey().getBytes());
    }

    public boolean verifyToken(String realm, Token token) {
        return getTokenFactory(realm).verifyToken(token);
    }

    public RealmSettings getRealm(String realm) {
        return realms.get(realm);
    }

    public PlayerCharacter createCharacter(String realmName, String name, String className) throws PlayerClassDisabledException {
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
