package com.codingchili.core.context;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Tests for the TimerSource API.
 */
@RunWith(VertxUnitRunner.class)
public class TimerSourceTest {

    @Test
    public void constructors() {
        TimerSource.of(0, TimeUnit.MILLISECONDS);
        TimerSource.of(0, TimeUnit.MILLISECONDS).setName("name");
        TimerSource.of(10);
        TimerSource.of(10).setName("name");
    }

    @Test
    public void pause(TestContext test) {
        TimerSource source = TimerSource.of(10);
        source.pause();
        test.assertTrue(source.isPaused());
        source.unpause();
        test.assertFalse(source.isPaused());
    }

    @Test
    public void terminate(TestContext test) {
        TimerSource source = TimerSource.of(1, TimeUnit.DAYS);
        test.assertFalse(source.isTerminated());
        source.terminate();
        test.assertTrue(source.isTerminated());
    }

}
