package com.codingchili.core.security;

import com.codingchili.core.protocol.Serializer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import static com.codingchili.core.configuration.CoreStrings.ERROR_TOKEN_FACTORY;

/**
 * @author Robin Duda
 * <p>
 * Verifies and generates tokens for access.
 */
public class    TokenFactory {
    private static final String ALGORITHM = "HmacSHA512";
    private final byte[] secret;

    public TokenFactory(byte[] secret) {
        this.secret = secret;
    }

    /**
     * @param token Containing token data.
     * @return true if the token is accepted.
     * @see #verifyToken(String token, Token token)
     */
    public boolean verifyToken(Token token) {
        return token != null && (verifyToken(token.getKey(), token));
    }

    /**
     * Checks if a token and its parameters is valid against the secret.
     *
     * @param key   hex encoded token to be verified.
     * @param token the token to be verified
     * @return true if the token is accepted.
     */
    private boolean verifyToken(String key, Token token) {
        if (token.getExpiry() > Instant.now().getEpochSecond()) {
            try {
                byte[] result = Base64.getEncoder().encode(generateKey(token));

                return ByteComparator.compare(result, key.getBytes());
            } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            }
        }
        return false;
    }

    private byte[] generateKey(Token token) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);

        SecretKeySpec spec = new SecretKeySpec(secret, ALGORITHM);
        mac.init(spec);

        // encoding to json object here to avoid byte differing byte representations.
        mac.update(Serializer.buffer(token.getProperties()).getBytes());
        mac.update(token.getDomain().getBytes());
        mac.update((token.getExpiry() + "").getBytes());

        return mac.doFinal();
    }

    /**
     * Signs the given token using HMAC.
     *
     * @param token the token to sign, sets the key of this token.
     */
    public void sign(Token token) {
        try {
            token.setKey(Base64.getEncoder().encodeToString(generateKey(token)));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(ERROR_TOKEN_FACTORY);
        }
    }
}