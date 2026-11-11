package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.security.AuthenticationGenerator.Mode.*;
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

/**
 * Tests the generator modes (fill missing, rotate, retire) and secret rollover in the token factory.
 */
@RunWith(VertxUnitRunner.class)
public class SecretRotationTest {
    private static final String DIR = "build/tmp/SecretRotation";
    private static final String GLOBAL = "global";
    private static final String LOCAL = "local";
    private static final String SERVICE_1_SECRET = "service1secret";
    private static final String SERVICE_1_TOKEN = "service1token";
    private static final String SERVICE_2_TOKEN = "service2token";
    private static final String PREVIOUS = SECRET_PREVIOUS_SUFFIX;
    private SystemContext context;

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Before
    public void setUp() {
        context = new ContextMock();
        Configurations.put(securitySettings());
        delete();
        write("service1", new JsonObject());
        write("service2", new JsonObject());
    }

    @After
    public void tearDown(TestContext test) {
        delete();
        Configurations.reset();
        context.close().onComplete(test.asyncAssertSuccess());
    }

    private static SecuritySettings securitySettings() {
        var security = new SecuritySettings();
        var dependencies = new HashMap<String, AuthenticationDependency>();
        security.setPath(PATH_SECURITY);
        security.setSecretBytes(32);

        dependencies.put("service.*", new AuthenticationDependency().addPreshare(GLOBAL).addSecret(LOCAL));
        dependencies.put("service1", new AuthenticationDependency()
                .addSecret(SERVICE_1_SECRET)
                .addToken(SERVICE_1_TOKEN, "service2", LOCAL));
        dependencies.put("service2", new AuthenticationDependency()
                .addToken(SERVICE_2_TOKEN, "service1", SERVICE_1_SECRET));
        security.setDependencies(dependencies);
        return security;
    }

