package com.codingchili.core.security;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.Arrays;

/**
 * @author Robin Duda
 * <p>
 * Tests for the Token Factory
 */
@RunWith(VertxUnitRunner.class)
public class TokenFactoryTest {
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
        tokenFactory = context.tokens(secret);
        tokenFactory2 = context.tokens(secret2);
    }

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void createToken() {
        tokenFactory.hmac(new Token()
                .setDomain(domain)
                .setExpiry(now));
    }

    @Test
    public void verifyValidToken() {
        Token token = new Token(tokenFactory, domain);
        Assert.assertTrue(tokenFactory.verify(token));
    }

    @Test
    public void verifyTokenFromAnotherFactory() {
        TokenFactory factory = context.tokens(secret2);
        Token token = new Token(factory, domain);

        Assert.assertTrue(tokenFactory2.verify(token));
    }

    @Test
    public void failVerifyTokenFromAnotherFactoryWithAnotherKey() {
        Token token = new Token(tokenFactory, domain);

        Assert.assertTrue(tokenFactory.verify(token));
        Assert.assertFalse(tokenFactory2.verify(token));
    }

    @Test
    public void failVerifyTokenChangedDomain() {
        Token token = new Token(tokenFactory, domain);
        token.setDomain("domain2");

        Assert.assertFalse(tokenFactory.verify(token));
    }

    @Test
    public void failVerifyOutDatedToken() {
        Token token = new Token(tokenFactory, domain).setExpiry(-10);
        Assert.assertFalse(tokenFactory.verify(token));
    }

    @Test
    public void failVerifyNullToken() {
        Assert.assertFalse(tokenFactory.verify(null));
    }

    @Test
    public void testVerifyTokenWithProperties(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.hmac(token);
        test.assertTrue(tokenFactory.verify(token));
    }

    @Test
    public void testVerifyFailTokenPropertiesModified(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.hmac(token);
        test.assertTrue(tokenFactory.verify(token));
        token.addProperty("roles", Arrays.asList("programmer", "root", "sysadmin"));
        test.assertFalse(tokenFactory.verify(token));
    }

    @Test
    public void testSerializeToken() {
        Token token = getTokenWithProperties();
        token.addProperty("account", new Account().setUsername("robba"));
        tokenFactory.hmac(token);
        Serializer.pack(Serializer.unpack(Serializer.pack(token), Token.class));
    }

    @Test
    public void generateSignedToken(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.sign(token, "test_key.jks");
        test.assertNotNull(token.getKey());
    }

    @Test
    public void verifySignedToken(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.sign(token, "test_key.jks");
        test.assertNotNull(token.getKey());
        test.assertTrue(tokenFactory.verify(token));
    }

    private Token getTokenWithProperties() {
        return new Token()
                .addProperty("version", 0)
                .addProperty("roles", Arrays.asList("programmer", "tester"))
                .setDomain("asd");
    }
}
