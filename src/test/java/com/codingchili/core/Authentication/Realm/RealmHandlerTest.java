package com.codingchili.core.Authentication.Realm;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Controller.RealmHandler;
import com.codingchili.core.Authentication.Model.ProviderMock;
import com.codingchili.core.Configuration.ConfigMock;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.REALM_REGISTER;

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
