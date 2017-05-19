package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;

/**
 * @author Robin Duda
 */
@Ignore("Extend this class to run the tests.")
public class BenchmarkReportTestCases {
    protected List<BenchmarkGroup> groups = new ArrayList<>();
    protected BenchmarkReport report;
    protected CoreContext context;

    @Before
    public void setUp() {
        context = new SystemContext(Vertx.vertx());
        groups.add(new MockGroup(context, "group#1", 750));
        groups.add(new MockGroup(context, "group#2", 500));
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }
}
