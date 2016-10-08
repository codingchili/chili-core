package com.codingchili.core.Website;

import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import com.codingchili.core.Website.Configuration.WebserverProvider;
import com.codingchili.core.Website.Controller.WebHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *         tests the website/resource server.
 */
@RunWith(VertxUnitRunner.class)
public class WebsiteTest {
    private WebHandler handler;
    private WebRequestMock request;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        handler = new WebHandler(new WebserverProvider(Vertx.vertx()));
    }

    @Test
    public void getAFile(TestContext context) {
        Async async = context.async();

        handle("/bower.json", ((response, status) -> {
            JsonObject bower = new JsonObject(request.getBuffer().toString());

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(bower.getString("license"), "MIT");

            async.complete();
        }));
    }

    @Test
    public void getIndexFile(TestContext context) {
        Async async = context.async();

        handle("/", ((response, status) -> {
            Buffer buffer = request.getBuffer();

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(buffer.toString().startsWith("<"));
            context.assertTrue(buffer.toString().endsWith(">"));

            async.complete();
        }));
    }

    @Test
    public void failGetWithTraversal(TestContext context) {
        Async async = context.async();

        handle("../.gitignore", ((response, status) -> {
            context.assertNull(response);
            context.assertEquals(ResponseStatus.MISSING, status);

            async.complete();
        }));
    }

    private void handle(String action, ResponseListener listener) {
        request = new WebRequestMock(action, listener, null);
        handler.handle(request);
    }
}
