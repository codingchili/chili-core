package com.codingchili.core.benchmarking;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Test cases for console reporting.
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkConsoleReportTest extends BenchmarkReportTestCases {

    @Before
    public void setUp() {
        super.setUp();
        report = new BenchmarkConsoleReport(groups);
    }

    @Test
    public void testSaveToFile(TestContext test) {
        String file = report.saveToFile();
        test.assertTrue(Paths.get(file).toFile().exists());
    }

    @Test
    public void testDisplay() {
        report.display();
    }

    @Test
    public void testSetTemplate() {
        report.template("%s%s%s%s%s");
        report.saveToFile();
    }

    @Test
    public void testSetInvalidTemplate(TestContext test) {
        try {
            report.template("null");
            test.fail("Did not fail for invalid template");
        } catch (Exception ignored) {
        }
    }
}
