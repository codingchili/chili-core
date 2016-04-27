package Authentication.Controller;

import Authentication.Protocol.RegisterRealm;
import Configuration.Config;
import Game.Model.RealmSettings;
import Protocol.Packet;
import Utilities.Logger;
import Utilities.Serializer;
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
    private TokenFactory tokenFactory;
    private Vertx vertx;
    private Logger logger;

    public RealmHandler(Vertx vertx, Logger logger) {
        this.tokenFactory = new TokenFactory(Config.Authentication.SERVER_SECRET);
        this.vertx = vertx;
        this.logger = logger;

        registerHandlers();
        startServer();
    }

    private void registerHandlers() {
        handlers.put(RegisterRealm.ACTION, (data, connection) -> {
            RegisterRealm request = (RegisterRealm) Serializer.unpack(data, RegisterRealm.class);

            if (tokenFactory.verifyToken(request.getToken())) {

                if (realms.containsKey(connection))
                    logger.onRealmUpdated(request.getRealm());
                else
                    logger.onRealmRegistered(request.getRealm());

                realms.put(connection, request.getRealm());
            }
        });
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

        }).listen(Config.Authentication.REALM_PORT);
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
