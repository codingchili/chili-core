package com.codingchili.core;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.Context.Delay;
import com.codingchili.core.Context.LaunchContext;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the launcher.
 */
@RunWith(VertxUnitRunner.class)
public class LauncherIT {
    public static Async async;
    private ContextMock context;

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
        Delay.initialize(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Ignore
    @Test
    public void testFailNotVerticle(TestContext test) {
        async = test.async();

        new Launcher(getLaunchContextFor("com.codingchili.core.LauncherTest$1")) {
            @Override
            void exit() {
                async.complete();
            }
        };
    }

    @Test
    public void testDeployAService(TestContext test) {
        async = test.async();
        new Launcher(getLaunchContextFor("com.codingchili.core.IsClusterNode"));
    }

    public LaunchContext getLaunchContextFor(String node) {
        return new LaunchContext(new String[]{node}) {
            @Override
            protected List<String> block(String block) {
                List<String> list = new ArrayList<>();
                list.add(node);
                return list;
            }
        };
    }

    /**
     * Test class that is not a cluster node and will fail to deploy.
     */
    private static class NotClusterNode {
    }
}
