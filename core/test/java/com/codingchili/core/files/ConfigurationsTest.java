package com.codingchili.core.files;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.ConfigurableTest;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.exception.InvalidConfigurableException;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.testing.ContextMock;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 * <p>
 * Tests for the configuration system.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {
    private static final String CONFIGURATIONS = "Configurations";
    private static final String DEFAULT_JSON = "default.json";
    @Rule
    public Timeout timeout = new Timeout(6, TimeUnit.SECONDS);
    private CoreContext context;

    @Before
    public void setUp() {
        context = new ContextMock();
        Configurations.reset();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
        Configurations.reset();
        Configurations.shutdown();
    }

    @Test
    public void loadLauncherConfiguration() throws IOException {
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
    public void testLoadDefaultsFromConfigurable(TestContext test) {
        Async async = test.async();

        Configurations.setWarnOnDefaultsLoaded(true);
        Configurations.initialize(new ContextMock(context) {
            @Override
            public Logger logger(Class aClass) {
                return new ConsoleLogger(aClass) {

                    @Override
                    public void onConfigurationDefaultsLoaded(String path, Class<?> clazz) {
                        if (path.equals(CoreStrings.testFile(CONFIGURATIONS, DEFAULT_JSON))) {
                            Configurations.reset();
                            async.complete();
                        }
                    }
                };
            }
        });
        load(CoreStrings.testFile(CONFIGURATIONS, DEFAULT_JSON), ConfigurableTest.class);
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
