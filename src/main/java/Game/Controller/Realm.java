package Game.Controller;

import Configuration.Gameserver.GameServerSettings;
import Configuration.Gameserver.InstanceSettings;
import Configuration.Gameserver.RealmSettings;
import Configuration.RemoteAuthentication;
import Game.Model.Connection;
import Protocols.Game.CharacterRequest;
import Protocols.Game.CharacterResponse;
import Protocols.Packet;
import Protocols.Authentication.RealmRegister;
import Protocols.Authentication.RealmUpdate;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.Serializer;
import Protocols.Authorization.TokenFactory;
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
 * @author Robin Duda
 *         Handles client connections on a higher level than instances.
 *         Authentication, Map travel, Trading, Private chat etc.
 */
public class Realm implements Verticle {
    private static final int REALM_UPDATE = 6000;
    private HashMap<String, Connection> connections = new HashMap<>();
    private HashMap<String, ClientPacketHandler> clientHandlers = new HashMap<>();
    private HashMap<String, AuthPacketHandler> authHandlers = new HashMap<>();
    private Boolean registered = false;
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

    public Realm(GameServerSettings game, RealmSettings settings, Logger logger) {
        this(game, settings);
        this.logger = logger;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (logger == null)
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

            Connection connection = new Connection(socket);

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

        }).listen(settings.getPort());
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
     * Register the realmName with the authentication server to mark that it is ready to receive clients.
     * The registration event will then periodically trigger to update its state.
     */
    private void connectToAuthenticationServer() {
        RemoteAuthentication authentication = settings.getAuthentication();
        logger.onRealmStarted(settings);

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), "", socket -> {
            authserver = socket;

            socket.handler(message -> {
                Packet packet = (Packet) Serializer.unpack(message.toString(), Packet.class);

                if (authHandlers.containsKey(packet.getAction()))
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

        authHandlers.put(RealmRegister.ACTION, (connection, data) -> {
            RealmRegister response = (RealmRegister) Serializer.unpack(data, RealmRegister.class);

            if (response.getRegistered()) {
                if (!registered) {
                    logger.onRealmRegistered(settings);
                } else {
                    logger.onRealmUpdated(settings);
                }
                registered = true;
            } else {
                logger.onRealmRejected(settings);
            }
        });
    }

    private void registerRealm() {
        sendAuthServer(new RealmRegister(settings));

        vertx.setPeriodic(REALM_UPDATE, event -> {
            sendAuthServer(new RealmUpdate(connections.size()));
        });
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
