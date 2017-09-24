package com.codingchili.core.files;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.FileSystemMock;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robin Duda
 * <p>
 * Tests the loading of files in CachedFileStore.
 */
@RunWith(VertxUnitRunner.class)
public class CachedFileStoreTest {
    private static final String TEST_NAME = "CachedFileStore";
    private static final String FILE = "test.txt";
    private static final String DIRECTORY = CoreStrings.testDirectory(TEST_NAME);
    private static final String FILE_ABS = CoreStrings.testFile(TEST_NAME, FILE);
    private static final String FILE_TRAVERSAL = "../TraversalTestFile.txt";
    private AtomicBoolean removeCalled = new AtomicBoolean(false);
    private AtomicBoolean modifyCalled = new AtomicBoolean(false);
    private Vertx vertx;

    @Before
    public void setUp() {
        Configurations.system().setCachedFilePoll(20);
        CachedFileStore.reset();
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void failGetWithTraversal(TestContext test) {
        try {
            getStore(DIRECTORY).getFile(FILE_TRAVERSAL);
            test.fail("Should not be able to access file.");
        } catch (FileMissingException ignored) {
        }
    }

    @Test
    public void succeedGetFile() throws FileMissingException, IOException {
        Buffer fromStore = getStore(DIRECTORY).getFile(FILE).getBuffer();
        Buffer fromDisk = getFromDisk();

        Assert.assertEquals(fromDisk.length(), fromStore.length());

        for (int i = 0; i < fromDisk.length(); i++) {
            Assert.assertEquals(fromDisk.getByte(i), fromStore.getByte(i));
        }
    }

    private Buffer getFromDisk() throws IOException {
        Path path = Paths.get(FILE_ABS);
        return Buffer.buffer(Files.readAllBytes(path));
    }

    @Test
    public void succeedGetFileWithGzip() throws FileMissingException, IOException {
        Buffer fromStore = getStore(DIRECTORY, true).getFile(FILE).getBuffer();
        Buffer fromDisk = getFromDisk();

        fromStore = Buffer.buffer(Serializer.ungzip(fromStore.getBytes()));

        Assert.assertEquals(fromDisk.length(), fromStore.length());

        for (int i = 0; i < fromDisk.length(); i++) {
            Assert.assertEquals(fromDisk.getByte(i), fromStore.getByte(i));
        }
    }

    private CachedFileStore getStore(String directory) {
        return getStore(directory, false);
    }

    private CachedFileStore getStore(String directory, boolean isGzip) {
        return new CachedFileStore(new ContextMock(vertx) {
            @Override
            public FileSystem fileSystem() {
                return new FileSystemMock(vertx);
            }
        }, new CachedFileStoreSettings()
                .setDirectory(directory)
                .setGzip(isGzip))
                .addListener(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path path) {
                        modifyCalled.set(true);
                    }

                    @Override
                    public void onFileRemove(Path path) {
                        removeCalled.set(true);
                    }
                });
    }
}
