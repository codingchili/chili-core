package com.codingchili.core.context;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Tests the timers and metrics in the system context.
 */
@RunWith(VertxUnitRunner.class)
public class SystemContextTest {
    private SystemContext context;
    private OnMetricListener listener;
    private SystemSettings settings;

    @Before
    public void setUp() {
        this.settings = new SystemSettings();

        settings.setMetricRate(100);
        settings.setMetrics(true);

        this.context = new ContextMock(Vertx.vertx()) {
            @Override
            protected void onMetricsSnapshot(JsonObject json) {
                if (listener != null) {
                    listener.onMetric(json);
                }
            }

            @Override
            public SystemSettings system() {
                return settings;
            }
        };
        Delay.initialize(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void testMetricsEnabled(TestContext test) {
        Async async = test.async();

        this.listener = metrics -> {
            async.complete();
        };
    }

    @Test
    public void testMetricsDisabled(TestContext test) {
        Async async = test.async();

        settings.setMetrics(false);
        this.listener = metrics -> test.fail("Metrics was not disabled.");

        Delay.forMS(async, settings.getMetricRate() * 2);
    }

    @Test
    public void testCancelTimer(TestContext test) {
        Async async = test.async();

        long id = context.timer(settings.getMetricRate(), handler -> {
            test.fail("The timer was not cancelled.");
        });

        context.cancel(id);

        Delay.forMS(async, settings.getMetricRate() * 2);
    }

    @Test
    public void testTimerCalled(TestContext test) {
        Async async = test.async();
        context.timer(1, event -> async.complete());
    }

    @Test
    public void testPeriodic(TestContext test) {
        Async async = test.async();
        int[] countdown = {3};
        int interval = 25;

        context.periodic(() -> interval, "test", event -> {
            countdown[0]--;
        });

        context.timer(90, event -> {
            test.assertTrue(0 == countdown[0]);
            async.complete();
        });
    }

    @Test
    public void testPeriodicIntervalChanged(TestContext test) {
        Async async = test.async();
        int[] countdown = {3};
        int[] interval = {25};

        // executes once, timer changes, executes again, notices change.
        context.periodic(() -> interval[0], "test", event -> {
            interval[0] = 5000;
            countdown[0]--;
        });

        // assert the periodic is only triggered twice.
        context.timer(interval[0] * 3, event -> {
            test.assertEquals(1, countdown[0]);
            async.complete();
        });
    }

    @FunctionalInterface
    interface OnMetricListener {
        void onMetric(JsonObject metrics);
    }
}
