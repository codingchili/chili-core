package com.codingchili.core.files;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests for the json file store.
 */
@RunWith(VertxUnitRunner.class)
public class JsonFileStoreTest extends ConfigurationFactoryTestCases {

    public JsonFileStoreTest() {
        super(new JsonFileStore());
    }
}
