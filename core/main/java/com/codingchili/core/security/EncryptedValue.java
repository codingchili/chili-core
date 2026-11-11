package com.codingchili.core.security;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.ByteArrayOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.function.Supplier;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.security.exception.EncryptedValueException;
import com.codingchili.core.security.exception.EncryptedValueException.Reason;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * A configuration value encrypted to the keys of one or more nodes ({@link NodeKey}): {@code enc:v1:<base64>}.
 * Opt-in per value: a configuration value that starts with {@code enc:} is decrypted when the configuration is
 * loaded, any other value is used as it is.
 * <p>
 * Format v1 (JDK algorithms only): a random 32 byte data key encrypts the value (ChaCha20-Poly1305), the data key is
 * wrapped once per recipient, so that any one of them can open the value. The wrapping key of a recipient is
 * HKDF-SHA256 (salt: the ephemeral public key and the recipient's public key) of the X25519 agreement of a key pair
 * made for this value (ephemeral) with the recipient's public key. The binary layout, base64 encoded:
 * <pre>
 * version         1 byte, 1
 * ephemeral key  32 bytes, X25519 public key
 * recipients      1 byte, n (1..255)
 * n times:        8 bytes key id (the first 8 bytes of the SHA-256 of the recipient's public key),
 *                48 bytes wrapped data key (ChaCha20-Poly1305 with the wrapping key, nonce 0, the key id as AAD)
 * nonce          12 bytes, random
 * ciphertext      the value as JSON ({"v": value}) and the 16 byte tag (ChaCha20-Poly1305 with the data key,
 *                 everything before the nonce as AAD)
 * </pre>
 * Any change of the bytes fails the authentication of the data key or the value: a tampered value is never used.
 */
public final class EncryptedValue {
    /**
     * the prefix of every encrypted value.
     */
    public static final String PREFIX = "enc:";
    /**
     * the prefix of an encrypted value of format version 1.
     */
    public static final String PREFIX_V1 = PREFIX + "v1:";
    static final int ID_SIZE = 8;
    private static final byte VERSION = 1;
    private static final int KEY_SIZE = 32;
    private static final int TAG_SIZE = 16;
    private static final int NONCE_SIZE = 12;
    private static final int ENTRY_SIZE = ID_SIZE + KEY_SIZE + TAG_SIZE;
    private static final int MAX_RECIPIENTS = 255;
    private static final String CIPHER = "ChaCha20-Poly1305";
    private static final String AGREEMENT = "XDH";
    private static final String HMAC = "HmacSHA256";
    private static final String VALUE = "v";
    private static final byte[] WRAP_INFO = "chili-core enc v1 data key".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom random = new SecureRandom();

    private EncryptedValue() {
    }

    /**
     * @param value a configuration value.
     * @return true when the value is an encrypted value (a string with the prefix {@code enc:}).
     */
    public static boolean isEncrypted(Object value) {
        return value instanceof String text && text.startsWith(PREFIX);
    }

    /**
     * Encrypts a configuration value to the given keys: each of them can open it.
     *
     * @param value      a JSON value: a string, number, boolean, JsonObject or JsonArray.
     * @param recipients the public keys of the nodes that may open the value, at least one.
     * @return the encrypted value, {@code enc:v1:<base64>}.
     */
    public static String encrypt(Object value, Collection<NodeKey.Public> recipients) {
        var keys = new ArrayList<>(new LinkedHashSet<>(recipients));
        if (keys.isEmpty() || keys.size() > MAX_RECIPIENTS) {
            throw new IllegalArgumentException("an encrypted value needs 1 to 255 recipients, not " + keys.size());
        }
        try {
            KeyPair ephemeral = KeyPairGenerator.getInstance("X25519").generateKeyPair();
            NodeKey.Public ephemeralPublic = NodeKey.Public.of(ephemeral.getPublic());
            byte[] dataKey = new byte[KEY_SIZE];
            byte[] nonce = new byte[NONCE_SIZE];
            random.nextBytes(dataKey);
            random.nextBytes(nonce);

            var header = new ByteArrayOutputStream();
            header.write(VERSION);
            header.write(ephemeralPublic.raw());
            header.write(keys.size());
            for (NodeKey.Public recipient : keys) {
                byte[] id = recipient.id();
                byte[] wrapping = wrappingKey(ephemeral.getPrivate(), recipient.key(), ephemeralPublic, recipient);
                header.write(id);
                header.write(seal(wrapping, new byte[NONCE_SIZE], id, dataKey));
            }
            byte[] aad = header.toByteArray();
            byte[] plain = new JsonObject().put(VALUE, value).encode().getBytes(StandardCharsets.UTF_8);
            byte[] sealed = seal(dataKey, nonce, aad, plain);

            var out = ByteBuffer.allocate(aad.length + NONCE_SIZE + sealed.length)
                    .put(aad).put(nonce).put(sealed);
            return PREFIX_V1 + Base64.getEncoder().encodeToString(out.array());
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Opens an encrypted value with the key of this node.
     *
     * @param value an encrypted value, {@code enc:v1:<base64>}.
     * @param key   the key of this node.
     * @return the value as it was encrypted: a string, number, boolean, JsonObject or JsonArray.
     * @throws EncryptedValueException when the key is not a recipient, the value was changed or is not readable.
     */
    public static Object decrypt(String value, NodeKey key) {
        byte[] bytes = bytes(value);
        try {
            var buffer = ByteBuffer.wrap(bytes);
            if (buffer.get() != VERSION) {
                throw new EncryptedValueException(Reason.VERSION, getEncryptedValueVersion(value));
            }
            byte[] ephemeral = new byte[NodeKey.Public.SIZE];
            buffer.get(ephemeral);
            int count = Byte.toUnsignedInt(buffer.get());
            byte[] own = key.getPublic().id();
            byte[] wrapped = null;

            for (int i = 0; i < count; i++) {
                byte[] id = new byte[ID_SIZE];
                byte[] entry = new byte[KEY_SIZE + TAG_SIZE];
                buffer.get(id).get(entry);
                if (Arrays.equals(id, own)) {
                    wrapped = entry;
                }
            }
            int headerSize = buffer.position();
            byte[] nonce = new byte[NONCE_SIZE];
            buffer.get(nonce);
            byte[] sealed = new byte[buffer.remaining()];
            buffer.get(sealed);

            if (wrapped == null) {
                throw new EncryptedValueException(Reason.NOT_RECIPIENT,
                        getEncryptedValueNotRecipient(key.getPublic().fingerprint(), count));
            }
            var ephemeralKey = new NodeKey.Public(ephemeral);
            byte[] wrapping = wrappingKey(key.privateKey(), ephemeralKey.key(), ephemeralKey, key.getPublic());
            byte[] dataKey = open(wrapping, new byte[NONCE_SIZE], own, wrapped);
            byte[] plain = open(dataKey, nonce, Arrays.copyOf(bytes, headerSize), sealed);
            return new JsonObject(new String(plain, StandardCharsets.UTF_8)).getValue(VALUE);
        } catch (BufferUnderflowException e) {
            throw new EncryptedValueException(Reason.FORMAT, getEncryptedValueFormat());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // a changed tag, nonce, key or ciphertext: the authentication fails (AEADBadTagException) or the key
            // agreement refuses the changed ephemeral key.
            throw new EncryptedValueException(Reason.TAMPERED, getEncryptedValueTampered());
        }
    }

    /**
     * Decrypts the encrypted values of a configuration in place (in nested objects and lists too) with the key of
     * this node ({@link NodeKey#local()}, read only when the configuration has an encrypted value).
     *
     * @param file   the configuration file, for messages.
     * @param config the configuration as read from the file.
     * @return true when the configuration had encrypted values.
     * @throws EncryptedValueException naming the file and the key when a value can not be opened: no node key,
     *                                 this node's key is not a recipient, or the value was changed.
     */
    public static boolean open(String file, JsonObject config) {
        return open(file, config, NodeKey::local);
    }

    /**
     * @param file   the configuration file, for messages.
     * @param config the configuration as read from the file.
     * @param keys   supplies the key of this node, called once when the first encrypted value is found.
     * @return true when the configuration had encrypted values.
     * @see #open(String, JsonObject)
     */
    public static boolean open(String file, JsonObject config, Supplier<Optional<NodeKey>> keys) {
        var key = new Supplier<Optional<NodeKey>>() {
            Optional<NodeKey> loaded;

            @Override
            public Optional<NodeKey> get() {
                if (loaded == null) {
                    loaded = keys.get();
                }
                return loaded;
            }
        };
        return openObject(file, "", config, key);
    }

    private static boolean openObject(String file, String parent, JsonObject json, Supplier<Optional<NodeKey>> key) {
        boolean found = false;
        for (String name : new ArrayList<>(json.fieldNames())) {
            Object value = json.getValue(name);
            String path = parent.isEmpty() ? name : parent + "." + name;
            if (isEncrypted(value)) {
                json.put(name, openValue(file, path, (String) value, key));
                found = true;
            } else {
                found |= openNested(file, path, value, key);
            }
        }
        return found;
    }

    private static boolean openNested(String file, String path, Object value, Supplier<Optional<NodeKey>> key) {
        boolean found = false;
        if (value instanceof JsonObject object) {
            found = openObject(file, path, object, key);
        } else if (value instanceof JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                Object item = array.getValue(i);
                if (isEncrypted(item)) {
                    array.set(i, openValue(file, path + "[" + i + "]", (String) item, key));
                    found = true;
                } else {
                    found |= openNested(file, path + "[" + i + "]", item, key);
                }
            }
        }
        return found;
    }

    private static Object openValue(String file, String path, String value, Supplier<Optional<NodeKey>> key) {
        Optional<NodeKey> own = key.get();
        if (own.isEmpty()) {
            throw new EncryptedValueException(Reason.NO_KEY,
                    getEncryptedConfigurationError(file, path, getEncryptedValueNoKey(NodeKey.path().toString())));
        }
        try {
            return decrypt(value, own.get());
        } catch (EncryptedValueException e) {
            throw new EncryptedValueException(e.getReason(), getEncryptedConfigurationError(file, path, e.getMessage()));
        }
    }

    // the bytes of an encrypted value: the version prefix and base64 checked.
    private static byte[] bytes(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            throw new EncryptedValueException(Reason.FORMAT, getEncryptedValueFormat());
        }
        if (!value.startsWith(PREFIX_V1)) {
            throw new EncryptedValueException(Reason.VERSION, getEncryptedValueVersion(value));
        }
        try {
            return Base64.getDecoder().decode(value.substring(PREFIX_V1.length()).trim());
        } catch (IllegalArgumentException e) {
            throw new EncryptedValueException(Reason.FORMAT, getEncryptedValueFormat());
        }
    }

    // HKDF-SHA256 (RFC 5869) of the X25519 agreement, 32 bytes: one expand block.
    private static byte[] wrappingKey(PrivateKey own, PublicKey other, NodeKey.Public ephemeral, NodeKey.Public recipient)
            throws GeneralSecurityException {
        KeyAgreement agreement = KeyAgreement.getInstance(AGREEMENT);
        agreement.init(own);
        agreement.doPhase(other, true);
        byte[] shared = agreement.generateSecret();

        byte[] salt = ByteBuffer.allocate(2 * NodeKey.Public.SIZE).put(ephemeral.raw()).put(recipient.raw()).array();
        Mac mac = Mac.getInstance(HMAC);
        mac.init(new SecretKeySpec(salt, HMAC));
        byte[] pseudorandom = mac.doFinal(shared);

        mac.init(new SecretKeySpec(pseudorandom, HMAC));
        mac.update(WRAP_INFO);
        mac.update((byte) 1);
        return mac.doFinal();
    }

    private static byte[] seal(byte[] key, byte[] nonce, byte[] aad, byte[] plain) throws GeneralSecurityException {
        return cipher(Cipher.ENCRYPT_MODE, key, nonce, aad).doFinal(plain);
    }

    private static byte[] open(byte[] key, byte[] nonce, byte[] aad, byte[] sealed) throws GeneralSecurityException {
        return cipher(Cipher.DECRYPT_MODE, key, nonce, aad).doFinal(sealed);
    }

    private static Cipher cipher(int mode, byte[] key, byte[] nonce, byte[] aad) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(mode, new SecretKeySpec(key, "ChaCha20"), new IvParameterSpec(nonce));
        cipher.updateAAD(aad);
        return cipher;
    }
}
