package com.codingchili.core.context;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.Environment;

/**
 * Tests for the simple service context.
 */
@RunWith(VertxUnitRunner.class)
public class SimpleServiceContextTest {
    private static final String TESTNODE = "testnode.node";
    private CoreContext core;

    @Before
    public void setUp() {
        core = new SystemContext(Vertx.vertx());
    }

    @After
    public void tearDown() {
        core.vertx().close();
    }

    @Test
    public void testGetService(TestContext test) {
        ServiceContext context = new SimpleServiceContext(core, TESTNODE);
        test.assertEquals(context.service().node(), TESTNODE);
        test.assertEquals(
                context.service().host(),
                Environment.hostname().orElse(""));
    }
}
