package com.codingchili.core.Files;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Configuration.CachedFileStoreSettings;
import com.codingchili.core.Exception.FileMissingException;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class CachedFileStoreTest {

    @Test
    public void failGetWithTraversal(TestContext context) {
        CachedFileStore store = CachedFileStore.instance(new ContextMock(Vertx.vertx()), new CachedFileStoreSettings()
                .setDirectory("/src")
                .setGzip(false)
        );

        try {
            store.getFile("../.gitignore");
            context.fail("Should not be able to access file.");
        } catch (FileMissingException ignored) {
        }
    }

}
