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
import Shared.ResponseStatus;
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

import static Configuration.Strings.REALM_REGISTER;

/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */

@Ignore
@RunWith(VertxUnitRunner.class)
public class RealmHandlerTest {
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
    public void registerWithRealmTest(TestContext context) {
        handle(REALM_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, getToken());
    }

    private JsonObject getToken() {
        return Serializer.json(new Token(factory, realmconfig.getName()));
    }

    @Test
    public void realmUpdateTest(TestContext context) {

    }

    @Test
    public void failToRegisterWithInvalidToken(TestContext context) {

    }


    @Test
    public void queryAccountCharacterTest(TestContext context) {

    }

    @Test
    public void queryAccountMissingCharacterTest(TestContext context) {

    }

    @Test
    public void queryMissingAccountCharactertest(TestContext context) {

    }


    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        try {
            handler.handle(new RealmRequestMock(data, listener, action));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
