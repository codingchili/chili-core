package com.codingchili.core.files;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests for the yaml file store.
 */
@RunWith(VertxUnitRunner.class)
public class YamlFileStoreTest extends ConfigurationFactoryTestCases {

    public YamlFileStoreTest() {
        super(new YamlFileStore());
    }
}
