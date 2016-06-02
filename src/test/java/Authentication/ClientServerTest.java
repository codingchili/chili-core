package Authentication;

import Authentication.Controller.AuthProvider;
import Authentication.Controller.ClientRequest;
import Authentication.Controller.Transport.ClientServer;
import Authentication.ClientRequestMock.ResponseStatus;
import Authentication.Controller.ClientHandler;
import Configuration.Provider;
import Protocols.Protocol;
import Authentication.Model.*;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ClientServerTest {
    private static Vertx vertx;
    private static ClientServer server;

    @Before
    public void setUp() throws IOException {
        vertx = Vertx.vertx();
        RealmStore realms = new RealmStore(vertx);
        realms.put(new ConfigMock.RealmSettingsMock());
        AuthProvider provider = new ProviderMock(vertx);
        new ClientHandler(provider);
        server = new ClientServer(provider);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext context) {
        handle(ClientRequest.REALMTOKEN, (response, status) -> {
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
        handle(ClientRequest.CHARACTERLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken())));
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext context) {
        handle(ClientRequest.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken()));
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext context) {
        handle(ClientRequest.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken()));
    }

}
