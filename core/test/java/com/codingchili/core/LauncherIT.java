package com.codingchili.core;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.Delay;
import com.codingchili.core.context.LaunchContext;
import com.codingchili.core.testing.ContextMock;

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

    @Test
    public void testFailNotVerticle(TestContext test) {
        launch(NotClusterNode.class.getName(), test.async());
    }

    @Test
    public void testFailNotFound(TestContext test) {
        launch("com.codingchili.core.Missing$1", test.async());
    }

    @Test
    public void testMetricsEnabled(TestContext test) {
        launch(IsClusterNode.class, test.async());
    }

    @Test
    public void testMetricsDisabled(TestContext test) {
        launch(IsClusterNode.class, test.async());
    }

    @Test
    public void testDeployAService(TestContext test) {
        launch(IsClusterNode.class, test.async());
    }

    public void launch(Class klass, Async async) {
        launch(klass.getName(), async);
    }

    public void launch(String klass, Async async) {
        new Launcher(getLaunchContextFor(klass)) {
            @Override
            void exit() {
                async.complete();
            }
        };
    }

    public LaunchContext getLaunchContextFor(String node) {
        return new LaunchContext(new String[]{}) {
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
