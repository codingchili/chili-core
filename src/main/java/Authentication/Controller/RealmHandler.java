package Authentication.Controller;

import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Protocol.Packet;
import Protocol.RegisterRealm;
import Configuration.Config;
import Utilities.Logger;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Vertx;

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
            RegisterRealm request = (RegisterRealm) Serializer.unpack(data, RegisterRealm.class);
            RealmSettings realm = request.getRealm();

            if (authorize(request)) {
                if (realms.containsKey(connection))
                    logger.onRealmUpdated(realm);
                else
                    logger.onRealmRegistered(realm);

                realm.setTrusted(settings.isPublicRealm(realm.getName()));

                realms.put(connection, realm);
            } else {
                logger.onRealmRejected(realm);
            }
        });
    }

    private boolean authorize(RegisterRealm request) {
        Token token = request.getRealm().getAuthentication().getToken();
        return tokenFactory.verifyToken(token) && (token.getDomain().equals(request.getRealm().getName()));
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
        RealmSettings realm = realms.get(connection);
        realms.remove(connection);
        logger.onRealmDeregistered(realm);
    }

    public ArrayList<RealmSettings> getList() {
        ArrayList<RealmSettings> list = new ArrayList<>();

        for (RealmSettings realm : realms.values()) {
            list.add(realm);
        }

        return list;
    }
}
