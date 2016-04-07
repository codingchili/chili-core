package Utilities;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * @author Robin Duda
 * <p>
 * Verifies and generates tokens for access.
 */
public class TokenFactory {
    private byte[] secret;
    private static final String ALGORITHM = "HmacSHA512";

    public TokenFactory(byte[] secret) {
        this.secret = secret;
    }

    /**
     * Checks if a token and its parameters is valid against the secret.
     *
     * @param token    hex encoded token to be verified.
     * @param domain   context name of the requestor.
     * @param expiry   the unix epoch time in which it is expired.
     * @return true if the token is accepted.
     */
    public boolean verifyToken(String token, String domain, Long expiry) {
        if (expiry > Instant.now().getEpochSecond()) {
            try {
                byte[] result = DatatypeConverter.printHexBinary(generateToken(domain, expiry)).getBytes();

                return constantTimeCompare(result, token.toUpperCase().getBytes());
            } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            }
        }
        return false;
    }

    /**
     * @param token Containing token data.
     * @return true if the token is accepted.
     * @see #verifyToken(String token, String username, Long expiry)
     */
    public boolean verifyToken(Token token) {
        return (verifyToken(token.getKey(), token.getDomain(), token.getExpiry()));
    }

    private byte[] generateToken(String username, Long expiry) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);

        SecretKeySpec key = new SecretKeySpec(secret, ALGORITHM);
        mac.init(key);

        mac.update(username.getBytes());
        mac.update(expiry.toString().getBytes());

        return mac.doFinal();
    }

    /**
     * Generates a new token from a given username.. be careful..
     *
     * @param domain   the token should be signed with.
     * @param expiry   indicates when the token expires.
     * @return a signed token as a base64 string.
     */
    public String signToken(String domain, long expiry) throws TokenException {
        try {
            return DatatypeConverter.printHexBinary(generateToken(domain, expiry));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new TokenException();
        }
    }

    private boolean constantTimeCompare(byte[] first, byte[] second) {
        int result = 0;

        if (first.length != second.length)
            return false;

        for (int i = 0; i < first.length; i++)
            result |= (first[i] ^ second[i]);

        return result == 0;
    }
}