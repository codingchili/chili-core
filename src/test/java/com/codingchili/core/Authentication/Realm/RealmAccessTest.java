package com.codingchili.core.Authentication.Realm;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Controller.RealmHandler;
import com.codingchili.core.Authentication.Model.ProviderMock;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class RealmAccessTest {
    private RealmHandler handler;

    @Rule
    public Timeout timeout = new Timeout(300, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        AuthProvider provider = new ProviderMock();
        handler = new RealmHandler(provider);
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
        handle(action, listener, new JsonObject());
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(new RealmRequestMock(data, listener, action));
    }
}
