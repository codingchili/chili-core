package com.codingchili.core.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import com.codingchili.core.security.exception.TokenException;

/**
 * @author Robin Duda
 *
 * Verifies and generates tokens for access.
 */
public class TokenFactory {
    private final byte[] secret;
    private static final String ALGORITHM = "HmacSHA512";

    public TokenFactory(byte[] secret) {
        this.secret = secret;
    }

    /**
     * @param token Containing token data.
     * @return true if the token is accepted.
     * @see #verifyToken(String token, String username, Long expiry)
     */
    public boolean verifyToken(Token token) {
        return token != null && (verifyToken(token.getKey(), token.getDomain(), token.getExpiry()));
    }

    /**
     * Checks if a token and its parameters is valid against the secret.
     *
     * @param key hex encoded token to be verified.
     * @param domain context name of the requestor.
     * @param expiry the unix epoch time in which it is expired.
     * @return true if the token is accepted.
     */
    private boolean verifyToken(String key, String domain, Long expiry) {
        if (expiry > Instant.now().getEpochSecond()) {
            try {
                byte[] result = Base64.getEncoder().encode(generateToken(domain, expiry));

                return ByteComparator.compare(result, key.getBytes());
            } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            }
        }
        return false;
    }

    private byte[] generateToken(String domain, Long expiry) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);

        SecretKeySpec key = new SecretKeySpec(secret, ALGORITHM);
        mac.init(key);

        mac.update(domain.getBytes());
        mac.update(expiry.toString().getBytes());

        return mac.doFinal();
    }

    /**
     * Generates a new token from a given username.. be careful..
     *
     * @param domain the token should be signed with.
     * @param expiry indicates when the token expires.
     * @return a signed token as a base64 string.
     */
    String signToken(String domain, long expiry) throws TokenException {
        try {
            return Base64.getEncoder().encodeToString(generateToken(domain, expiry));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new TokenException();
        }
    }
}