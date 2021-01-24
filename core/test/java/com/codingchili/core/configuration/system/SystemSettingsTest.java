package com.codingchili.core.configuration.system;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SystemSettingsTest {

    @Test
    public void kryoUnsafeDisabledByDefault() {
        Assert.assertFalse(new SystemSettings().isUnsafe());
        Assert.assertEquals(
                "false",
                System.getProperty("kryo.unsafe")
        );
    }

}
