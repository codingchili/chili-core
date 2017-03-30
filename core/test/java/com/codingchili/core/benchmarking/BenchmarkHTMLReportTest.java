package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.*;
import java.nio.file.*;

import com.codingchili.core.files.exception.NoSuchResourceException;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the HTML reporter
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkHTMLReportTest extends BenchmarkReportTestCases {

    @Before
    public void setUp() {
        super.setUp();
        report = new BenchmarkHTMLReport(groups);
        report.template("/main/resources/benchmarking/report.jade");
    }

    @Test
    public void testThrowsExceptionOnTemplateMissing(TestContext test) {
        try {
            report.template("missing.jade");
            report.display();
            test.fail("test did not fail when template is missing.");
        } catch (NoSuchResourceException | HeadlessException e) {
            // resource is expected to be missing.
            // headless indicates that an attempt to open the browser has been
            // made (and failed), which is enough to pass this test.
        }
    }

    @Test
    public void setCustomJadeReport(TestContext test) {
        report.template("/benchmarking/report.jade");
        Path path = Paths.get(report.saveToFile());
        test.assertTrue(path.toFile().exists());
    }

    @Test
    public void testReportSavedToFile(TestContext test) {
        Path path = Paths.get(report.saveToFile());
        test.assertTrue(path.toFile().exists());
    }

    @Test
    public void testDisplayHTMLReport() {
        try {
            report.display();
        } catch (HeadlessException e) {
            // headless indicates that an attempt to open the browser has been
            // made (and failed), which is enough to pass this test.
        }
    }
}
