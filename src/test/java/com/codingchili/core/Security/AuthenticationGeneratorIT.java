package com.codingchili.core.security;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.Strings.testFile;

/**
 * @author Robin Duda
 *
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
        context = new ContextMock(Vertx.vertx());
        Configurations.initialize(context);

        Configurations.put(createSecuritySettings());

        generator = new AuthenticationGenerator(Strings.testDirectory(AUTHENTICATION_GENERATOR),
                new ConsoleLogger());
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    private SecuritySettings createSecuritySettings() {
        SecuritySettings security = new SecuritySettings();
        HashMap<String, AuthenticationDependency> dependencies = new HashMap<>();

        security.setPath(Strings.PATH_SECURITY);
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
        JsonFileStore.writeObject(new JsonObject(), testFile("AuthenticationGenerator", "service1.json"));
        JsonFileStore.writeObject(new JsonObject(), testFile("AuthenticationGenerator", "service2.json"));
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

    private JsonObject getService1() throws IOException {
        return JsonFileStore.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE1_JSON));
    }

    private JsonObject getService2() throws IOException {
        return JsonFileStore.readObject(testFile(AUTHENTICATION_GENERATOR, SERVICE2_JSON));
    }

    @Test
    public void testGeneratePreshared(TestContext test) throws IOException {
        generator.preshare();

        test.assertEquals(getService1().getString(GLOBAL), getService2().getString(GLOBAL));
    }

    @Test
    public void testGenerateTokens(TestContext test) throws IOException {
        generator.all();

        byte[] service1secret = getService1().getBinary(SERVICE_1_SECRET);
        byte[] service2secret = getService2().getBinary(LOCAL);

        TokenFactory service1factory = new TokenFactory(service1secret);
        TokenFactory service2factory = new TokenFactory(service2secret);

        Token service1token = Serializer.unpack(getService1().getJsonObject(SERVICE_1_TOKEN), Token.class);
        Token service2token = Serializer.unpack(getService2().getJsonObject(SERVICE_2_TOKEN), Token.class);

        test.assertTrue(service1factory.verifyToken(service2token));
        test.assertTrue(service2factory.verifyToken(service1token));
    }

    @Test
    public void testGenerateAll() {
        generator.all();
    }
}
