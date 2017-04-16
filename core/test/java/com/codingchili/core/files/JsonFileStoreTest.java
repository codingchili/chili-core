package com.codingchili.core.files;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.files.exception.NoSuchResourceException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the JSON file store.
 */
@RunWith(VertxUnitRunner.class)
public class JsonFileStoreTest {

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Test
    public void testReadObject(TestContext test) throws IOException {
        JsonObject json = JsonFileStore.readObject(testFile("JsonFileStore", "ReadObject.json"));
        test.assertEquals("object", json.getString("item"));
    }

    @Test
    public void testReadList(TestContext test) throws IOException {
        JsonArray array = JsonFileStore.readList(testFile("JsonFileStore", "ReadList.json"));
        test.assertEquals(3, array.size());
    }

    @Test
    public void testReadDirectoryObjects(TestContext test) throws IOException {
        List<JsonObject> json = JsonFileStore.readDirectoryObjects(testDirectory("JsonFileStore/Objects"));
        test.assertEquals(3, json.size());
    }

    @Test
    public void testReadDirectoryList(TestContext test) throws IOException {
        List<JsonArray> json = JsonFileStore.readDirectoryList(testDirectory("JsonFileStore/Lists"));
        test.assertEquals(3, json.size());
    }

    @Test
    public void testWriteObject() {
        JsonFileStore.writeObject(new JsonObject(), testFile("JsonFileStore", "tmp.json"));
    }

    @Test
    public void testReadMissing(TestContext test) {
        try {
            JsonFileStore.readObject("missing.json");
            test.fail("store did not throw NoSuchResource when target file is missing.");
        } catch (NoSuchResourceException ignored) {
        }
    }

    @Test
    public void testDeleteObject(TestContext test) {
        JsonFileStore.writeObject(new JsonObject(), testFile("JsonFileStore", "tmp.json"));
        test.assertTrue(JsonFileStore.deleteObject(testFile("JsonFileStore", "tmp.json")));
    }

}
