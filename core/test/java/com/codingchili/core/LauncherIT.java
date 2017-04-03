package com.codingchili.core;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.codingchili.core.context.Delay;
import com.codingchili.core.context.LaunchContext;
import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the launcher.
 */
@RunWith(VertxUnitRunner.class)
public class LauncherIT {
    private static Async async;
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
        launchWithFail(NotClusterNode.class.getName(), test.async());
    }

    @Test
    public void testFailNotFound(TestContext test) {
        launchWithFail("com.codingchili.core.Missing$1", test.async());
    }

    @Test
    public void testMetricsEnabled(TestContext test) {
        async = test.async();
        system().setMetrics(true);
        onStart = (vx) -> test.assertTrue(vx.isMetricsEnabled());
        launchWithSuccess(TestNode.class);
    }

    @Test
    public void testMetricsDisabled(TestContext test) {
        async = test.async();
        system().setMetrics(false);
        onStart = (vx) -> test.assertFalse(vx.isMetricsEnabled());
        launchWithSuccess(TestNode.class);
    }

    @Test
    public void testDeployAService(TestContext test) {
        async = test.async();
        launchWithSuccess(TestNode.class);
    }

    @Test
    public void testDeployVerticle(TestContext test) {
        async = test.async();
        launchWithSuccess(TestNodeVerticle.class);
    }

    public void launchWithSuccess(Class klass) {
        new Launcher(getLaunchContextFor(klass.getName()));
    }

    public void launchWithFail(Class klass, Async async) {
        launchWithFail(klass.getName(), async);
    }

    public void launchWithFail(String klass, Async async) {
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

    private static Consumer<Vertx> onStart = (vx) -> {};

    /**
     * Testnode that calls async-complete on deploy.
     */
    public static class TestNode extends ClusterNode {
        @Override
        public void start(Future<Void> future) {
            onStart.accept(vertx);
            async.complete();
            future.complete();
        }
    }

    /**
     * Testnode that calls async-complete on deploy.
     */
    public static class TestNodeVerticle extends AbstractVerticle {
        @Override
        public void start(Future<Void> future) {
            onStart.accept(vertx);
            async.complete();
            future.complete();
        }
    }

    /**
     * Test class that is not a cluster node and will fail to deploy.
     */
    private static class NotClusterNode {
    }
}
