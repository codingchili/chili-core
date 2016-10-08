package com.codingchili.core.Realm;

import com.codingchili.core.Configuration.ConfigMock;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Controller.RealmHandler;
import com.codingchili.core.Realm.Controller.RealmRequestMock;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
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

import static com.codingchili.core.Configuration.Strings.REALM_PING;

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
        RealmServerSettings serverSettings = new ConfigMock.RealmServerSettingsMock();
        RealmSettings realmSettings = new ConfigMock.RealmSettingsMock();
        handler = new RealmHandler(new RealmProvider(vertx, serverSettings, realmSettings));
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void realmPingTest(TestContext context) {
        handle(REALM_PING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handler.handle(new RealmRequestMock(action, listener, null));
    }
}
