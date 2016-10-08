package com.codingchili.core.Patching;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Patching.Controller.PatchHandler;
import com.codingchili.core.Patching.Configuration.PatchProvider;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the patching server as a client.
 */

@RunWith(VertxUnitRunner.class)
public class PatchHandlerTest {
    private static PatchHandler handler;
    private static Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @BeforeClass
    public static void startUp() {
        vertx = Vertx.vertx();
        PatchProvider provider = new ProviderMock(vertx);
        handler = new PatchHandler(provider);
        new PatchHandler(provider);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetPatchInfo(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_IDENTIFIER, (response, status) -> {

            context.assertTrue(response.containsKey("name"));
            context.assertTrue(response.containsKey("version"));
            context.assertTrue(response.containsKey("date"));
            context.assertTrue(response.containsKey("changes"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();

        }, new JsonObject());
    }

    @Test
    public void testGetPatchData(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_DATA, (response, status) -> {
            context.assertTrue(response.containsKey("files"));
            async.complete();
        }, null);
    }

    @Test
    public void testGetGameInfo(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_GAME_INFO, (response, status) -> {
            context.assertTrue(response.containsKey("content"));
            context.assertTrue(response.containsKey("title"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject());
    }

    @Test
    public void testGetGameNews(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_NEWS, (response, status) -> {
            JsonArray list = response.getJsonArray("list");

            context.assertTrue(list.size() == 1);
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject());
    }

    @Test
    public void testDownloadPatchFiles(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_DOWNLOAD, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertNotNull(response.getBinary(ID_BYTES));
            context.assertNotNull(response.getLong(ID_MODIFIED));
            context.assertNotNull(response.getLong(ID_SIZE));
            context.assertEquals(response.getString(ID_PATH), "game.js");

            async.complete();
        }, getDownloadFile("game.js").put(ID_VERSION, "9999999"));
    }

    @Test
    public void testDownloadPatchFileOutdatedVersion(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_DOWNLOAD, (response, status) -> {

            context.assertEquals(ResponseStatus.CONFLICT, status);

            async.complete();
        }, getDownloadFile("game.js").put(ID_VERSION, "-1"));
    }

    @Test
    public void testDownloadMissingFile(TestContext context) {
        Async async = context.async();

        handle(Strings.PATCH_DOWNLOAD, (response, status) -> {

            context.assertEquals(ResponseStatus.MISSING, status);

            async.complete();
        }, getDownloadFile("missing-file").put(ID_VERSION, "99999999"));
    }

    private JsonObject getDownloadFile(String filename) {
        return new JsonObject()
                .put("file", filename);
    }



    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(new PatchRequestMock(action, listener, data));
    }
}
