import Configuration.FileConfiguration;
import Configuration.RealmSettings;
import Game.Controller.Realm;
import Authentication.LoggerMock;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author Robin Duda
 *         tests the services provided by the game server.
 */
@RunWith(VertxUnitRunner.class)
public class RealmPingTest {
    private Vertx vertx;
    private static final int TEST_PORT = 11132;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        RealmSettings realm = new RealmSettings();
        realm.getBinding().setPort(TEST_PORT);
        realm.getAuthentication().getToken().setKey("null");
        realm.getAuthentication().setRemote("localhost");

        vertx.deployVerticle(new Realm(FileConfiguration.instance().getGameServerSettings(), realm, new LoggerMock()), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testPingRealm(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().getNow(TEST_PORT, "localhost", "/", response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
            async.complete();
        });
    }
}
