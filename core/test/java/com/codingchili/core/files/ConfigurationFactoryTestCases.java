package com.codingchili.core.files;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.codingchili.core.files.exception.NoSuchResourceException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Tests the JSON file store.
 */
@RunWith(VertxUnitRunner.class)
@Ignore("Extend this class to run tests.")
public abstract class ConfigurationFactoryTestCases {
    private static final String DIR = ConfigurationFactoryTestCases.class.getSimpleName();
    private String extension;

    public ConfigurationFactoryTestCases(FileStore store) {
        extension = store.getExtensions().iterator().next();
    }

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Test
    public void testReadObject(TestContext test) {
        JsonObject json = ConfigurationFactory.readObject(testFile(DIR, getFile("ReadObject")));
        test.assertEquals("object", json.getString("item"));
    }

    @Test
    public void testReadDirectoryObjects(TestContext test) {
        Collection<JsonObject> json = ConfigurationFactory.readDirectory(testDirectory(DIR + "/Objects"))
                .collect(Collectors.toList());

        test.assertEquals(2, json.size());
    }

    @Test
    public void testReadDirectoryTree(TestContext test) {
        Collection<JsonObject> json = ConfigurationFactory.readDirectoryTree(testDirectory(DIR + "/Objects"))
                .collect(Collectors.toList());

        test.assertEquals(3, json.size());
    }

    @Test
    public void testWriteObject() {
        ConfigurationFactory.writeObject(new JsonObject(), testFile(DIR, getFile("tmp")));
        ConfigurationFactory.delete(testFile(DIR, getFile("tmp")));
    }

    @Test
    public void testReadMissing(TestContext test) {
        try {
            ConfigurationFactory.readObject(getFile("missing"));
            test.fail("store did not throw NoSuchResource when target file is missing.");
        } catch (NoSuchResourceException ignored) {
        }
    }

    @Test
    public void testDeleteObject(TestContext test) {
        ConfigurationFactory.writeObject(new JsonObject(), testFile(DIR, getFile("tmp")));
        test.assertTrue(ConfigurationFactory.delete(testFile(DIR, getFile("tmp"))));
    }

    private String getFile(String name) {
        return name + extension;
    }
}
