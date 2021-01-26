package com.codingchili.core.context;

import com.codingchili.core.configuration.Environment;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.files.Configurations;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Tests for argument/parsing and block/remote mapping.
 */
@RunWith(VertxUnitRunner.class)
public class LaunchContextCommandLine {
    private static final String SERVICE_DEFAULT = "service-default";
    private static final String SERVICE_REMOTE = "service-remote";
    private static final String SERVICE_1 = "service-1";
    private static final String SERVICE_2 = "service-2";
    private static final String SERVICE_3 = "service-3";
    private static final String BLOCK_DEFAULT = "default";
    private static final String BLOCK_REMOTE = "remote";
    private static final String BLOCK_1 = "block_1";
    private static final String BLOCK_2 = "block_2";
    private static final String BLOCK_3 = "block_3";
    private static final String DEPLOY = "deploy";

    @Test
    public void singleBlock_3() {
        execute(DEPLOY, BLOCK_3).onSuccess(services -> {
            Assert.assertTrue(services.contains(SERVICE_3));
            Assert.assertEquals(1, services.size());
        });
    }

    @Test
    public void multipleBlocks_1_2() {
        execute(DEPLOY, String.format("%s,%s", BLOCK_1, BLOCK_2))
                .onSuccess(services -> {
                    Assert.assertTrue(services.contains(SERVICE_1));
                    Assert.assertTrue(services.contains(SERVICE_2));
                    Assert.assertEquals(2, services.size());
                });
    }

    @Test
    public void assertHostName() {
        executeWithHostMatching(DEPLOY).onSuccess(services -> {
            Assert.assertTrue(services.contains(SERVICE_REMOTE));
            Assert.assertEquals(1, services.size());
        });
    }

    @Test
    public void assertDefaultBlock() {
        execute(DEPLOY).onSuccess(services -> {
            Assert.assertTrue(services.contains(SERVICE_DEFAULT));
            Assert.assertEquals(1, services.size());
        });
    }

    private Future<List<String>> executeWithHostMatching(String... line) {
        return execute(true, line);
    }

    private Future<List<String>> execute(String... line) {
        return execute(false, line);
    }

    private Future<List<String>> execute(Boolean matchHost, String... line) {
        Configurations.put(new LauncherSettings());
        Configurations.launcher()
                .addBlock(BLOCK_1, List.of(SERVICE_1))
                .addBlock(BLOCK_2, List.of(SERVICE_2))
                .addBlock(BLOCK_3, List.of(SERVICE_3))
                .addBlock(BLOCK_DEFAULT, List.of(SERVICE_DEFAULT))
                .addBlock(BLOCK_REMOTE, List.of(SERVICE_REMOTE))
                .addHost(matchHost ? Environment.hostname().orElse("failed to get hostname") : "null", BLOCK_REMOTE);

        var promise = Promise.<List<String>>promise();
        var context = new LaunchContext(line);
        context.execute().onComplete(result -> {
            try {
                promise.complete(context.services());
            } catch (CoreException e) {
                Assert.fail("Failed to list services: " + e.getMessage());
            }
        });
        return promise.future();
    }

}
