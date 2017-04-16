package com.codingchili.core;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codingchili.core.context.*;
import com.codingchili.core.listener.*;

import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the launcher.
 */
@RunWith(VertxUnitRunner.class)
public class LauncherIT {
    private static TestContext test;
    private static Async async;
    private LaunchContext context;

    @Before
    public void setUp() {
        Delay.initialize(context);
    }

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @After
    public void tearDown(TestContext test) {
        if (context != null)
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
        launchWithSuccess(TestService.class);
    }

    @Test
    public void testMetricsDisabled(TestContext test) {
        async = test.async();
        system().setMetrics(false);
        onStart = (vx) -> test.assertFalse(vx.isMetricsEnabled());
        launchWithSuccess(TestService.class);
    }

    @Test
    public void testDeployHandler(TestContext test) {
        LauncherIT.test = test;
        async = test.async();
        launchWithSuccess(TestHandler.class);
    }

    @Test
    public void testDeployListener(TestContext test) {
        LauncherIT.test = test;
        async = test.async();
        launchWithSuccess(TestListener.class);
    }

    @Test
    public void testDeployAService(TestContext test) {
        LauncherIT.test = test;
        async = test.async();
        launchWithSuccess(TestService.class);
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
        context = new LaunchContext(new String[]{}) {
            @Override
            protected List<String> block(String block) {
                List<String> list = new ArrayList<>();
                list.add(node);
                return list;
            }
        };
        return context;
    }

    private static Consumer<Vertx> onStart = (vx) -> {
    };

    /**
     * Testnode that calls async-complete on deploy.
     */
    public static class TestService implements CoreService {
        private CoreContext core;

        @Override
        public void init(CoreContext core) {
            this.core = core;
        }

        @Override
        public void stop(Future<Void> stop) {
            stop.complete();
        }

        @Override
        public void start(Future<Void> start) {
            test.assertNotNull(core);
            onStart.accept(core.vertx());
            async.complete();
            start.complete();
        }
    }

    public static class TestHandler implements CoreHandler {

        public TestHandler() {
            async.complete();
        }

        @Override
        public void handle(Request request) {

        }

        @Override
        public String address() {
            return "";
        }
    }

    public static class TestListener implements CoreListener {
        private ListenerSettings settings;
        private CoreContext core;
        private CoreHandler handler;

        @Override
        public void init(CoreContext core) {
            this.core = core;
        }

        @Override
        public CoreListener settings(Supplier<ListenerSettings> settings) {
            this.settings = settings.get();
            return this;
        }

        @Override
        public CoreListener handler(CoreHandler handler) {
            this.handler = handler;
            return this;
        }

        @Override
        public void start(Future<Void> start) {
            test.assertNotNull(settings);
            test.assertNotNull(core);
            test.assertNotNull(handler);
            async.complete();
            start.complete();
        }

        @Override
        public void stop(Future<Void> stop) {
            stop.complete();
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
