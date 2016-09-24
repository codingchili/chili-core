package Patching;

import Configuration.Strings;
import Patching.Controller.ClientPatchHandler;
import Patching.Controller.ClientRequest;
import Patching.Configuration.PatchProvider;
import Protocols.Access;
import Protocols.AuthorizationHandler;
import Protocols.Protocol;
import Shared.ResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the patching server as a client.
 */

// todo complete tests in class.

@RunWith(VertxUnitRunner.class)
public class PatchTest {
    private static ClientPatchHandler handler;
    private static Vertx vertx;

    @BeforeClass
    public static void startUp() {
        vertx = Vertx.vertx();
        PatchProvider provider = new PatchProvider(vertx);
        handler = new ClientPatchHandler(provider);
        new ClientPatchHandler(provider);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetPatchInfo(TestContext context) throws Exception {
        handle(new ClientRequestMock((response, status) -> {

            context.assertTrue(response.containsKey("name"));
            context.assertTrue(response.containsKey("version"));
            context.assertTrue(response.containsKey("date"));
            context.assertTrue(response.containsKey("changes"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);

        }, new JsonObject(), Strings.PATCH_IDENTIFIER));
    }

    private void handle(ClientRequest request) throws Exception {
        handler.process(request);
    }

    @Test
    public void testGetPatchData(TestContext context) throws Exception {
        handle(new ClientRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("files"));
        }, null, Strings.PATCH_DATA));
    }

    private JsonObject getDownloadFile(String filename) {
        return new JsonObject()
                .put("file", filename);
    }

    @Test
    public void testGetGameInfo(TestContext context) throws Exception {
        handle(new ClientRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("content"));
            context.assertTrue(response.containsKey("title"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject(), Strings.PATCH_GAME_INFO));
    }

    @Test
    public void testGetGameNews(TestContext context) throws Exception {
        handle(new ClientRequestMock((response, status) -> {
            JsonArray list = response.getJsonArray("list");

            context.assertTrue(list.size() == 1);
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject(), Strings.PATCH_NEWS));
    }

    @Test
    public void testGetAuthServer(TestContext context) throws Exception {
        handle(new ClientRequestMock((response, status) -> {

            context.assertTrue(response.containsKey("remote"));
            context.assertTrue(response.containsKey("port"));

            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject(), Strings.PATCH_AUTHSERVER));
    }

    @Ignore
    @Test
    public void testUpdatePatchVersion() {

    }

    @Ignore
    @Test
    public void testDownloadPatchReloadedException() {

    }

    @Ignore
    @Test
    public void testDownloadPatchFiles() {

    }
}
