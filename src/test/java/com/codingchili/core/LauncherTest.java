package com.codingchili.core;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
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
public class LauncherTest {
    public static Async async;

    @Before
    public void setUp() {
        Delay.initialize(new ContextMock(Vertx.vertx()));
    }

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
            public List<String> block(String block) {
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
