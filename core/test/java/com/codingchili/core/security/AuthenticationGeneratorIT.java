package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Tests for the generation of secrets and tokens in configuration files.
 */
@RunWith(VertxUnitRunner.class)
public class AuthenticationGeneratorIT {
    private static final String AUTHENTICATION_GENERATOR = "AuthenticationGenerator";
    private static final String GLOBAL = "global";
    private static final String SERVICE_REGEX = "service.*";
    private static final String LOCAL = "local";
    private static final String SERVICE_1 = "service1";
    private static final String SERVICE_1_SECRET = "service1secret";
    private static final String SERVICE_1_TOKEN = "service1token";
    private static final String SERVICE_2 = "service2";
    private static final String SERVICE_2_TOKEN = "service2token";
    private static final String SERVICE1_JSON = "service1.json";
    private static final String SERVICE2_JSON = "service2.json";
    private AuthenticationGenerator generator;
    private SystemContext context;

    @Before
    public void setUp() {
        context = new ContextMock();

        Configurations.put(createSecuritySettings());

        generator = new AuthenticationGenerator(context, CoreStrings.testDirectory(AUTHENTICATION_GENERATOR) + DIR_ROOT);
    }

    @After
    public void tearDown(TestContext test) {
        Configurations.reset();
        context.close(test.asyncAssertSuccess());
    }

    private SecuritySettings createSecuritySettings() {
        SecuritySettings security = new SecuritySettings();
        HashMap<String, AuthenticationDependency> dependencies = new HashMap<>();

        security.setPath(PATH_SECURITY);
        security.setSecretBytes(64);

        dependencies.put(SERVICE_REGEX, new AuthenticationDependency()
                .addPreshare(GLOBAL)
                .addSecret(LOCAL)
        );

        dependencies.put(SERVICE_1, new AuthenticationDependency()
                .addSecret(SERVICE_1_SECRET)
                .addToken(SERVICE_1_TOKEN, SERVICE_2, LOCAL)
        );

        dependencies.put(SERVICE_2, new AuthenticationDependency()
                .addToken(SERVICE_2_TOKEN, SERVICE_1, SERVICE_1_SECRET)
        );

        security.setDependencies(dependencies);
        return security;
    }

    @After
    public void tearDown() {
        ConfigurationFactory.writeObject(new JsonObject(), testFile(AUTHENTICATION_GENERATOR, SERVICE1_JSON));
        ConfigurationFactory.writeObject(new JsonObject(), testFile(AUTHENTICATION_GENERATOR, SERVICE2_JSON));
    }

    @Test
    public void testGenerateSecrets(TestContext test) throws IOException {
        generator.secrets();

        test.assertNotNull(getService1().getString(LOCAL));
        test.assertNotNull(getService2().getString(LOCAL));
        test.assertNotEquals(getService1().getString(LOCAL), getService2().getString(LOCAL));
        test.assertNotNull(getService1().getString(SERVICE_1_SECRET));
        test.assertNull(getService2().getString(SERVICE_1_SECRET));
    }

    @Test
    public void testGeneratePreshared(TestContext test) {
        generator.preshare();
        test.assertEquals(getService1().getString(GLOBAL), getService2().getString(GLOBAL));
    }

    @Test
    @Ignore("file/test based race conditions.")
    public void testGenerateTokens(TestContext test) {
        Async async = test.async();
        generator.all().onComplete(done -> {
            test.assertTrue(done.succeeded());

            byte[] service1secret = getService1().getBinary(SERVICE_1_SECRET);
            byte[] service2secret = getService2().getBinary(LOCAL);

            TokenFactory service1factory = new TokenFactory(context, service1secret);
            TokenFactory service2factory = new TokenFactory(context, service2secret);

            Token service1token = Serializer.unpack(getService1().getJsonObject(SERVICE_1_TOKEN), Token.class);
            Token service2token = Serializer.unpack(getService2().getJsonObject(SERVICE_2_TOKEN), Token.class);

            service1factory.verify(service2token).onComplete(verify -> {
                if (!verify.succeeded()) {
                    throw new CoreRuntimeException(verify.cause());
                }

                service2factory.verify(service1token).onComplete(verify2 -> {
                    if (!verify.succeeded()) {
                        throw new CoreRuntimeException(verify.cause());
                    }
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testGenerateWithBrokenReferenceFails(TestContext test) {
        String tokenName = "token";
        String missingName = "mising-target-secret";
        SecuritySettings settings = Configurations.get(PATH_SECURITY, SecuritySettings.class);
        settings.getDependencies().get(SERVICE_1).addToken(tokenName, SERVICE_2, missingName);
        try {
            generator.all();
            test.fail("Generation did not fail when missing a secret dependency.");
        } catch (SecurityMissingDependencyException ignored) {
        }
    }

    @Test
    public void testGenerateAll() {
        generator.all();
    }

    private JsonObject getService1() {
        return ConfigurationFactory.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE1_JSON));
    }

    private JsonObject getService2() {
        return ConfigurationFactory.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE2_JSON));
    }
}
