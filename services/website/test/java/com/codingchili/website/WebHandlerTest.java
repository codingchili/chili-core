package com.codingchili.website;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.testing.FileSystemMock;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.configuration.WebserverSettings;
import com.codingchili.website.controller.WebHandler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.common.Strings.ID_BUFFER;

/**
 * @author Robin Duda
 * tests the website/resource server.
 */
@RunWith(VertxUnitRunner.class)
public class WebHandlerTest {
    private static final String ONE_MISSING_FILE = "one-missing-file";
    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
    private WebHandler handler;
    private WebserverContext context;

    @Before
    public void setUp() {
        context = new WebserverContext(new SystemContext(Vertx.vertx())) {
            @Override
            public FileSystem fileSystem() {
                return new FileSystemMock(vertx);
            }
        };
        Configurations.initialize(context);
        Configurations.put(new WebserverSettings()
                .setResources(CoreStrings.testDirectory())
                .setStartPage("index.html")
                .setMissingPage("404.html"));
        handler = new WebHandler(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void getAFile(TestContext test) {
        Async async = test.async();

        handle("bower.json", (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertEquals(response.getString(Strings.ID_LICENSE), "MIT");
            async.complete();
        });
    }

    @Test
    public void getIndexFile(TestContext test) {
        Async async = test.async();

        handle("", (response, status) -> {
            String buffer = response.getString(ID_BUFFER);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertTrue(buffer.startsWith("<"));
            test.assertTrue(buffer.endsWith(">"));

            async.complete();
        });
    }

    @Test
    public void get404File(TestContext test) {
        Async async = test.async();

        handle(ONE_MISSING_FILE, (response, status) -> {
            String buffer = response.getString(ID_BUFFER);
            test.assertNull(buffer);
            test.assertEquals(ResponseStatus.MISSING, status);
            async.complete();
        });
    }

    private void handle(String action, ResponseListener listener) {
        handler.handle(RequestMock.get(action, listener, null));
    }
}
