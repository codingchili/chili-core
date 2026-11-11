package com.codingchili.core.security;

import com.codingchili.core.configuration.system.SecuritySettingsTest;
import com.codingchili.core.files.Configurations;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.Arrays;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;

/**
 * Tests for the Token Factory
 */
@RunWith(VertxUnitRunner.class)
public class TokenFactoryTest {
    private static final String TEST_KEYSTORE = "test_key.jks";
    private static CoreContext context;
    private static TokenFactory tokenFactory;
    private static TokenFactory tokenFactory2;
    private static byte[] secret = "the_secret".getBytes();
    private static byte[] secret2 = "the_secret_2".getBytes();
    private static String domain = "domain";
    private Long now = Instant.now().getEpochSecond();

    @BeforeClass
    public static void setUp() {
        context = new SystemContext();
        tokenFactory = new TokenFactory(context, secret);
        tokenFactory2 = new TokenFactory(context, secret2);
        Configurations.security()
                .addKeystore()
                .setPath(SecuritySettingsTest.KEYSTORE_JKS)
                .setPassword(SecuritySettingsTest.PWD)
                .build()
                .save();
    }

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void createToken(TestContext test) {
        Async async = test.async();
        tokenFactory.hmac(new Token()
                .setDomain(domain)
                .setExpiry(now)).onComplete(done -> {
                    test.assertTrue(done.succeeded());
                    async.complete();
        });
    }


