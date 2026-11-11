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
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

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
        context.close().onComplete(test.asyncAssertSuccess());
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
        generator.secrets().onComplete(test.asyncAssertSuccess(done -> {
            test.assertNotNull(getService1().getString(LOCAL));
            test.assertNotNull(getService2().getString(LOCAL));
            test.assertNotEquals(getService1().getString(LOCAL), getService2().getString(LOCAL));
            test.assertNotNull(getService1().getString(SERVICE_1_SECRET));
            test.assertNull(getService2().getString(SERVICE_1_SECRET));
        }));
    }

    @Test
    public void testGeneratePreshared(TestContext test) {
        generator.preshare().onComplete(test.asyncAssertSuccess(done ->
                test.assertEquals(getService1().getString(GLOBAL), getService2().getString(GLOBAL))));
    }

    @Test
    public void testGenerateTokens(TestContext test) {
        generator.all().compose(done -> {
            byte[] service1secret = BASE64_DECODER.decode(getService1().getString(SERVICE_1_SECRET));
            byte[] service2secret = BASE64_DECODER.decode(getService2().getString(LOCAL));

            Token service1token = Serializer.unpack(getService1().getJsonObject(SERVICE_1_TOKEN), Token.class);
            Token service2token = Serializer.unpack(getService2().getJsonObject(SERVICE_2_TOKEN), Token.class);

            // service1token is signed with the local secret of service2, service2token with service1secret.
            return new TokenFactory(context, service1secret).verify(service2token)
                    .compose(verified -> new TokenFactory(context, service2secret).verify(service1token));
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testGenerateWithBrokenReferenceFails(TestContext test) {
        String tokenName = "token";
        String missingName = "mising-target-secret";
        SecuritySettings settings = Configurations.get(PATH_SECURITY, SecuritySettings.class);
        settings.getDependencies().get(SERVICE_1).addToken(tokenName, SERVICE_2, missingName);
        generator.all().onComplete(test.asyncAssertFailure(e ->
                test.assertTrue(e instanceof SecurityMissingDependencyException, String.valueOf(e))));
    }

    @Test
    public void testGenerateAll(TestContext test) {
        generator.all().onComplete(test.asyncAssertSuccess());
    }

    private JsonObject getService1() {
        return ConfigurationFactory.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE1_JSON));
    }

    private JsonObject getService2() {
        return ConfigurationFactory.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE2_JSON));
    }
}
