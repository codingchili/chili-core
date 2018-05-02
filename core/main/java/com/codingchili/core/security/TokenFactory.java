package com.codingchili.core.security;

import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import static com.codingchili.core.configuration.CoreStrings.ERROR_TOKEN_FACTORY;

/**
 * @author Robin Duda
 * <p>
 * Verifies and generates tokens for access.
 */
public class TokenFactory {
    private static final String CRYPTO_TYPE = "type";
    private static final String ALIAS = "alias";
    private final byte[] secret;
    private CoreContext core;
    private Logger logger;

    /**
     * @param core   the core context to execute on.
     * @param secret the secret to use to generate HMAC tokens, must not be null.
     */
    public TokenFactory(CoreContext core, byte[] secret) {
        Objects.requireNonNull(secret, "Cannot create TokenFactory with 'null' secret.");
        this.secret = secret;
        this.logger = core.logger(getClass());
        this.core = core;
    }

    /**
     * @param token the token to be verified.
     * @return true if the token is accepted.
     */
    public boolean verify(Token token) {
        // verify token not null and token is still valid.
        if (token != null && token.getExpiry() > Instant.now().getEpochSecond()) {
            if (token.getProperties().containsKey(CRYPTO_TYPE)) {
                String algorithm = token.getProperty(CRYPTO_TYPE);
                SecuritySettings security = Configurations.security();

                // don't trust the algorithm in the token - match existing algorithms only.
                if (algorithm.equals(security.getHmacAlgorithm())) {
                    return verifyHmac(token);
                } else if (algorithm.equals(security.getSignatureAlgorithm())) {
                    return verifySignature(token);
                } else {
                    logger.event("token.verify", Level.WARNING)
                            .send(String.format("Token algorithm '%s' - not enabled/trusted.", algorithm));
                    return false;
                }
                // only log an error if the token is secured and type is missing.
            } else if (token.getKey() != null && !token.getKey().isEmpty()) {
                logger.event("token.verify", Level.WARNING)
                        .send(String.format("Token is missing property '%s' - unable to verify.", CRYPTO_TYPE));
            }
        }
        return false;
    }

    private boolean verifyHmac(Token token) {
        try {
            byte[] result = Base64.getEncoder().encode(hmacKey(token));
            return ByteComparator.compare(result, token.getKey().getBytes());
        } catch (Exception e) {
            logger.onError(e);
            return false;
        }
    }

    private byte[] hmacKey(Token token) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = Configurations.security().getHmacAlgorithm();
        Mac mac = Mac.getInstance(algorithm);

        SecretKeySpec spec = new SecretKeySpec(secret, algorithm);
        mac.init(spec);
        canonicalizeTokenWithCrypto(token, mac::update);

        return mac.doFinal();
    }

    /**
     * Signs the given token using HMAC.
     *
     * @param token the token to sign, sets the key of this token.
     */
    public void hmac(Token token) {
        try {
            token.addProperty(CRYPTO_TYPE, Configurations.security().getHmacAlgorithm());
            token.setKey(Base64.getEncoder().encodeToString(hmacKey(token)));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(ERROR_TOKEN_FACTORY);
        }
    }

    /**
     * Signs the given token using the private key in the named JKS. If the JKS does not
     * exist an error will be thrown.
     *
     * @param token    the token to be signed.
     * @param keystore the keystore that contains the private key to use for signing.
     */
    public void sign(Token token, String keystore) {
        byte[] key = signedKey(token, keystore);
        token.setKey(Base64.getEncoder().encodeToString(key));
    }

    private byte[] signedKey(Token token, String keystore) {
        try {
            TrustAndKeyProvider provider = Configurations.security().getKeystore(core, keystore);
            Signature signature = Signature.getInstance(Configurations.security().getSignatureAlgorithm());
            signature.initSign(provider.getPrivateKey());

            token.addProperty(CRYPTO_TYPE, Configurations.security().getSignatureAlgorithm());
            token.addProperty(ALIAS, keystore);
            canonicalizeTokenWithCrypto(token, signature::update);

            return signature.sign();
        } catch (Exception e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean verifySignature(Token token) {
        String alias = token.getProperty(ALIAS);

        if (alias == null) {
            logger.event("token.verify", Level.WARNING)
                    .send(String.format("token is missing property '%s' - unable to verify.", ALIAS));
        } else {
            TrustAndKeyProvider provider = Configurations.security().getKeystore(core, alias);
            try {
                Signature signature = Signature.getInstance(Configurations.security().getSignatureAlgorithm());
                signature.initVerify(provider.getPublicKey());
                canonicalizeTokenWithCrypto(token, signature::update);
                return signature.verify(Base64.getDecoder().decode(token.getKey()));
            } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
                logger.onError(e);
                return false;
            }
        }
        return false;
    }

    /**
     * Serializes a token and its properties and calls the given crypto function.
     * All data included in the canonicalization is secured.
     *
     * @param token    the token to canonicalize and process with a crypto function.
     * @param function the crypto function to apply to the serialized token parts.
     */
    private void canonicalizeTokenWithCrypto(Token token, CryptoFunction function) {
        try {
            function.update(Serializer.buffer(token.getProperties()).getBytes());
            function.update(token.getDomain().getBytes());
            function.update((token.getExpiry() + "").getBytes());
        } catch (SignatureException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @FunctionalInterface
    private interface CryptoFunction {
        /**
         * processes a data part with a crypto function - this could be a HMAC or signature.
         *
         * @param data the data to be processed.
         */
        void update(byte[] data) throws SignatureException;

    }
}