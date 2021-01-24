package com.codingchili.core.context;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.AbstractLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.testing.ContextMock;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the timers and metrics in the system context.
 */
@RunWith(VertxUnitRunner.class)
public class SystemContextTest {
    private SystemContext context;
    private OnMetricListener listener;
    private SystemSettings settings;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        this.settings = Configurations.system();

        settings.getMetrics().setRate(100);
        settings.getMetrics().setEnabled(true);

        this.context = new ContextMock() {
            @Override
            public Logger logger(Class aClass) {
                return new AbstractLogger(getClass()) {
                    @Override
                    public void onMetricsSnapshot(JsonObject metrics) {
                        listener.onMetric(metrics);
                    }
                };
            }

            @Override
            public SystemSettings system() {
                return settings;
            }
        };
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void testMetricsEnabled(TestContext test) {
        Async async = test.async();
        this.listener = metrics -> async.complete();
    }

    @Test
    public void testMetricsDisabled(TestContext test) {
        Async async = test.async();

        settings.getMetrics().setEnabled(false);
        this.listener = metrics -> test.fail("Metrics was not disabled.");

        delayMS(async, settings.getMetrics().getRate() * 2);
    }

    @Test
    public void testCancelTimer(TestContext test) {
        Async async = test.async();

        long id = context.timer(settings.getMetrics().getRate(), handler -> {
            test.fail("The timer was not cancelled.");
        });

        context.cancel(id);

        delayMS(async, settings.getMetrics().getRate() * 2);
    }

    @Test
    public void exceptionsDoesNotExhaustBlockingPool(TestContext test) {
        int poolSize = Configurations.system().getOptions().getInternalBlockingPoolSize();
        Async async = test.async(poolSize * 2);
        for (int i = 0; i < poolSize * 2; i++) {
            context.blocking((blocking) -> {
                throw new RuntimeException();
            }, complete -> async.countDown());
        }
    }

    private void delayMS(Async async, int ms) {
        context.timer(ms, handler -> async.complete());
    }

    @Test
    public void testTimerCalled(TestContext test) {
        Async async = test.async();
        context.timer(1, event -> async.complete());
    }

    @Test
    public void testPeriodic(TestContext test) {
        Async async = test.async();
        AtomicInteger countdown = new AtomicInteger(3);
        int interval = 50;

        context.periodic(TimerSource.of(interval).setName("test"), event -> {
            countdown.getAndDecrement();
        });

        context.timer(185, event -> {
            test.assertTrue(0 == countdown.get());
            async.complete();
        });
    }

    @Test
    public void testPeriodicIntervalChanged(TestContext test) {
        Async async = test.async();
        AtomicInteger countdown = new AtomicInteger(3);
        AtomicInteger interval = new AtomicInteger(50);

        // assert the periodic is only triggered twice.
        context.timer(400, event -> {
            test.assertEquals(1, countdown.get());
            async.complete();
        });

        // executes once, timer changes, executes again, notices change.
        context.periodic(TimerSource.of(interval::get).setName("test"), event -> {
            interval.set(1000);
            countdown.decrementAndGet();
        });
    }

    @FunctionalInterface
    interface OnMetricListener {
        void onMetric(JsonObject metrics);
    }
}
