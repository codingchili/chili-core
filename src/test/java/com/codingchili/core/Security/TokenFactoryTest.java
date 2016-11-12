package com.codingchili.core.Security;

import org.junit.*;

import java.time.Instant;

import com.codingchili.core.Exception.TokenException;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the Token Factory
 */
public class TokenFactoryTest {
    private TokenFactory tokenFactory;
    private TokenFactory tokenFactory2;
    private byte[] secret = "the_secret".getBytes();
    private byte[] secret2 = "the_secret_2".getBytes();
    private String domain = "domain";
    private String domain2 = "domain2";
    private Long now = Instant.now().getEpochSecond();

    @Before
    public void setUp() {
        tokenFactory = new TokenFactory(secret);
        tokenFactory2 = new TokenFactory(secret2);
    }

    @Test
    public void createToken() throws TokenException {
        tokenFactory.signToken(domain, now);
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
        token.setDomain(domain2);

        Assert.assertFalse(tokenFactory.verifyToken(token));
    }

    @Test
    public void failVerifyOutDatedToken() throws TokenException {
        String key = tokenFactory.signToken(domain, now - 10);
        Token token = new Token()
                .setDomain(domain)
                .setKey(key)
                .setExpiry(now - 10);

        Assert.assertFalse(tokenFactory.verifyToken(token));
    }

    @Test
    public void failVerifyNullToken() {
        Assert.assertFalse(tokenFactory.verifyToken(null));
    }
}
