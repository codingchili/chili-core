package com.codingchili.core.security;

import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.exception.TokenException;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    private TokenFactory tokenFactory;
    private TokenFactory tokenFactory2;
    private byte[] secret = "the_secret".getBytes();
    private byte[] secret2 = "the_secret_2".getBytes();
    private String domain = "domain";
    private Long now = Instant.now().getEpochSecond();

    @Before
    public void setUp() {
        tokenFactory = new TokenFactory(secret);
        tokenFactory2 = new TokenFactory(secret2);
    }

    @Test
    public void createToken() throws TokenException {
        tokenFactory.sign(new Token()
                .setDomain(domain)
                .setExpiry(now));
    }

    @Test
    public void verifyValidToken() {
        Token token = new Token(tokenFactory, domain);
        Assert.assertTrue(tokenFactory.verifyToken(token));
    }

    @Test
    public void verifyTokenFromAnotherFactory() {
        tokenFactory2 = new TokenFactory(secret);
        Token token = new Token(tokenFactory, domain);

        Assert.assertTrue(tokenFactory2.verifyToken(token));
    }

    @Test
    public void failVerifyTokenFromAnotherFactoryWithAnotherKey() {
        Token token = new Token(tokenFactory, domain);

        Assert.assertFalse(tokenFactory2.verifyToken(token));
        Assert.assertTrue(tokenFactory.verifyToken(token));
    }

    @Test
    public void failVerifyTokenChangedDomain() {
        Token token = new Token(tokenFactory, domain);
        token.setDomain("domain2");

        Assert.assertFalse(tokenFactory.verifyToken(token));
    }

    @Test
    public void failVerifyOutDatedToken() throws TokenException {
        Token token = new Token(tokenFactory, domain).setExpiry(-10);
        Assert.assertFalse(tokenFactory.verifyToken(token));
    }

    @Test
    public void failVerifyNullToken() {
        Assert.assertFalse(tokenFactory.verifyToken(null));
    }

    @Test
    public void testVerifyTokenWithProperties(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.sign(token);
        test.assertTrue(tokenFactory.verifyToken(token));
    }

    @Test
    public void testVerifyFailTokenPropertiesModified(TestContext test) {
        Token token = getTokenWithProperties();
        tokenFactory.sign(token);
        test.assertTrue(tokenFactory.verifyToken(token));
        token.addProperty("roles", Arrays.asList("programmer", "root", "sysadmin"));
        test.assertFalse(tokenFactory.verifyToken(token));
    }

    @Test
    public void testSerializeToken() {
        Token token = getTokenWithProperties();
        token.addProperty("account", new Account().setUsername("robba"));
        tokenFactory.sign(token);
        Serializer.pack(Serializer.unpack(Serializer.pack(token), Token.class));
    }

    private Token getTokenWithProperties() {
        return new Token()
                .addProperty("version", 0)
                .addProperty("roles", Arrays.asList("programmer", "tester"))
                .setDomain("asd");
    }
}
