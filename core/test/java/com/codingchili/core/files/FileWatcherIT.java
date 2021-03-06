package com.codingchili.core.files;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.testing.ContextMock;


/**
 * Tests that the FileWatcher emits modified/delete events when a file is
 * created/deleted/modified and that it ignores files outside the specified directory.
 */
@RunWith(VertxUnitRunner.class)
public class FileWatcherIT {
    private static final String FILE_WATCHER_TEST = "FileWatcher";
    private static final String TOUCH_JSON = CoreStrings.testFile(FILE_WATCHER_TEST, "touch.json");
    private static final String NOT_WATCHED_FILE = CoreStrings.testFile("", "touch.json");
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    private ContextMock context;

    @Before
    public void setUp() {
        this.context = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void getNotifiedOnModify(TestContext test) {
        Async async = test.async();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                async.complete();
            }
        });

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);
    }

    @Test
    public void getNotifiedOnDelete(TestContext test) {
        Async async = test.async();

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileRemove(Path path) {
                async.complete();
            }
        });

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        context.timer(400, event -> {
            test.assertTrue(ConfigurationFactory.delete(TOUCH_JSON));
        });
    }

    @Test
    public void getNotifiedOnCreate(TestContext test) {
        Async async = test.async();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                async.complete();
            }
        });

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);
    }

    @Test
    public void notNotifiedWhenOutsidePath(TestContext test) {
        Async async = test.async();

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                test.fail("Reacted to file modify event outside watched path.");
            }

            @Override
            public void onFileRemove(Path path) {
                test.fail("Reacted to file delete event outside the watched path.");
            }
        });

        ConfigurationFactory.writeObject(new JsonObject(), NOT_WATCHED_FILE);
        test.assertTrue(ConfigurationFactory.delete(NOT_WATCHED_FILE));

        delayMS(async, 400);
    }

    private void delayMS(Async async, int ms) {
        context.timer(ms, handler -> async.complete());
    }

    private void listenFiles(FileStoreListener listener) {
        new FileWatcherBuilder(context)
                .onDirectory(CoreStrings.testDirectory(FILE_WATCHER_TEST))
                .rate(TimerSource.of(25))
                .withListener(listener)
                .build();
    }
}
