package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

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
        context = new SystemContext();
        groups.add(new MockGroupBuilder(context, "group#1", 750));
        groups.add(new MockGroupBuilder(context, "group#2", 500));
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }
}
