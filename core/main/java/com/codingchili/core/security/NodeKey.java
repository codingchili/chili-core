package com.codingchili.core.security;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.spec.*;
import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * The X25519 key of a node: the private key opens the configuration values that were encrypted to its public key
 * ({@code enc:}, see {@link EncryptedValue}). The key file ({@link #path()}) is written by the launcher command
 * {@code keygen}:
 * <pre>
 * # comment lines
 * x25519-private:&lt;base64 PKCS#8&gt;
 * x25519:&lt;base64 of the 32 byte public key&gt;
 * </pre>
 * The public key in its text form ({@code x25519:<base64>}, {@link #publicKey()}) is what the generator encrypts to.
 */
public class NodeKey {
    private static final String ALGORITHM = "X25519";
    private static final String PRIVATE_PREFIX = "x25519-private:";
    private final PrivateKey privateKey;
    private final Public publicKey;

    private NodeKey(PrivateKey privateKey, Public publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * @return a new random node key.
     */
    public static NodeKey generate() {
        try {
            KeyPair pair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
            return new NodeKey(pair.getPrivate(), Public.of(pair.getPublic()));
        } catch (NoSuchAlgorithmException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * @return the key file of this node: the system property {@value com.codingchili.core.configuration.CoreStrings#PROPERTY_NODE_KEY},
     * else the environment variable {@value com.codingchili.core.configuration.CoreStrings#ENV_NODE_KEY}, else
     * {@value com.codingchili.core.configuration.CoreStrings#PATH_NODE_KEY}.
     */
    public static Path path() {
        String configured = System.getProperty(PROPERTY_NODE_KEY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENV_NODE_KEY);
        }
        if (configured == null || configured.isBlank()) {
            configured = PATH_NODE_KEY;
        }
        return Paths.get(configured);
    }

    /**
     * @return the key of this node, empty when the key file is missing or empty (an empty file, such as /dev/null
     * mounted in its place, means no key).
     * @throws CoreRuntimeException when the file exists and can not be read or is not a node key.
     */
    public static Optional<NodeKey> local() {
        Path path = path();
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            return text.isBlank() ? Optional.empty() : Optional.of(parse(text, path.toString()));
        } catch (IOException e) {
            throw new CoreRuntimeException(getNodeKeyUnreadable(path.toString(), e.getMessage()));
        }
    }

    /**
     * @param path the key file.
     * @return the key in the file.
     */
    public static NodeKey read(Path path) {
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8), path.toString());
        } catch (IOException e) {
            throw new CoreRuntimeException(getNodeKeyUnreadable(path.toString(), e.getMessage()));
        }
    }

    private static NodeKey parse(String text, String source) {
        String privateText = null;
        String publicText = null;
        for (String line : text.split("\\R")) {
            line = line.trim();
            if (line.startsWith(PRIVATE_PREFIX)) {
                privateText = line.substring(PRIVATE_PREFIX.length());
            } else if (line.startsWith(Public.PREFIX)) {
                publicText = line;
            }
        }
        if (privateText == null || publicText == null) {
            throw new CoreRuntimeException(getNodeKeyUnreadable(source, "not a node key (keygen writes one)"));
        }
        try {
            PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateText)));
            return new NodeKey(key, Public.parse(publicText));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CoreRuntimeException(getNodeKeyUnreadable(source, e.getMessage()));
        }
    }

    /**
     * Writes the key file, readable by its owner only where the file system supports it.
     *
     * @param path  the file to write.
     * @param force replaces an existing file when true.
     * @throws CoreRuntimeException when the file exists and force is false, or can not be written.
     */
    public void write(Path path, boolean force) {
        if (Files.exists(path) && !force) {
            throw new CoreRuntimeException(getNodeKeyExists(path.toString()));
        }
        String text = String.format("# the key of this node (chili-core keygen): keep it private, on this node only.%n" +
                        "# its public key, which the generator encrypts the secrets of this node's services to:%n" +
                        "# %s%n%s%s%n%s%n", publicKey.text(), PRIVATE_PREFIX,
                Base64.getEncoder().encodeToString(privateKey.getEncoded()), publicKey.text());
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = Files.createTempFile(parent, ".node", ".key");
            try {
                Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException e) {
                // not a POSIX file system (Windows): the directory's permissions apply.
            }
            Files.writeString(temporary, text, StandardCharsets.UTF_8);
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new CoreRuntimeException(getNodeKeyUnreadable(path.toString(), e.getMessage()));
        }
    }

    /**
     * @return the public key of this node in its text form, {@code x25519:<base64>}.
     */
    public String publicKey() {
        return publicKey.text();
    }

    /**
     * @return the public key of this node.
     */
    public Public getPublic() {
        return publicKey;
    }

    PrivateKey privateKey() {
        return privateKey;
    }

    /**
     * The public key of a node: what values are encrypted to.
     *
     * @param raw the 32 bytes of the X25519 public key (u coordinate, little endian).
     */
    public record Public(byte[] raw) {
        /**
         * the prefix of the text form of a public key.
         */
        public static final String PREFIX = "x25519:";
        // the X.509 encoding of an X25519 public key: this header and the 32 bytes of the key.
        private static final byte[] X509_HEADER = {0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x6e, 0x03, 0x21, 0x00};
        static final int SIZE = 32;

        /**
         * @param raw the 32 bytes of the key.
         */
        public Public {
            if (raw.length != SIZE) {
                throw new IllegalArgumentException("an X25519 public key has 32 bytes, not " + raw.length);
            }
            raw = raw.clone();
        }

        /**
         * @param text a public key as {@code x25519:<base64>}.
         * @return the public key.
         * @throws CoreRuntimeException when the text is not a public key.
         */
        public static Public parse(String text) {
            String trimmed = text.trim();
            if (!trimmed.startsWith(PREFIX)) {
                throw new CoreRuntimeException(getNodePublicKeyInvalid(trimmed));
            }
            try {
                return new Public(Base64.getDecoder().decode(trimmed.substring(PREFIX.length())));
            } catch (IllegalArgumentException e) {
                throw new CoreRuntimeException(getNodePublicKeyInvalid(trimmed));
            }
        }

        /**
         * @return the 32 bytes of the key.
         */
        @Override
        public byte[] raw() {
            return raw.clone();
        }

        static Public of(PublicKey key) {
            byte[] encoded = key.getEncoded();
            return new Public(Arrays.copyOfRange(encoded, encoded.length - SIZE, encoded.length));
        }

        PublicKey key() {
            try {
                byte[] encoded = Arrays.copyOf(X509_HEADER, X509_HEADER.length + SIZE);
                System.arraycopy(raw, 0, encoded, X509_HEADER.length, SIZE);
                return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
            } catch (GeneralSecurityException e) {
                throw new CoreRuntimeException(e);
            }
        }

        /**
         * @return the key in its text form, {@code x25519:<base64>}.
         */
        public String text() {
            return PREFIX + Base64.getEncoder().encodeToString(raw);
        }

        /**
         * @return the id of the key in an encrypted value: the first 8 bytes of its SHA-256.
         */
        public byte[] id() {
            try {
                return Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(raw), EncryptedValue.ID_SIZE);
            } catch (NoSuchAlgorithmException e) {
                throw new CoreRuntimeException(e);
            }
        }

        /**
         * @return the id of the key as hex, for messages.
         */
        public String fingerprint() {
            return String.format("%016x", new BigInteger(1, id()));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Public key && Arrays.equals(raw, key.raw);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(raw);
        }

        @Override
        public String toString() {
            return text();
        }
    }
}
