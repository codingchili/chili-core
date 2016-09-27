package Authentication.Realm;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Controller.RealmHandler;
import Authentication.ProviderMock;
import Configuration.ConfigMock;
import Protocols.*;
import Realm.Configuration.RealmSettings;
import Protocols.Realm.CharacterRequest;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Shared.ResponseListener;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */


@Ignore
@RunWith(VertxUnitRunner.class)
public class RealmAuthenticationHandlerTest {
    private AuthServerSettings authconfig = new ConfigMock().getAuthSettings();
    private RealmSettings realmconfig = new ConfigMock().getRealm();
    private RealmHandler handler;
    private TokenFactory factory;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        AuthProvider provider = new ProviderMock();
        handler = new RealmHandler(provider);
        RealmSettings realm = new ConfigMock.RealmSettingsMock();
        provider.getRealmStore().put(Future.future(), realm);
        factory = new TokenFactory("null".getBytes());
    }

    @Test
    public void shouldRegisterWithRealm(TestContext context) {
        Async async = context.async();
        Future<WebSocket> future = Future.future();

        future.setHandler(complete -> {
            if (future.failed())
                context.fail();

            async.complete();
        });
        registerRealm(future, realmconfig);
    }

    @Test
    public void shouldFailToRegisterWithRealm(TestContext context) {
        Async async = context.async();
        Future<WebSocket> future = Future.future();
        future.setHandler(complete -> {

            if (future.succeeded())
                context.fail();

            async.complete();
        });

        realmconfig.getAuthentication().setToken(new Token(factory, "null"));
        registerRealm(future, realmconfig);
    }

    private void registerRealm(Future<WebSocket> future, RealmSettings settings) {
        /*vertx.createHttpClient().websocket(authconfig.getRealmPort(), "localhost", "/", socket -> {

            socket.handler(message -> {
                RealmRegister response = (RealmRegister) Serializer.unpack(message.toString(), RealmRegister.class);

                if (response.getRegistered()) {
                    future.complete(socket);
                } else
                    future.fail("Failed to register.");
            });

            socket.write(Buffer.buffer(Serializer.pack(new RealmRegister(settings))));
        });*/
    }

    @Ignore
    public void shouldQueryForCharacter(TestContext context) {
        Async async = context.async();
        Future<JsonObject> future = Future.future();

        future.setHandler(json -> {

            async.complete();
        });

        send(new CharacterRequest(), future);
    }

    private void send(Object object, Future<JsonObject> future) {
        Future<WebSocket> register = Future.future();

        register.setHandler(connection -> {
            if (connection.succeeded()) {
                WebSocket websocket = connection.result();

                websocket.handler(event -> {
                    future.complete(event.toJsonObject());
                });

                websocket.write(Buffer.buffer(Serializer.pack(object)));
            } else {
                future.fail("Failed to register.");
            }
        });
        registerRealm(register, realmconfig);
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        try {
            handler.handle(new RealmAuthenticationRequestMock(data, listener, action));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
