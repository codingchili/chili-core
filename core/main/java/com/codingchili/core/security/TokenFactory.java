package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import static com.codingchili.core.configuration.CoreStrings.*;
import static io.vertx.core.json.impl.JsonUtil.*;

/**
 * Verifies and generates tokens for access. A factory works in one of two modes:
 * <ul>
 * <li>HMAC ({@link #TokenFactory(CoreContext, byte[])}): a shared secret signs ({@link #hmac(Token)}) and verifies.
 * Anyone who can verify can also sign: for tokens between services that trust each other.</li>
 * <li>key pair ({@link #signer(CoreContext, byte[], byte[])}, {@link #verifier(CoreContext, byte[])}): a private key
 * signs ({@link #sign(Token)}) with {@link SecuritySettings#getKeyPairAlgorithm()} (Ed25519) and the public key
 * verifies. A verifier holds only the public key: it can never sign, so a service that verifies the tokens of
 * another cannot forge them.</li>
 * </ul>
 * Tokens signed with a keystore ({@link #sign(Token, String)}) are verified by any factory.
 */
public class TokenFactory {
    private static final String CRYPTO_TYPE = "type";
    private static final String ALIAS = "alias";
    private final byte[] secret;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final CoreContext core;
    private final ObjectMapper mapper = Serializer.json
            .rebuild()
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .build();

    /**
     * Creates a factory for HMAC tokens.
     *
     * @param core   the core context to run async operations on.
     * @param secret the secret to use to generate HMAC tokens, must not be null.
     */
    public TokenFactory(CoreContext core, byte[] secret) {
        Objects.requireNonNull(secret, "Cannot create TokenFactory with 'null' secret.");
        this.secret = secret;
        this.privateKey = null;
        this.publicKey = null;
        this.core = core;
    }