    @Test
    public void fillKeepsExistingAndFillsNewFile(TestContext test) {
        generate(FILL).compose(v -> {
            var service1 = read("service1");
            var service2 = read("service2");
            write("service3", new JsonObject());

            return generate(FILL).map(done -> {
                test.assertEquals(service1, read("service1"));
                test.assertEquals(service2, read("service2"));

                var service3 = read("service3");
                test.assertNotNull(service3.getString(LOCAL));
                test.assertNotEquals(service1.getString(LOCAL), service3.getString(LOCAL));
                // the preshared key of the new file is the existing one.
                test.assertEquals(service1.getString(GLOBAL), service3.getString(GLOBAL));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void fillKeepsTheMostCommonPreshare(TestContext test) {
        write("service1", new JsonObject().put(GLOBAL, "a"));
        write("service2", new JsonObject().put(GLOBAL, "b"));
        write("service3", new JsonObject().put(GLOBAL, "b"));

        generate(FILL).onComplete(test.asyncAssertSuccess(done -> {
            test.assertEquals("b", read("service1").getString(GLOBAL));
            test.assertEquals("b", read("service2").getString(GLOBAL));
        }));
    }

    @Test
    public void fillKeepsValidTokensAndReissuesInvalid(TestContext test) {
        generate(FILL).compose(v -> {
            var token1 = read("service1").getJsonObject(SERVICE_1_TOKEN);
            var token2 = read("service2").getJsonObject(SERVICE_2_TOKEN);

            // service2token no longer verifies with service1secret: reissued; service1token is kept.
            write("service2", read("service2").put(SERVICE_2_TOKEN, token2.copy().put("key", "forged")));

            return generate(FILL).compose(done -> {
                test.assertEquals(token1, read("service1").getJsonObject(SERVICE_1_TOKEN));
                var reissued = read("service2").getJsonObject(SERVICE_2_TOKEN);
                test.assertNotEquals("forged", reissued.getString("key"));
                return factory("service1", SERVICE_1_SECRET).verify(token(reissued));
            }).compose(done -> {
                // a missing token is issued.
                write("service1", read("service1").put(SERVICE_1_TOKEN, (Object) null));
                return generate(FILL);
            }).map(done -> {
                test.assertNotNull(read("service1").getJsonObject(SERVICE_1_TOKEN));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void rotateChangesEverythingAndKeepsPrevious(TestContext test) {
        generate(FILL).compose(v -> {
            var service1 = read("service1");
            var service2 = read("service2");

            return generate(ROTATE).map(done -> {
                var rotated1 = read("service1");
                var rotated2 = read("service2");

                for (var key : new String[]{LOCAL, SERVICE_1_SECRET, GLOBAL}) {
                    test.assertNotEquals(service1.getString(key), rotated1.getString(key));
                    test.assertEquals(service1.getString(key), rotated1.getString(key + PREVIOUS));
                }
                test.assertEquals(rotated1.getString(GLOBAL), rotated2.getString(GLOBAL));
                test.assertEquals(service2.getString(GLOBAL), rotated2.getString(GLOBAL + PREVIOUS));
                test.assertNotEquals(service1.getJsonObject(SERVICE_1_TOKEN), rotated1.getJsonObject(SERVICE_1_TOKEN));
                test.assertNotEquals(service2.getJsonObject(SERVICE_2_TOKEN), rotated2.getJsonObject(SERVICE_2_TOKEN));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void retireRemovesPreviousOnly(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            var rotated = read("service1");
            return generate(RETIRE).map(done -> {
                var retired = read("service1");
                test.assertNull(retired.getValue(LOCAL + PREVIOUS));
                test.assertNull(retired.getValue(GLOBAL + PREVIOUS));
                test.assertNull(retired.getValue(SERVICE_1_SECRET + PREVIOUS));
                test.assertEquals(rotated.getString(LOCAL), retired.getString(LOCAL));
                test.assertEquals(rotated.getJsonObject(SERVICE_1_TOKEN), retired.getJsonObject(SERVICE_1_TOKEN));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void tokenOfPreviousSecretVerifiesUntilRetired(TestContext test) {
        byte[] previous = BASE64_DECODER.decode(SecretFactory.generate(32));
        byte[] current = BASE64_DECODER.decode(SecretFactory.generate(32));
        var old = new Token("old");
        var rotated = config(current, previous);

        TokenFactory.strict(context, previous).hmac(old).compose(v -> {
            SecretRollover.register("rollover.yaml", rotated);
            return new TokenFactory(context, current).verify(old);
        }).compose(v -> {
            // a factory created with the old secret (before the rotation) signs with the current one.
            var fresh = new Token("new");
            return new TokenFactory(context, previous).hmac(fresh)
                    .compose(signed -> TokenFactory.strict(context, current).verify(fresh))
                    .compose(signed -> new TokenFactory(context, previous).verify(fresh));
        }).compose(v -> {
            SecretRollover.register("rollover.yaml", config(current, null));
            return new TokenFactory(context, current).verify(old)
                    .compose(accepted -> Future.<Void>failedFuture("accepted a token of a retired secret"),
                            rejected -> new TokenFactory(context, previous).verify(old)
                                    .compose(accepted -> Future.<Void>failedFuture("accepted after retire"),
                                            rejectedAgain -> Future.succeededFuture()));
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void loadedConfigurationRegistersRollover(TestContext test) {
        byte[] previous = BASE64_DECODER.decode(SecretFactory.generate(32));
        byte[] current = BASE64_DECODER.decode(SecretFactory.generate(32));
        var old = new Token("old");
        String path = DIR + "/loaded.json";
        ConfigurationFactory.writeObject(config(current, previous), path);

        TokenFactory.strict(context, previous).hmac(old).compose(v -> {
            Configurations.get(path, BaseConfigurable.class);
            return new TokenFactory(context, current).verify(old);
        }).onComplete(test.asyncAssertSuccess());
    }

    private static JsonObject config(byte[] current, byte[] previous) {
        var config = new JsonObject().put("secret", current);
        if (previous != null) {
            config.put("secret" + PREVIOUS, previous);
        }
        return config;
    }

    private Future<Void> generate(AuthenticationGenerator.Mode mode) {
        return new AuthenticationGenerator(context, DIR + DIR_ROOT).setMode(mode).all();
    }

    private TokenFactory factory(String service, String secret) {
        return TokenFactory.strict(context, BASE64_DECODER.decode(read(service).getString(secret)));
    }

    private static Token token(JsonObject json) {
        return Serializer.unpack(json, Token.class);
    }

    private static JsonObject read(String name) {
        return ConfigurationFactory.readObject(DIR + DIR_ROOT + name + ".json");
    }

    private static void write(String name, JsonObject json) {
        ConfigurationFactory.writeObject(json, DIR + DIR_ROOT + name + ".json");
    }

    private static void delete() {
        var dir = new File(DIR);
        for (var file : Objects.requireNonNullElse(dir.listFiles(), new File[0])) {
            file.delete();
        }
        dir.delete();
    }
}
