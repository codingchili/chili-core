package Game;

import Configuration.GameServerSettings;
import Configuration.InstanceSettings;
import Configuration.RealmSettings;
import Protocol.RegisterRealm;
import Utilities.DefaultLogger;
import Utilities.Logger;
import Utilities.RemoteAuthentication;
import Utilities.Serializer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * handles players on a realm.
 */
public class Realm implements Verticle {
    private static final int REALM_UPDATE = 15000;
    private HashMap<String, ServerWebSocket> connections = new HashMap<>();
    private RealmSettings settings;
    private GameServerSettings game;
    private Logger logger;
    private Vertx vertx;

    public Realm(GameServerSettings game, RealmSettings settings) {
        this.settings = settings;
        this.game = game;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, game.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        startInstances();
        startRealm();
        authenticateRealm();

        logger.onRealmStarted(settings);
        start.complete();
    }

    /**
     * Register the realm with the authentication server to mark that it is ready to receive clients.
     * The registration event will then periodically trigger to update its state.
     */
    private void authenticateRealm() {
        RemoteAuthentication authentication = settings.getAuthentication();

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), "", handler -> {
            registerRealm(handler);

            vertx.setPeriodic(REALM_UPDATE, event -> {
                registerRealm(handler);
            });

        });
    }

    private void registerRealm(WebSocket handler) {
        handler.write(Buffer.buffer(Serializer.pack(new RegisterRealm(settings))));
    }

    private void startInstances() throws IOException {

        for (InstanceSettings instance : settings.getInstance()) {
            vertx.deployVerticle(new Game.Instance(game, settings, instance));
        }
    }


    private void startRealm() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route("/*").handler(context -> {
            allowCors(context);

            vertx.setTimer(new Random().nextInt(1)+1, event -> {
                context.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            });
        });

        vertx.createHttpServer().requestHandler(router::accept).websocketHandler(connection -> {

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

        }).listen(settings.getPort());
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
