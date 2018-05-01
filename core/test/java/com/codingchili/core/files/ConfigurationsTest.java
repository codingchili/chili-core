package com.codingchili.core.files;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.exception.InvalidConfigurableException;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 * <p>
 * Tests for the configuration system.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {

    @Rule
    public Timeout timeout = new Timeout(4, TimeUnit.SECONDS);
    private CoreContext context;

    @Before
    public void setUp() {
        Configurations.shutdown();
        context = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
        Configurations.reset();
        Configurations.shutdown();
    }

    @Test
    public void loadLauncherConfiguration() {
        Assert.assertNotNull(Configurations.launcher());
    }

    @Test
    public void loadSystemConfiguration() {
        Assert.assertNotNull(Configurations.system());
    }

    @Test
    public void loadSecurityConfiguration() {
        Assert.assertNotNull(Configurations.security());
    }

    @Test
    public void loadStorageConfiguration() {
        Assert.assertNotNull(Configurations.storage());
    }

    @Test
    public void testLoadInvalidConfigurable(TestContext test) {
        Async async = test.async();
        try {
            load("z", CoreStrings.class);
        } catch (InvalidConfigurableException e) {
            async.complete();
        }
    }

    @SuppressWarnings("unchecked")
    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, (Class<Configurable>) clazz);
    }
}
