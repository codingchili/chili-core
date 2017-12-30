package com.codingchili.patching;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.FileSystemMock;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.controller.PatchHandler;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * <p>
 * Tests the patching server as a client.
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
    private static CoreContext system;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @BeforeClass
    public static void startUp() {
        system = new SystemContext();

        PatchContext context = new PatchContext(system) {
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
        system.close(context.asyncAssertSuccess());
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
