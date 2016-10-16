package com.codingchili.core.Realm;

import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Protocols.ResponseStatus;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Controller.RealmHandler;
import com.codingchili.core.Shared.RequestMock;
import com.codingchili.core.Shared.ResponseListener;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.ID_PING;

/**
 * @author Robin Duda
 *         tests the API from client->realmName.
 */

@RunWith(VertxUnitRunner.class)
public class RealmHandlerTest {
    private RealmHandler handler;
    private Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        RealmServerSettings serverSettings = new RealmServerSettings();
        RealmSettings realmSettings = new RealmSettings()
                .setAuthentication(new RemoteAuthentication()
                .setToken(new Token(new TokenFactory("s".getBytes()), "realmName")));

        handler = new RealmHandler(new RealmProvider(vertx, serverSettings, realmSettings));
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void realmPingTest(TestContext context) {
        handle(ID_PING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handler.process(RequestMock.get(action, listener, null));
    }
}
