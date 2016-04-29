package Game;

import Protocol.RegisterRealm;
import Game.Model.RealmSettings;
import Utilities.Config;
import Game.Model.InstanceSettings;
import Utilities.*;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * handles players on a realm.
 */
public class Realm implements Verticle {
    private static final int REALM_UPDATE = 15000;
    private HashMap<String, ServerWebSocket> connections = new HashMap<>();
    private RemoteAuthentication authserver;
    private RealmSettings realm;
    private Logger logger;
    private Vertx vertx;

    public Realm(RealmSettings realm, RemoteAuthentication authserver) {
        this.realm = realm;
        this.authserver = authserver;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, Config.Gameserver.LOGTOKEN);
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        startInstances();
        startRealm();
        authenticateRealm();

        logger.onRealmStarted(realm);
        start.complete();
    }

    /**
     * Register the realm with the authentication server to mark that it is ready to receive clients.
     * The registration event will then periodically trigger to update its state.
     */
    private void authenticateRealm() {
        vertx.createHttpClient().websocket(authserver.getPort(), authserver.getRemote(), "", handler -> {
            registerRealm(handler);

            vertx.setPeriodic(REALM_UPDATE, event -> {
                registerRealm(handler);
            });

        });
    }

    private void registerRealm(WebSocket handler) {
        RegisterRealm request = new RegisterRealm(realm, authserver.getToken());
        handler.write(Buffer.buffer(Serializer.pack(request)));
    }

    private void startInstances() throws IOException {
        ArrayList<JsonObject> instances = JsonFileReader.readDirectoryObjects("conf/game/world/");

        for (JsonObject instance : instances) {
            InstanceSettings configuration = (InstanceSettings) Serializer.unpack(instance, InstanceSettings.class);
            vertx.deployVerticle(new Game.Instance(configuration, realm.getName()));
        }
    }

    private void startRealm() {
        vertx.createHttpServer().websocketHandler(connection -> {

            connection.handler(event -> {
                // on authenticated
                connections.put("accname", connection);

                // todo connection object requires webserversocket, account data, character data, authenticated status, instance

                // todo step 1 authenticate user
                // todo step 2 select character
                // todo step 3 place character into instance
                // todo step 4 relay messages into instance
                // todo step 5 handle the movement of characters between instances. (shared map? / bus?)
                // todo step 6 handle realm-level events, PMs, world chat
            });

            connection.endHandler(event -> {
                // todo remove the user
                connections.remove("accname");
            });

        }).listen(realm.getPort());
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
