package com.codingchili.core.Patching;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Patching.Controller.PatchHandler;
import com.codingchili.core.Patching.Controller.PatchRequest;
import com.codingchili.core.Patching.Configuration.PatchProvider;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the patching server as a client.
 */

@RunWith(VertxUnitRunner.class)
public class PatchTest {
    private static PatchHandler handler;
    private static Vertx vertx;

    @BeforeClass
    public static void startUp() {
        vertx = Vertx.vertx();
        PatchProvider provider = new PatchProvider(vertx);
        handler = new PatchHandler(provider);
        new PatchHandler(provider);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetPatchInfo(TestContext context) throws Exception {
        Async async = context.async();

        handle(new PatchRequestMock((response, status) -> {

            context.assertTrue(response.containsKey("name"));
            context.assertTrue(response.containsKey("version"));
            context.assertTrue(response.containsKey("date"));
            context.assertTrue(response.containsKey("changes"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();

        }, new JsonObject(), Strings.PATCH_IDENTIFIER));
    }

    private void handle(PatchRequest request) throws Exception {
        handler.handle(request);
    }

    @Test
    public void testGetPatchData(TestContext context) throws Exception {
        Async async = context.async();

        handle(new PatchRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("files"));
            async.complete();
        }, null, Strings.PATCH_DATA));
    }

    private JsonObject getDownloadFile(String filename) {
        return new JsonObject()
                .put("file", filename);
    }

    @Test
    public void testGetGameInfo(TestContext context) throws Exception {
        Async async = context.async();

        handle(new PatchRequestMock((response, status) -> {
            context.assertTrue(response.containsKey("content"));
            context.assertTrue(response.containsKey("title"));
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject(), Strings.PATCH_GAME_INFO));
    }

    @Test
    public void testGetGameNews(TestContext context) throws Exception {
        Async async = context.async();

        handle(new PatchRequestMock((response, status) -> {
            JsonArray list = response.getJsonArray("list");

            context.assertTrue(list.size() == 1);
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject(), Strings.PATCH_NEWS));
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
