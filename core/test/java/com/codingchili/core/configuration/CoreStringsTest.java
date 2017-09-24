package com.codingchili.core.configuration;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Robin Duda
 * <p>
 * Tests for the String constant/helper class.
 */
@RunWith(VertxUnitRunner.class)
public class CoreStringsTest {
    private static final String FILE_NAME = "testfile.txt";
    private static final String FOLDER_NAME = "CoreStrings";
    private static final String FOLDER_RELATIVE = "test/resources/CoreStrings";
    private static final String FILE_RELATIVE = "test/resources/CoreStrings/testfile.txt";

    @Test
    public void testFormatRelativePath(TestContext test) {
        Path testDir = Paths.get(CoreStrings.testFile(FOLDER_NAME, FILE_NAME)).toAbsolutePath();
        Path testFile = Paths.get(CoreStrings.testDirectory(FOLDER_NAME)).toAbsolutePath();

        String relative = CoreStrings.format(testDir, testFile.toString());
        test.assertEquals(relative, FILE_NAME);
    }

    @Test
    public void testFormatPreserveSlashes(TestContext test) {
        String path = CoreStrings.format(Paths.get(FOLDER_RELATIVE), "test");
        test.assertEquals(path, "resources/CoreStrings");
    }

    @Test
    public void testGetTestDirectory(TestContext test) {
        String testDir = CoreStrings.testDirectory(FOLDER_NAME);
        test.assertEquals(FOLDER_RELATIVE, testDir);
        test.assertTrue(Paths.get(testDir).toFile().exists());
        test.assertTrue(Paths.get(testDir).toFile().isDirectory());
    }

    @Test
    public void testGetTestFile(TestContext test) {
        String testFile = CoreStrings.testFile(FOLDER_NAME, FILE_NAME);
        test.assertEquals(FILE_RELATIVE, testFile);
        test.assertTrue(Paths.get(testFile).toFile().exists());
        test.assertTrue(Paths.get(testFile).toFile().isFile());
    }
}
