package Patching;

import Configuration.Strings;
import Patching.Configuration.PatchServerSettings;
import Patching.Controller.ClientHandler;
import Patching.Controller.ClientRequest;
import Patching.Configuration.PatchProvider;
import Protocols.AuthorizationHandler;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.PacketHandler;
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
    private static PatchServerSettings settings;
    private static Protocol<PacketHandler<ClientRequest>> protocol;
    private static Vertx vertx;

    @BeforeClass
    public static void startUp() {
        vertx = Vertx.vertx();
        PatchProvider provider = new PatchProvider(vertx);
        protocol = provider.protocol();
        settings = provider.getSettings();
        new ClientHandler(provider);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetPatchInfo(TestContext context) throws Exception {
        handle(Strings.PATCH_IDENTIFIER, new ClientRequestMock((response, status) -> {

            context.assertTrue(response.containsKey("name"));
            context.assertTrue(response.containsKey("version"));
            context.assertTrue(response.containsKey("date"));
            context.assertTrue(response.containsKey("changes"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);

        }, new JsonObject()));
    }

    private void handle(String action, ClientRequest request) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.get(action, AuthorizationHandler.Access.PUBLIC).handle(request);
    }

    @Test
    public void testGetPatchData(TestContext context) throws Exception {
        handle(Strings.PATCH_DATA, new ClientRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("files"));
        }, null));
    }

    private JsonObject getDownloadFile(String filename) {
        return new JsonObject()
                .put("file", filename);
    }

    @Test
    public void testGetGameInfo(TestContext context) throws Exception {
        handle(Strings.PATCH_GAME_INFO, new ClientRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("content"));
            context.assertTrue(response.containsKey("title"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()));
    }

    @Test
    public void testGetGameNews(TestContext context) throws Exception {
        handle(Strings.PATCH_NEWS, new ClientRequestMock((response, status) -> {
            JsonArray list = response.getJsonArray("list");

            context.assertTrue(list.size() == 1);
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()));
    }

    @Test
    public void testGetAuthServer(TestContext context) throws Exception {
        handle(Strings.PATCH_AUTHSERVER, new ClientRequestMock((response, status) -> {

            context.assertTrue(response.containsKey("remote"));
            context.assertTrue(response.containsKey("port"));

            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()));
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
