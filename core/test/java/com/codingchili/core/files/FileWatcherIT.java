package com.codingchili.core.files;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    // a watcher of an earlier test would still be notified of the files of the next one.
    private final List<FileWatcher> watchers = new ArrayList<>();

    @Before
    public void setUp() {
        this.context = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        watchers.forEach(FileWatcher::stop);
        context.close().onComplete(test.asyncAssertSuccess());
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
    public void burstOfModificationsIsReportedOnce(TestContext test) {
        Async async = test.async();
        AtomicInteger notifications = new AtomicInteger();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                notifications.incrementAndGet();
            }
        });

        for (int i = 0; i < 5; i++) {
            ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);
        }

        context.timer(400, handler -> {
            test.assertEquals(1, notifications.get());
            async.complete();
        });
    }

    @Test
    public void notNotifiedAfterStopped(TestContext test) {
        Async async = test.async();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                test.fail("Reacted to file modify event after the watcher was stopped.");
            }
        }).stop();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        delayMS(async, 400);
    }

    @Test
    public void everyWatcherOfADirectoryIsNotified(TestContext test) {
        Async first = test.async();
        Async second = test.async();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                first.complete();
            }
        });
        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                second.complete();
            }
        });

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);
    }

    @Test
    public void stoppedWatcherDoesNotStopTheOthers(TestContext test) {
        Async async = test.async();

        ConfigurationFactory.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                async.complete();
            }
        });
        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                test.fail("Reacted to file modify event after the watcher was stopped.");
            }
        }).stop();

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

    private FileWatcher listenFiles(FileStoreListener listener) {
        var watcher = new FileWatcherBuilder(context)
                .onDirectory(CoreStrings.testDirectory(FILE_WATCHER_TEST))
                .rate(TimerSource.of(25))
                .withListener(listener)
                .build();
        watchers.add(watcher);
        return watcher;
    }
}
