package Game.Controller;

import Configuration.GameServerSettings;
import Configuration.InstanceSettings;
import Configuration.RealmSettings;
import Game.Model.Connection;
import Protocol.Game.CharacterRequest;
import Protocol.Game.CharacterResponse;
import Protocol.Packet;
import Protocol.RegisterRealm;
import Utilities.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * handles players on a realm.
 */
public class Realm implements Verticle {
    private static final int REALM_UPDATE = 6000;
    private static final String AUTH_ADDRESS = "authentication.server";
    private HashMap<String, Connection> connections = new HashMap<>();
    private HashMap<String, ClientPacketHandler> clientHandlers = new HashMap<>();
    private HashMap<String, AuthPacketHandler> authHandlers = new HashMap<>();
    private WebSocket authserver;
    private RealmSettings settings;
    private GameServerSettings game;
    private TokenFactory tokenFactory;
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
        this.tokenFactory = new TokenFactory(settings.getAuthentication().getToken().getKey().getBytes());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        startInstances();
        startServer(createRouter());
        setupAuthenticationServerHandlers();
        connectToAuthenticationServer();

        start.complete();
    }

    private void startInstances() throws IOException {

        for (InstanceSettings instance : settings.getInstance()) {
            vertx.deployVerticle(new Instance(game, settings, instance));
        }
    }


    private Router createRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route("/*").handler(context -> {
            allowCors(context);
            context.response().setStatusCode(HttpResponseStatus.OK.code()).end();
        });

        return router;
    }

    private HttpServerResponse allowCors(RoutingContext context) {
        return context.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "POST, GET")
                .putHeader("Access-Control-Allow-Headers",
                        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    }

    private void startServer(Router router) {
        vertx.createHttpServer(new HttpServerOptions()
                .setCompressionSupported(true))
                .requestHandler(router::accept).websocketHandler(socket -> {

            Connection connection = new Connection(vertx, socket);

            socket.handler(event -> {
                Packet packet = (Packet) Serializer.unpack(event.toString(), Packet.class);

                if (packet.getAction().equals(CharacterRequest.ACTION))
                    handleLogin(connection, event.toString());
                else if (connection.isAuthenticated()) {
                    clientHandlers.get(packet.getAction()).handle(connection, event.toString());
                }
            });

            socket.endHandler(event -> {
                connections.remove(socket.textHandlerID());
            });

        }).listen(settings.getBinding().getPort());
    }


    private void handleLogin(Connection connection, String data) {
        CharacterRequest login = (CharacterRequest) Serializer.unpack(data, CharacterRequest.class);
        login.setConnection(connection.getAddress());

        if (tokenFactory.verifyToken(login.getToken())) {
            connection.setAuthenticated(true);
            connection.sendAuthenticationSuccess();
            connections.put(connection.getAddress(), connection);

            sendAuthServer(login);
        } else {
            connection.sendAuthenticationError();
        }
    }

    private void sendAuthServer(Object object) {
        authserver.write(Buffer.buffer(Serializer.pack(object)));
    }


    /**
     * Register the realm with the authentication server to mark that it is ready to receive clients.
     * The registration event will then periodically trigger to update its state.
     */
    private void connectToAuthenticationServer() {
        RemoteAuthentication authentication = settings.getAuthentication();
        logger.onRealmStarted(settings);

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), "", socket -> {
            authserver = socket;

            socket.handler(message -> {
                Packet packet = (Packet) Serializer.unpack(message.toString(), Packet.class);
                authHandlers.get(packet.getAction()).handle(socket.textHandlerID(), message.toString());
            });

            registerRealm();
        });
    }

    private void setupAuthenticationServerHandlers() {
        authHandlers.put(CharacterResponse.ACTION, (connection, data) -> {
            CharacterResponse response = (CharacterResponse) Serializer.unpack(data, CharacterResponse.class);
            connections.get(response.getConnection()).sendCharacterResponse(response);
        });
    }

    private void registerRealm() {
        sendAuthServer(new RegisterRealm(settings));

        vertx.setPeriodic(REALM_UPDATE, event -> {
            sendAuthServer(new RegisterRealm(settings.setPlayers(connections.size())));
        });
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
