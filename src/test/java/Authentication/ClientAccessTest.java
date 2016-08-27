package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.Transport.ClientServer;
import Authentication.Controller.ClientHandler;
import Authentication.Model.*;
import Configuration.ConfigMock;
import Configuration.Strings;
import Shared.ResponseListener;
import Protocols.Serializer;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Shared.ResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ClientAccessTest {
    private static Vertx vertx;
    private static ClientServer server;

    @BeforeClass
    public static void setUp() throws IOException {
        vertx = Vertx.vertx();
        RealmStore realms = new RealmStore(vertx);
        realms.put(new ConfigMock.RealmSettingsMock());
        AuthProvider provider = new ProviderMock(vertx);
        new ClientHandler(provider);
        server = new ClientServer(provider);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_REALM_TOKEN, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken()));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        server.handle(action, new ClientRequestMock(data, listener));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), "username"));
    }

    @Test
    public void failListCharactersOnRealmWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken())));
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken()));
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken()));
    }

}
