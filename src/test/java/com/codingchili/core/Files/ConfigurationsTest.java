package com.codingchili.core.files;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.*;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.configuration.exception.InvalidConfigurableException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Tests for the configuration system.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {
    private static final String CONFIGURATIONS = "Configurations";
    private static final String DEFAULT_JSON = "default.json";
    private CoreContext context;

    @Rule
    public Timeout timeout = new Timeout(6, TimeUnit.SECONDS);

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
                        if (path.equals(Strings.testFile(CONFIGURATIONS, DEFAULT_JSON))) {
                            Configurations.reset();
                            async.complete();
                        }
                    }
                };
            }
        });
        load(Strings.testFile(CONFIGURATIONS, DEFAULT_JSON), ConfigurableTest.class);
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
