package com.codingchili.core.files;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.*;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Tests the loading of files in CachedFileStore.
 */
@RunWith(VertxUnitRunner.class)
public class CachedFileStoreTest {
    private static final String TEST_DIRECTORY = CoreStrings.testDirectory("CachedFileStore");
    private static final String TEST_FILE_ABSOLUTE = CoreStrings.testFile("CachedFileStore", "test.txt");
    private static final String TEST_FILE = "test.txt";
    private static final String TEST_FILE_TRAVERSAL = "../TraversalTestFile.txt";
    private Vertx vertx;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        async.complete();
        CachedFileStore.reset();
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void failGetWithTraversal(TestContext context) {
        try {
            getStore(TEST_DIRECTORY).getFile(TEST_FILE_TRAVERSAL);
            context.fail("Should not be able to access file.");
        } catch (FileMissingException ignored) {
        }
    }

    @Test
    public void succeedGetFile() throws FileMissingException, IOException {
        Buffer fromStore = getStore(TEST_DIRECTORY).getFile(TEST_FILE);
        Buffer fromDisk = getFromDisk();

        Assert.assertEquals(fromDisk.length(), fromStore.length());

        for (int i = 0; i < fromDisk.length(); i++) {
            Assert.assertEquals(fromDisk.getByte(i), fromStore.getByte(i));
        }
    }

    private Buffer getFromDisk() throws IOException {
        Path path = Paths.get(TEST_FILE_ABSOLUTE);
        return Buffer.buffer(Files.readAllBytes(path));
    }

    @Test
    public void succeedGetFileWithGzip() throws FileMissingException, IOException {
        Buffer fromStore = getStore(TEST_DIRECTORY, true).getFile(TEST_FILE);
        Buffer fromDisk = getFromDisk();

        fromStore = Buffer.buffer(Serializer.ungzip(fromStore.getBytes()));

        Assert.assertEquals(fromDisk.length(), fromStore.length());

        for (int i = 0; i < fromDisk.length(); i++) {
            Assert.assertEquals(fromDisk.getByte(i), fromStore.getByte(i));
        }
    }

    private CachedFileStore<Buffer> getStore(String directory) {
        return getStore(directory, false);
    }

    private CachedFileStore<Buffer> getStore(String directory, boolean isGzip) {
        return new CachedFileStore<>(new ContextMock(vertx), new CachedFileStoreSettings()
                .setDirectory(directory)
                .setGzip(isGzip));
    }
}
