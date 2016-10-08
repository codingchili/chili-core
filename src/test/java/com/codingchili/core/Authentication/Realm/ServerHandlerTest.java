package com.codingchili.core.Authentication.Realm;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Controller.AuthenticationHandler;
import com.codingchili.core.Authentication.Model.ProviderMock;
import com.codingchili.core.Configuration.ConfigMock;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ServerHandlerTest {
    private RealmSettings realmconfig = new ConfigMock().getRealm();
    private AuthenticationHandler handler;
    private TokenFactory factory;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        AuthProvider provider = new ProviderMock();
        handler = new AuthenticationHandler(provider);
        factory = new TokenFactory("null".getBytes());
    }

    @Ignore
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

    @Test
    public void failUpdateWhenInvalidToken(TestContext context) {
        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCloseWhenInvalidToken(TestContext context) {
        handle(CLIENT_CLOSE, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCharacterRequestInvalidToken(TestContext context) {
        handle(REALM_CHARACTER_REQUEST, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        try {
            handler.handle(new AuthenticationRequestMock(action, listener, data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
