package com.codingchili.website;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.common.Strings;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import com.codingchili.website.controller.WebHandler;

import static com.codingchili.common.Strings.ID_BUFFER;

/**
 * @author Robin Duda
 *         tests the website/resource server.
 */
@RunWith(VertxUnitRunner.class)
public class WebsiteTest {
    private static final String ONE_MISSING_FILE = "/one-missing-file";
    private WebHandler handler;
    private ContextMock context;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
        handler = new WebHandler<>(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void getAFile(TestContext context) {
        Async async = context.async();

        handle("/bower.json", (response, status) -> {
            JsonObject bower = new JsonObject(response.getString(ID_BUFFER));

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(bower.getString(Strings.ID_LICENSE), "MIT");

            async.complete();
        });
    }

    @Test
    public void getIndexFile(TestContext context) {
        Async async = context.async();

        handle("/", (response, status) -> {
            String buffer = response.getString(ID_BUFFER);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(buffer.startsWith("<"));
            context.assertTrue(buffer.endsWith(">"));

            async.complete();
        });
    }

    @Test
    public void get404File(TestContext context) {
        Async async = context.async();

        handle(ONE_MISSING_FILE, (response, status) -> {
            JsonObject bower = new JsonObject(response.getString(ID_BUFFER));
            context.assertEquals(bower.getString(Strings.ID_LICENSE), "404");
            async.complete();
        });
    }

    private void handle(String action, ResponseListener listener) {
        try {
            handler.handle(RequestMock.get(action, listener, null));
        } catch (AuthorizationRequiredException e) {
            throw new RuntimeException(e);
        }
    }
}