package com.codingchili.patching;

import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.*;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.*;
import com.codingchili.core.testing.*;
import com.codingchili.patching.configuration.*;
import com.codingchili.patching.controller.*;

import io.vertx.core.*;
import io.vertx.core.file.*;
import io.vertx.core.json.*;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.*;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 *         <p>
 *         Tests the patching server as a client.
 */

@RunWith(VertxUnitRunner.class)
public class PatchHandlerTest {
    private static final String TEST_FILE = "file.html";
    private static final String MAX_VERSION = "99999999";
    private static final String MISSING_FILE = "missing-file";
    private static final String MIN_VERSION = "-1";
    private static final String BYTE_PREFIX_HEADER = "bytes=";
    private static final String RANGE_DELIMETER = "-";
    private static PatchHandler handler;
    private static Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @BeforeClass
    public static void startUp() {
        vertx = Vertx.vertx();

        PatchContext context = new PatchContext(new SystemContext(vertx)) {
            @Override
            public String directory() {
                return testDirectory();
            }

            @Override
            public FileSystem fileSystem() {
                return new FileSystemMock(vertx);
            }
        };

        handler = new PatchHandler(context);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetPatchInfo(TestContext context) {
        Async async = context.async();

        handle(PATCH_IDENTIFIER, (response, status) -> {
            context.assertTrue(response.containsKey(ID_NAME));
            context.assertTrue(response.containsKey(ID_VERSION));
            context.assertTrue(response.containsKey(ID_DATE));
            context.assertTrue(response.containsKey(ID_CHANGES));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();

        }, new JsonObject());
    }

    @Test
    public void testGetPatchData(TestContext context) {
        Async async = context.async();

        handle(PATCH_DATA, (response, status) -> {
            context.assertTrue(response.containsKey(ID_FILES));
            context.assertTrue(response.containsKey(ID_NAME));
            context.assertTrue(response.containsKey(ID_VERSION));
            async.complete();
        }, null);
    }

    @Test
    public void testGetGameInfo(TestContext context) {
        Async async = context.async();

        handle(PATCH_GAME_INFO, (response, status) -> {
            context.assertTrue(response.containsKey(ID_CONTENT));
            context.assertTrue(response.containsKey(ID_TITLE));
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject());
    }

    @Test
    public void testGetGameNews(TestContext context) {
        Async async = context.async();

        handle(PATCH_NEWS, (response, status) -> {
            JsonArray list = response.getJsonArray(ID_LIST);

            context.assertTrue(list.size() == 1);
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            async.complete();
        }, new JsonObject());
    }

    @Test
    public void testDownloadPatchFiles(TestContext context) {
        Async async = context.async();

        handle(PATCH_DOWNLOAD, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertNotNull(response.getBinary(ID_BYTES));
            context.assertNotNull(response.getLong(ID_MODIFIED));
            context.assertNotNull(response.getLong(ID_SIZE));
            context.assertEquals(response.getString(ID_PATH), TEST_FILE);

            async.complete();
        }, getDownloadFile(TEST_FILE).put(ID_VERSION, MAX_VERSION));
    }

    @Test
    public void testWebseedFile(TestContext context) {
        Async async = context.async();

        handle(PATCH_WEBSEED, (response, status) -> {
            context.assertEquals("/**", response.getString(ID_BUFFER));
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, getWebseedFile(TEST_FILE, 0, 2));
    }

    private JsonObject getWebseedFile(String fileName, int start, int end) {
        return new JsonObject()
                .put(ID_FILE, fileName)
                .put(ID_RANGE, BYTE_PREFIX_HEADER + start + RANGE_DELIMETER + end);
    }

    @Test
    public void testDownloadPatchFileOutdatedVersion(TestContext context) {
        Async async = context.async();

        handle(PATCH_DOWNLOAD, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);

            async.complete();
        }, getDownloadFile(TEST_FILE).put(ID_VERSION, MIN_VERSION));
    }

    @Test
    public void testDownloadMissingFile(TestContext context) {
        Async async = context.async();

        handle(PATCH_DOWNLOAD, (response, status) -> {

            context.assertEquals("Could not find file missing-file", response.getString(PROTOCOL_MESSAGE));
            context.assertEquals(ResponseStatus.MISSING, status);

            async.complete();
        }, getDownloadFile(MISSING_FILE).put(ID_VERSION, MAX_VERSION));
    }

    private JsonObject getDownloadFile(String filename) {
        return new JsonObject()
                .put(ID_FILE, filename);
    }

    @Test
    public void testPingPatchHandler(TestContext context) {
        handle(ID_PING, (response, status) -> {
            context.assertEquals(status, ResponseStatus.ACCEPTED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject());
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }
}