    private TokenFactory(CoreContext core, PrivateKey privateKey, PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "Cannot create a key pair TokenFactory without a public key.");
        this.secret = null;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.core = core;
    }

    /**
     * Creates a factory that signs tokens with a private key and verifies them with its public key.
     *
     * @param core       the core context to run async operations on.
     * @param privateKey the private key, PKCS#8 encoded (as {@link SecretFactory#keyPair()} and the generator
     *                   write it, base64 decoded).
     * @param publicKey  the public key of the pair, X.509 encoded.
     * @return a factory that signs with {@link #sign(Token)} and verifies.
     */
    public static TokenFactory signer(CoreContext core, byte[] privateKey, byte[] publicKey) {
        Objects.requireNonNull(privateKey, "Cannot create a signing TokenFactory with a 'null' private key.");
        try {
            KeyFactory keys = KeyFactory.getInstance(Configurations.security().getKeyPairAlgorithm());
            return new TokenFactory(core,
                    keys.generatePrivate(new PKCS8EncodedKeySpec(privateKey)),
                    keys.generatePublic(new X509EncodedKeySpec(publicKey)));
        } catch (GeneralSecurityException e) {
            throw new CoreRuntimeException(getInvalidKey(e.getMessage()), e);
        }
    }

    /**
     * Creates a factory that only verifies tokens signed with the private key of the given public key: it cannot
     * sign ({@link #sign(Token)} and {@link #hmac(Token)} fail).
     *
     * @param core      the core context to run async operations on.
     * @param publicKey the public key, X.509 encoded (base64 decoded).
     * @return a factory that verifies only.
     */
    public static TokenFactory verifier(CoreContext core, byte[] publicKey) {
        Objects.requireNonNull(publicKey, "Cannot create a verifying TokenFactory with a 'null' public key.");
        try {
            KeyFactory keys = KeyFactory.getInstance(Configurations.security().getKeyPairAlgorithm());
            return new TokenFactory(core, null, keys.generatePublic(new X509EncodedKeySpec(publicKey)));
        } catch (GeneralSecurityException e) {
            throw new CoreRuntimeException(getInvalidKey(e.getMessage()), e);
        }
    }

    /**
     * @param text a key as written in a configuration file: base64, url safe or standard.
     * @return the decoded key.
     */
    public static byte[] decodeKey(String text) {
        try {
            return BASE64_DECODER.decode(text);
        } catch (IllegalArgumentException e) {
            return Base64.getDecoder().decode(text);
        }
    }

    /**
     * @return true if this factory can sign tokens: it has a secret (HMAC) or a private key.
     */
    public boolean canSign() {
        return secret != null || privateKey != null;
    }

    /**
     * Verifies the validity of the given token.
     *
     * @param token the token to be verified.
     * @return true if the token is accepted.
     */
    public Future<Void> verify(Token token) {
        // verify token not null and token is still valid.
        if (token != null && token.getExpiry() > Instant.now().getEpochSecond()) {
            if (token.getProperties().containsKey(CRYPTO_TYPE)) {
                String algorithm = token.getProperty(CRYPTO_TYPE);
                SecuritySettings security = Configurations.security();

                // don't trust the algorithm in the token - match existing algorithms only, and only the mode
                // of this factory: a key pair factory never accepts an HMAC token and the other way around.
                if (algorithm.equals(security.getHmacAlgorithm()) && secret != null) {
                    return verifyHmac(token);
                } else if (algorithm.equals(security.getKeyPairAlgorithm()) && publicKey != null) {
                    return verifyKeyPair(token);
                } else if (algorithm.equals(security.getSignatureAlgorithm())) {
                    return verifySignature(token);
                } else {
                    return Future.failedFuture(
                            String.format("Token algorithm '%s' - not enabled/trusted.", algorithm));
                }
                // only log an error if the token is secured and type is missing.
            } else if (token.getKey() != null && !token.getKey().isEmpty()) {
                return Future.failedFuture(
                        String.format("Token is missing property '%s' - unable to verify.", CRYPTO_TYPE));
            }
        }
        return Future.failedFuture("Token is not valid.");
    }

    private Future<Void> verifyHmac(Token token) {
        return core.blocking(() -> {
            try {
                byte[] result = BASE64_ENCODER.encode(hmacKey(token));
                if (!ByteComparator.compare(result, token.getKey().getBytes())) {
                    throw new CoreRuntimeException("Failed to verify HMAC token.");
                }
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new CoreRuntimeException(e);
            }
        });
    }

    private Future<Void> verifyKeyPair(Token token) {
        return core.blocking(() -> {
            try {
                Signature signature = Signature.getInstance(Configurations.security().getKeyPairAlgorithm());
                signature.initVerify(publicKey);
                canonicalizeTokenWithCrypto(token, signature::update);
                if (!signature.verify(decodeKey(token.getKey()))) {
                    throw new CoreRuntimeException("Failed to verify the signature of the token.");
                }
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                throw new CoreRuntimeException("Failed to verify the signature of the token: " + e.getMessage());
            }
        });
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
     * @return callback, failed when this factory has no secret (a key pair factory).
     */
    public Future<Void> hmac(Token token) {
        if (secret == null) {
            return Future.failedFuture(new CoreRuntimeException(getTokenFactoryNoSecret()));
        }
        return core.blocking(() -> {
            try {
                token.addProperty(CRYPTO_TYPE, Configurations.security().getHmacAlgorithm());
                token.setKey(BASE64_ENCODER.encodeToString(hmacKey(token)));
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new CoreRuntimeException(ERROR_TOKEN_FACTORY);
            }
        });
    }

    /**
     * Signs the given token with the private key of this factory.
     *
     * @param token the token to sign, sets the key of this token.
     * @return callback, failed when the factory has no private key: a verifier (only the public key) or an HMAC
     * factory.
     */
    public Future<Void> sign(Token token) {
        if (privateKey == null) {
            return Future.failedFuture(new CoreRuntimeException(getTokenFactoryVerifyOnly()));
        }
        return core.blocking(() -> {
            try {
                Signature signature = Signature.getInstance(Configurations.security().getKeyPairAlgorithm());
                signature.initSign(privateKey);
                token.addProperty(CRYPTO_TYPE, Configurations.security().getKeyPairAlgorithm());
                canonicalizeTokenWithCrypto(token, signature::update);
                token.setKey(BASE64_ENCODER.encodeToString(signature.sign()));
            } catch (GeneralSecurityException e) {
                throw new CoreRuntimeException(ERROR_TOKEN_FACTORY);
            }
        });
    }

    /**
     * Signs the given token using the private key in the named JKS. If the JKS does not
     * exist an error will be thrown.
     *
     * @param token    the token to be signed.
     * @param keystore the keystore that contains the private key to use for signing.
     * @return callback
     */
    public Future<Void> sign(Token token, String keystore) {
        return core.blocking(() -> {
            try {
                byte[] key = signedKey(token, keystore);
                token.setKey(BASE64_ENCODER.encodeToString(key));
            } catch (Throwable e) {
                throw new CoreRuntimeException(e);
            }
        });
    }

    private byte[] signedKey(Token token, String keystore) {
        try {
            TrustAndKeyProvider provider = Configurations.security().getKeystore(keystore);
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

    private Future<Void> verifySignature(Token token) {
        Promise<Void> promise = Promise.promise();
        String alias = token.getProperty(ALIAS);

        if (alias == null) {
            promise.fail(String.format("token is missing property '%s' - unable to verify.", ALIAS));
        } else {
            core.blocking(() -> {
                TrustAndKeyProvider provider = Configurations.security().getKeystore(alias);
                try {
                    Signature signature = Signature.getInstance(Configurations.security().getSignatureAlgorithm());
                    signature.initVerify(provider.getPublicKey());
                    canonicalizeTokenWithCrypto(token, signature::update);
                    if (!signature.verify(BASE64_DECODER.decode(token.getKey()))) {
                        throw new SignatureException("Failed to verify token signature.");
                    }
                } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new CoreRuntimeException(e);
                }
            }).onComplete(promise::handle);
        }
        return promise.future();
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
            function.update(mapper.writeValueAsBytes(token.getProperties()));
            function.update(token.getDomain().getBytes());
            function.update(".".getBytes());
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
