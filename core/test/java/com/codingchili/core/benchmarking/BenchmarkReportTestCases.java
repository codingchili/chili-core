package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 */
@Ignore("Extend this class to run the tests.")
public class BenchmarkReportTestCases {
    // reports are saved to the working directory as 'yyyy-MM-dd HH.mm.ss' + extension.
    private static final Pattern REPORT_FILE = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2}\\.(html|txt)");
    protected List<BenchmarkGroup> groups = new ArrayList<>();
    protected BenchmarkReport report;
    protected CoreContext context;
    private Set<String> reportsBefore;

    @Before
    public void listReports() {
        reportsBefore = reports();
    }

    @After
    public void removeReports() {
        Set<String> created = reports();
        created.removeAll(reportsBefore);
        created.forEach(name -> new File(name).delete());
    }

    private static Set<String> reports() {
        String[] names = new File(".").list((directory, name) -> REPORT_FILE.matcher(name).matches());
        return (names == null) ? new HashSet<>() : new HashSet<>(List.of(names));
    }

    @Before
    public void setUp() {
        context = new SystemContext();
        groups.add(new MockGroupBuilder(context, "group#1", 750));
        groups.add(new MockGroupBuilder(context, "group#2", 500));
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }
}
