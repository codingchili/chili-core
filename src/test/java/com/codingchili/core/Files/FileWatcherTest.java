package com.codingchili.core.Files;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.Path;

import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class FileWatcherTest {
    private ContextMock context;

    @Before
    public void setUp() {
        this.context = new ContextMock(Vertx.vertx());
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void getNotifiedOnChange() {
        new FileWatcherBuilder(context)
                .onDirectory("")
                .rate(() -> 50)
                .withListener(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path path) {

                    }

                    @Override
                    public void onFileRemove(Path path) {

                    }
                }).build();
    }

    @Test
    public void getNotifiedOnDelete() {

    }
}
