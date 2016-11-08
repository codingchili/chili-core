package com.codingchili.services.Website;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocol.ResponseStatus;
import com.codingchili.core.Shared.RequestMock;
import com.codingchili.core.Shared.ResponseListener;

import com.codingchili.services.Website.Controller.WebHandler;

import static com.codingchili.services.Shared.Strings.ID_BUFFER;

/**
 * @author Robin Duda
 *         tests the website/resource server.
 */
@RunWith(VertxUnitRunner.class)
public class WebsiteTest {
    private static final String ONE_MISSING_FILE = "/one-missing-file";
    private WebHandler handler;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        handler = new WebHandler<>(new ContextMock(Vertx.vertx()));
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
            context.assertEquals(bower.getString(Strings.ID_LICENSE), "MIT");
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
