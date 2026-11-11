package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.HashMap;
import java.util.Objects;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.exception.SecretRotationRefusedException;
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
    private static final int TTL = 3600 * 24 * 7;
    private static final int SERVICE_TTL = 3600 * 24 * 30;
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
        security.setTokenttl(TTL);
        security.setServiceTokenttl(SERVICE_TTL);

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
            backdateRotation(TTL + 1);
            var rotated = read("service1");
            return generate(RETIRE).map(done -> {
                var retired = read("service1");
                test.assertNull(retired.getValue(LOCAL + PREVIOUS));
                test.assertNull(retired.getValue(GLOBAL + PREVIOUS));
                test.assertNull(retired.getValue(SERVICE_1_SECRET + PREVIOUS));
                test.assertEquals(rotated.getString(LOCAL), retired.getString(LOCAL));
                test.assertEquals(rotated.getJsonObject(SERVICE_1_TOKEN), retired.getJsonObject(SERVICE_1_TOKEN));
                test.assertNull(retired.getValue(SECRETS_ROTATED));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void rotateRecordsItsTime(TestContext test) {
        long before = Instant.now().getEpochSecond();
        generate(FILL).compose(v -> generate(ROTATE)).onComplete(test.asyncAssertSuccess(done -> {
            for (var name : new String[]{"service1", "service2"}) {
                long rotated = read(name).getLong(SECRETS_ROTATED);
                test.assertTrue(rotated >= before && rotated <= Instant.now().getEpochSecond());
            }
        }));
    }

    @Test
    public void retireRefusedInsideTheWindow(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            // just inside: the window is one tokenttl after the rotate.
            backdateRotation(TTL - 60);
            var rotated = read("service1");
            return refused(generate(RETIRE)).map(done -> {
                test.assertEquals(rotated, read("service1"));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void retireForcedInsideTheWindow(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE))
                .compose(v -> generator(RETIRE).setForce(true).all())
                .onComplete(test.asyncAssertSuccess(done -> {
                    test.assertNull(read("service1").getValue(LOCAL + PREVIOUS));
                    test.assertNull(read("service1").getValue(SECRETS_ROTATED));
                }));
    }

    @Test
    public void retireOfUnknownRotationTimeRefused(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            // rotated by an older version: no time.
            write("service1", read("service1").put(SECRETS_ROTATED, (Object) null));
            return refused(generate(RETIRE));
        }).compose(v -> generate(FILL)).map(v -> {
            // fill starts the window: the time is now.
            test.assertNotNull(read("service1").getLong(SECRETS_ROTATED));
            test.assertNotNull(read("service1").getString(LOCAL + PREVIOUS));
            return v;
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void rotateRefusedInsideTheWindowUnlessForced(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            var first = read("service1");
            return refused(generate(ROTATE)).compose(done -> {
                test.assertEquals(first, read("service1"));
                return generator(ROTATE).setForce(true).all();
            }).map(done -> {
                // only the newest previous value is kept.
                test.assertEquals(first.getString(LOCAL), read("service1").getString(LOCAL + PREVIOUS));
                return done;
            });
        }).compose(v -> {
            // after the window a rotate needs no force.
            backdateRotation(TTL + 1);
            return generate(ROTATE);
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void fillKeepsPreviousInsideTheWindow(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            backdateRotation(TTL - 60);
            var rotated = read("service1");
            return generate(FILL).map(done -> {
                test.assertEquals(rotated, read("service1"));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void fillRetiresAfterTheWindow(TestContext test) {
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            backdateRotation(TTL + 1);
            var rotated1 = read("service1");
            var rotated2 = read("service2");
            return generate(FILL).map(done -> {
                var filled1 = read("service1");
                for (var key : new String[]{LOCAL, SERVICE_1_SECRET, GLOBAL}) {
                    test.assertNull(filled1.getValue(key + PREVIOUS));
                    test.assertEquals(rotated1.getString(key), filled1.getString(key));
                }
                test.assertNull(filled1.getValue(SECRETS_ROTATED));
                test.assertNull(read("service2").getValue(GLOBAL + PREVIOUS));
                // the current values and the tokens are kept.
                test.assertEquals(rotated1.getJsonObject(SERVICE_1_TOKEN), filled1.getJsonObject(SERVICE_1_TOKEN));
                test.assertEquals(rotated2.getJsonObject(SERVICE_2_TOKEN), read("service2").getJsonObject(SERVICE_2_TOKEN));
                return done;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void serviceTokensLiveForTheServiceTokenttl(TestContext test) {
        long now = Instant.now().getEpochSecond();
        generate(FILL).compose(v -> {
            long expiry = read("service1").getJsonObject(SERVICE_1_TOKEN).getLong("expiry");
            test.assertTrue(expiry >= now + SERVICE_TTL && expiry <= now + SERVICE_TTL + 60);
            // in its last quarter: reissued; issued for longer than configured (an earlier tokenttl): reissued.
            return reissue("service1", SERVICE_1_TOKEN, "service2", LOCAL, now + SERVICE_TTL / 4 - 60)
                    .compose(x -> reissue("service2", SERVICE_2_TOKEN, "service1", SERVICE_1_SECRET, now + SERVICE_TTL * 12L))
                    .compose(x -> generate(FILL))
                    .compose(x -> {
                        var renewed1 = read("service1").getJsonObject(SERVICE_1_TOKEN);
                        var renewed2 = read("service2").getJsonObject(SERVICE_2_TOKEN);
                        // reissued for the service token lifetime from now (the same token when issued in the same second).
                        long renewed = Instant.now().getEpochSecond() + SERVICE_TTL;
                        test.assertTrue(renewed1.getLong("expiry") >= now + SERVICE_TTL);
                        test.assertTrue(renewed1.getLong("expiry") <= renewed);
                        test.assertTrue(renewed2.getLong("expiry") >= now + SERVICE_TTL);
                        test.assertTrue(renewed2.getLong("expiry") <= renewed);
                        return factory("service2", LOCAL).verify(token(renewed1));
                    });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void serviceTokensWithoutLifetimeNeverExpire(TestContext test) {
        Configurations.security().setServiceTokenttl(0);
        long now = Instant.now().getEpochSecond();

        // a token issued with a lifetime (an earlier setting) is replaced once by one that never expires.
        generate(FILL).compose(v -> reissue("service1", SERVICE_1_TOKEN, "service2", LOCAL, now + 60))
                .compose(v -> generate(FILL))
                .compose(v -> {
                    var never = read("service1").getJsonObject(SERVICE_1_TOKEN);
                    test.assertEquals(Token.NEVER, never.getLong("expiry"));
                    test.assertEquals(Token.NEVER, read("service2").getJsonObject(SERVICE_2_TOKEN).getLong("expiry"));
                    // kept: not reissued for age.
                    return generate(FILL).map(done -> {
                        test.assertEquals(never, read("service1").getJsonObject(SERVICE_1_TOKEN));
                        return never;
                    });
                }).compose(never -> {
                    // reissued when it no longer verifies with the issuer's current secret (a rotate).
                    return factory("service2", LOCAL).verify(token(never)).compose(v -> {
                        backdateRotation(0);
                        return generate(ROTATE);
                    }).map(done -> {
                        var rotated = read("service1").getJsonObject(SERVICE_1_TOKEN);
                        test.assertNotEquals(never.getString("key"), rotated.getString("key"));
                        test.assertEquals(Token.NEVER, rotated.getLong("expiry"));
                        return done;
                    });
                }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void revokeRejectsEveryOldTokenAtOnce(TestContext test) {
        String path = DIR + "/service2.json";
        // inside the retire window of a rotate: a revoke needs no force.
        generate(FILL).compose(v -> generate(ROTATE)).compose(v -> {
            var rotated = read("service2");
            var service1 = read("service1");
            // a node that loaded the rotated file, with a token factory from its startup.
            SecretRollover.register(path, rotated);
            var node = new TokenFactory(context, BASE64_DECODER.decode(rotated.getString(LOCAL)));
            var old = new Token("old");

            return node.hmac(old).compose(signed -> node.verify(old)).compose(accepted -> generate(REVOKE)).compose(done -> {
                var revoked = read("service2");
                for (var key : new String[]{LOCAL, GLOBAL}) {
                    test.assertNotEquals(rotated.getString(key), revoked.getString(key));
                    test.assertNull(revoked.getValue(key + PREVIOUS));
                }
                test.assertNull(revoked.getValue(SECRETS_ROTATED));
                test.assertNull(read("service1").getValue(SERVICE_1_SECRET + PREVIOUS));
                // the service tokens are reissued with the new secrets.
                test.assertNotEquals(service1.getJsonObject(SERVICE_1_TOKEN).getString("key"),
                        read("service1").getJsonObject(SERVICE_1_TOKEN).getString("key"));

                // the node reloads the file: the old token is rejected at once, a new one is signed with the new secret.
                SecretRollover.register(path, revoked);
                var fresh = new Token("new");
                return refused(node.verify(old), Throwable.class)
                        .compose(rejected -> node.hmac(fresh))
                        .compose(signed -> factory("service2", LOCAL).verify(fresh));
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void failedGenerateWritesNothing(TestContext test) {
        var security = Configurations.security();
        security.getDependencies().get("service1").addToken("broken", "service2", "missing-secret");
        refused(generate(FILL), com.codingchili.core.security.exception.SecurityMissingDependencyException.class)
                .onComplete(test.asyncAssertSuccess(done -> {
                    test.assertEquals(new JsonObject(), read("service1"));
                    test.assertEquals(new JsonObject(), read("service2"));
                }));
    }

    @Test
    public void filesAreNeverReadHalfWritten(TestContext test) throws Exception {
        // a reader of the files (a configuration reload) while the generator rewrites them, many times.
        var running = new AtomicBoolean(true);
        var reads = new AtomicInteger();
        var failures = new AtomicReference<Throwable>();
        var reader = new Thread(() -> {
            while (running.get()) {
                try {
                    read("service1");
                    read("service2");
                    reads.incrementAndGet();
                } catch (Throwable e) {
                    failures.compareAndSet(null, e);
                }
            }
        });
        reader.start();

        Future<Void> rotations = generate(FILL);
        for (int i = 0; i < 25; i++) {
            rotations = rotations.compose(v -> generator(ROTATE).setForce(true).all());
        }
        rotations.onComplete(test.asyncAssertSuccess(done -> {
            running.set(false);
            try {
                reader.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            test.assertNull(failures.get(), "a file was read while it was written: " + failures.get());
            test.assertTrue(reads.get() > 0);
        }));
    }

    @Test
    public void tokenExpiresIsInSeconds(TestContext test) {
        long now = Instant.now().getEpochSecond();
        var token = new Token("domain").expires(1, TimeUnit.HOURS);
        test.assertTrue(token.getExpiry() >= now + 3600 && token.getExpiry() <= now + 3601);
        // without an expiry: the security tokenttl, the lifetime of the tokens that services issue.
        test.assertTrue(new Token("domain").getExpiry() >= now + TTL);
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
        return generator(mode).all();
    }

    private AuthenticationGenerator generator(AuthenticationGenerator.Mode mode) {
        return new AuthenticationGenerator(context, DIR + DIR_ROOT).setMode(mode);
    }

    // succeeds when the generator refused (failed with the given exception).
    private static Future<Void> refused(Future<Void> generated) {
        return refused(generated, SecretRotationRefusedException.class);
    }

    private static Future<Void> refused(Future<Void> generated, Class<? extends Throwable> expected) {
        // a refusal is a failure of the expected type.
        return generated.transform(done -> (done.failed() && expected.isInstance(done.cause())) ?
                Future.succeededFuture() :
                Future.failedFuture("expected " + expected.getSimpleName() + ", was " + done.cause()));
    }

    // moves the time of the last rotate the given number of seconds back.
    private static void backdateRotation(long seconds) {
        for (var name : new String[]{"service1", "service2"}) {
            var config = read(name);
            if (config.containsKey(SECRETS_ROTATED)) {
                write(name, config.put(SECRETS_ROTATED, config.getLong(SECRETS_ROTATED) - seconds));
            }
        }
    }

    // replaces a token with one of the given expiry, signed by its issuer.
    private Future<Void> reissue(String file, String key, String issuer, String secret, long expiry) {
        var token = new Token(read(file).getJsonObject(key).getString("domain")).setExpiry(expiry);
        return factory(issuer, secret).hmac(token).map(signed -> {
            write(file, read(file).put(key, Serializer.json(token)));
            return null;
        });
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
