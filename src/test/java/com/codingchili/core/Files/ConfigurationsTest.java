package com.codingchili.core.Files;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.Configuration.*;
import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Exception.InvalidConfigurableException;
import com.codingchili.core.Logging.ConsoleLogger;
import com.codingchili.core.Logging.Logger;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Tests for the configuration system.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {
    private CoreContext context;

    @Rule
    public Timeout timeout = new Timeout(50, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
        Configurations.reset();
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
        Configurations.reset();
    }

    @Test
    public void loadLauncherConfiguration() throws IOException {
        Assert.assertNotNull(Configurations.launcher());
    }

    @Test
    public void loadValidatorConfiguration() {
        Assert.assertNotNull(Configurations.validator());
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
    public void testLoadDefaultsFromConfigurable(TestContext test) {
        Async async = test.async();

        Configurations.initialize(new ContextMock(context) {
            @Override
            public Logger console() {
                return new ConsoleLogger() {

                    @Override
                    public void onConfigurationDefaultsLoaded(String path, Class<?> clazz) {
                        if (path.equals(Strings.testFile("Configurations", "default.json"))) {
                            Configurations.reset();
                            async.complete();
                        }
                    }
                };
            }
        });
        load(Strings.testFile("Configurations", "default.json"), ConfigurableTest.class);
    }

    @Test
    public void testLoadInvalidConfigurable(TestContext test) {
        Async async = test.async();
        try {
            load("z", Strings.class);
        } catch (InvalidConfigurableException e) {
            async.complete();
        }
    }

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