    @Test
    public void testVerifyTokenWithProperties(TestContext test) {
        Async async = test.async();
        TokenFactory another = new TokenFactory(context, secret);
        Token token = getTokenWithProperties();
        tokenFactory.hmac(token).onComplete(hmac -> {
            test.assertTrue(hmac.succeeded());

            // test verifying with another factory with the same secret.
            another.verify(token).onComplete(done -> {
                test.assertTrue(done.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void failVerifyTokenFromAnotherFactoryWithAnotherKey(TestContext test) {
        Token token = new Token(domain);
        Async async = test.async();

        tokenFactory.hmac(token).onComplete(hmac -> {
            test.assertTrue(hmac.succeeded());

            tokenFactory.verify(token).onComplete(ok -> {
                test.assertTrue(ok.succeeded());

                tokenFactory2.verify(token).onComplete(fail -> {
                    test.assertTrue(fail.failed());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void failVerifyTokenChangedDomain(TestContext test) {
        Async async = test.async();
        Token token = new Token(domain);

        tokenFactory.hmac(token).onComplete(hmac -> {
            test.assertTrue(hmac.succeeded());
            token.setDomain("domain2");

            tokenFactory.verify(token).onComplete(verify -> {
                test.assertTrue(verify.failed());
                async.complete();
            });
        });
    }

    @Test
    public void failVerifyOutDatedToken(TestContext test) {
        Async async = test.async();
        Token token = new Token(domain).setExpiry(-10);

        tokenFactory.hmac(token).onComplete(hmac -> {
            tokenFactory.verify(token).onComplete(verify -> {
                test.assertTrue(verify.failed());
                async.complete();
            });
        });
    }

    @Test
    public void failVerifyNullToken(TestContext test) {
        Async async = test.async();
        tokenFactory.verify(null).onComplete(done -> {
            test.assertTrue(done.failed());
            async.complete();
        });
    }

    @Test
    public void testVerifyFailTokenPropertiesModified(TestContext test) {
        Async async = test.async();
        Token token = getTokenWithProperties();
        tokenFactory.hmac(token).onComplete(hmac -> {
            test.assertTrue(hmac.succeeded());

            tokenFactory.verify(token).onComplete(verify -> {
                test.assertTrue(verify.succeeded());
                token.addProperty("roles", Arrays.asList("programmer", "root", "sysadmin"));

                tokenFactory.verify(token).onComplete(done2 -> {
                    test.assertFalse(done2.succeeded());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testSerializeToken(TestContext test) {
        Async async = test.async();
        Token token = getTokenWithProperties();
        token.addProperty("account", new Account().setUsername("robba"));
        tokenFactory.hmac(token).onComplete(hmac -> {
            test.assertTrue(hmac.succeeded());
            Serializer.pack(Serializer.unpack(Serializer.pack(token), Token.class));
            async.complete();
        });
    }

    @Test
    public void generateSignedToken(TestContext test) {
        Async async = test.async();
        Token token = getTokenWithProperties();
        tokenFactory.sign(token, TEST_KEYSTORE).onComplete(sign -> {
           test.assertNotNull(token.getKey());
           test.assertTrue(sign.succeeded());
           async.complete();
        });
    }

    @Test
    public void verifySignedToken(TestContext test) {
        Async async = test.async();
        Token token = getTokenWithProperties();

        tokenFactory.sign(token, TEST_KEYSTORE).onComplete(sign -> {
            test.assertTrue(sign.succeeded());
            test.assertNotNull(token.getKey());

            tokenFactory.verify(token).onComplete(verify -> {
                test.assertTrue(verify.succeeded());
                async.complete();
            });
        });
    }

    private Token getTokenWithProperties() {
        return new Token()
                .addProperty("version", 0)
                .addProperty("roles", Arrays.asList("programmer", "tester"))
                .setDomain("asd");
    }

    // key pair mode: the private key signs, a verifier with only the public key verifies and cannot sign.
    private static TokenFactory[] keyPair() {
        var pair = SecretFactory.keyPair();
        byte[] privateKey = pair.getPrivate().getEncoded();
        byte[] publicKey = pair.getPublic().getEncoded();
        return new TokenFactory[]{
                TokenFactory.signer(context, privateKey, publicKey), TokenFactory.verifier(context, publicKey)};
    }

    @Test
    public void keyPairSignedTokenVerifiesWithThePublicKey(TestContext test) {
        var factories = keyPair();
        var token = new Token(domain).addProperty("admin", false);
        factories[0].sign(token)
                .compose(signed -> {
                    test.assertEquals("Ed25519", token.getProperty("type"));
                    return factories[1].verify(Serializer.unpack(Serializer.json(token), Token.class));
                })
                .compose(verified -> factories[0].verify(token))
                .onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void keyPairVerifierCannotSign(TestContext test) {
        var verifier = keyPair()[1];
        test.assertFalse(verifier.canSign());
        verifier.sign(new Token(domain)).onComplete(test.asyncAssertFailure(e -> {
            test.assertTrue(e.getMessage().contains("cannot sign"), e.getMessage());
            verifier.hmac(new Token(domain)).onComplete(test.asyncAssertFailure());
        }));
    }

    @Test
    public void keyPairOtherKeyRejected(TestContext test) {
        var token = new Token(domain);
        keyPair()[0].sign(token).onComplete(test.asyncAssertSuccess(signed ->
                keyPair()[1].verify(token).onComplete(test.asyncAssertFailure())));
    }

    @Test
    public void keyPairTamperedTokenRejected(TestContext test) {
        var factories = keyPair();
        var token = new Token(domain).addProperty("admin", false);
        factories[0].sign(token).onComplete(test.asyncAssertSuccess(signed -> {
            var domainChanged = Serializer.unpack(Serializer.json(token), Token.class).setDomain("other");
            var propertyChanged = Serializer.unpack(Serializer.json(token), Token.class).addProperty("admin", true);
            var expiryChanged = Serializer.unpack(Serializer.json(token), Token.class).setExpiry(token.getExpiry() + 1);
            factories[1].verify(domainChanged).onComplete(test.asyncAssertFailure(e1 ->
                    factories[1].verify(propertyChanged).onComplete(test.asyncAssertFailure(e2 ->
                            factories[1].verify(expiryChanged).onComplete(test.asyncAssertFailure())))));
        }));
    }

    @Test
    public void keyPairExpiredTokenRejected(TestContext test) {
        var factories = keyPair();
        var token = new Token(domain).setExpiry(Instant.now().getEpochSecond() - 1);
        factories[0].sign(token).onComplete(test.asyncAssertSuccess(signed ->
                factories[1].verify(token).onComplete(test.asyncAssertFailure())));
    }

    @Test
    public void keyPairAndHmacModesDoNotMix(TestContext test) {
        var factories = keyPair();
        var hmac = new Token(domain);
        var signed = new Token(domain);
        // an HMAC token is not accepted by a verifier, a key pair token not by an HMAC factory.
        tokenFactory.hmac(hmac)
                .compose(v -> factories[0].sign(signed))
                .onComplete(test.asyncAssertSuccess(v ->
                        factories[1].verify(hmac).onComplete(test.asyncAssertFailure(e ->
                                tokenFactory.verify(signed).onComplete(test.asyncAssertFailure())))));
    }
}
