package com.codingchili.core.protocol;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.ERROR_CLUSTERING_REQUIRED;

/**
 * @author Robin Duda
 *
 * Tests the clusternode class to require clustering.
 */
@RunWith(VertxUnitRunner.class)
public class ClusterNodeIT {
    private Vertx vertx;

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void testVertxNotClusteredError(TestContext test) {
        try {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new ClusterNode() {});
        } catch (RuntimeException e) {
            test.assertEquals(ERROR_CLUSTERING_REQUIRED, e.getMessage());
        }
    }

    @Test
    public void testVertxClusteredOk(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), handler -> {
            vertx = handler.result();
            handler.result().deployVerticle(new ClusterNode() {});
            async.complete();
        });
    }

}
