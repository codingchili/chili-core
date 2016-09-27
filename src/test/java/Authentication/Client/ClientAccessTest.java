package Authentication.Client;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientHandler;
import Authentication.ProviderMock;
import Configuration.ConfigMock;
import Configuration.Strings;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.Serializer;
import Shared.ResponseListener;
import Shared.ResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
public class ClientAccessTest {
    private static ClientHandler handler;

    @Before
    public void setUp() throws IOException {
        AuthProvider provider = new ProviderMock();
        provider.getRealmStore().put(Future.future(), new ConfigMock.RealmSettingsMock());
        handler = new ClientHandler(provider);
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


    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(new ClientAuthenticationRequestMock(data, listener, action));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), "username"));
    }
}
