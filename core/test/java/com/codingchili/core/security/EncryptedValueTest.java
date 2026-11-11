package com.codingchili.core.security;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.exception.EncryptedValueException;
import com.codingchili.core.security.exception.EncryptedValueException.Reason;

import static com.codingchili.core.configuration.CoreStrings.PROPERTY_NODE_KEY;

/**
 * Tests the encrypted configuration values (enc:v1), the node key file and the decryption of configurations.
 */
@RunWith(VertxUnitRunner.class)
public class EncryptedValueTest {
    private static final Path DIR = Paths.get("build/tmp/EncryptedValue");
    private static final String SECRET = "c2VjcmV0IHZhbHVlIG9mIGEgc2VydmljZQ==";

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Before
    public void setUp() throws Exception {
        clear();
        Files.createDirectories(DIR);
    }

    @After
    public void tearDown() {
        System.clearProperty(PROPERTY_NODE_KEY);
        Configurations.reset();
        clear();
    }

    @Test
    public void roundTripOneRecipient() {
        NodeKey node = NodeKey.generate();
        String sealed = EncryptedValue.encrypt(SECRET, List.of(node.getPublic()));

        Assert.assertTrue(sealed.startsWith("enc:v1:"));
        Assert.assertTrue(EncryptedValue.isEncrypted(sealed));
        Assert.assertFalse(sealed.contains(SECRET));
        Assert.assertEquals(SECRET, EncryptedValue.decrypt(sealed, node));
        // a new ephemeral key and nonce each time.
        Assert.assertNotEquals(sealed, EncryptedValue.encrypt(SECRET, List.of(node.getPublic())));
    }

    @Test
    public void roundTripThreeRecipients() {
        var nodes = List.of(NodeKey.generate(), NodeKey.generate(), NodeKey.generate());
        var token = new JsonObject().put("domain", "realm.node").put("expiry", 1L).put("key", "abc")
                .put("list", new JsonArray().add(1).add("two"));
        String sealed = EncryptedValue.encrypt(token, nodes.stream().map(NodeKey::getPublic).toList());

        nodes.forEach(node -> Assert.assertEquals(token, EncryptedValue.decrypt(sealed, node)));
    }

    @Test
    public void nonRecipientCanNotOpen() {
        NodeKey node = NodeKey.generate();
        String sealed = EncryptedValue.encrypt(SECRET, List.of(node.getPublic(), NodeKey.generate().getPublic()));

        var e = refused(() -> EncryptedValue.decrypt(sealed, NodeKey.generate()), Reason.NOT_RECIPIENT);
        Assert.assertTrue(e.getMessage(), e.getMessage().contains("this node's key can't open it"));
        Assert.assertTrue(e.getMessage(), e.getMessage().contains("2 recipients"));
    }

    @Test
    public void tamperingIsDetected() {
        NodeKey node = NodeKey.generate();
        String sealed = EncryptedValue.encrypt(SECRET, List.of(node.getPublic()));
        byte[] bytes = Base64.getDecoder().decode(sealed.substring("enc:v1:".length()));

        // every byte after the version and the recipient count: the ephemeral key, the wrapped key, the nonce, the
        // ciphertext and the tag. A changed key id makes the node no recipient.
        for (int i = 1; i < bytes.length; i++) {
            if (i == 33) {
                continue;
            }
            byte[] changed = bytes.clone();
            changed[i] ^= 0x01;
            String tampered = "enc:v1:" + Base64.getEncoder().encodeToString(changed);
            Reason expected = (i >= 34 && i < 42) ? Reason.NOT_RECIPIENT : Reason.TAMPERED;
            refused(() -> EncryptedValue.decrypt(tampered, node), expected);
        }
        // truncated.
        String truncated = "enc:v1:" + Base64.getEncoder().encodeToString(Arrays.copyOf(bytes, 40));
        refused(() -> EncryptedValue.decrypt(truncated, node), Reason.FORMAT);
    }

    @Test
    public void versionedFormat() {
        NodeKey node = NodeKey.generate();
        String sealed = EncryptedValue.encrypt(SECRET, List.of(node.getPublic()));

        var e = refused(() -> EncryptedValue.decrypt(sealed.replace("enc:v1:", "enc:v2:"), node), Reason.VERSION);
        Assert.assertTrue(e.getMessage(), e.getMessage().contains("'v2'"));
        // the version byte inside.
        byte[] bytes = Base64.getDecoder().decode(sealed.substring("enc:v1:".length()));
        bytes[0] = 2;
        refused(() -> EncryptedValue.decrypt("enc:v1:" + Base64.getEncoder().encodeToString(bytes), node), Reason.VERSION);
        refused(() -> EncryptedValue.decrypt("enc:v1:not base64!", node), Reason.FORMAT);
        refused(() -> EncryptedValue.decrypt("plain", node), Reason.FORMAT);
        Assert.assertFalse(EncryptedValue.isEncrypted("plain"));
        Assert.assertFalse(EncryptedValue.isEncrypted(new JsonObject()));
    }

    @Test
    public void configurationLoadDecryptsAndKeepsPlainValues() {
        NodeKey node = useKey(NodeKey.generate());
        var token = new JsonObject().put("domain", "a.node").put("key", "k");
        String path = write("service.yaml", new JsonObject()
                .put("plain", "visible")
                .put("secret", EncryptedValue.encrypt(SECRET, List.of(node.getPublic())))
                .put("token", EncryptedValue.encrypt(token, List.of(node.getPublic())))
                .put("nested", new JsonObject().put("inner", EncryptedValue.encrypt(7, List.of(node.getPublic())))));

        var config = Configurations.get(path, Settings.class);
        Assert.assertEquals("visible", config.plain);
        Assert.assertEquals(SECRET, config.secret);
        Assert.assertEquals(token, config.token);
        Assert.assertEquals(7, config.nested.getInteger("inner").intValue());
    }

    @Test
    public void missingKeyFailsOnlyTheFileThatIsUsed() {
        // no node key at all: a file without encrypted values loads, the one with fails, naming the file and key.
        System.setProperty(PROPERTY_NODE_KEY, DIR.resolve("missing.key").toString());
        String plain = write("plain.yaml", new JsonObject().put("plain", "visible"));
        String sealed = write("sealed.yaml", new JsonObject().put("plain", "visible")
                .put("secret", EncryptedValue.encrypt(SECRET, List.of(NodeKey.generate().getPublic()))));

        Assert.assertEquals("visible", Configurations.get(plain, Settings.class).plain);
        var e = refused(() -> Configurations.get(sealed, Settings.class), Reason.NO_KEY);
        Assert.assertTrue(e.getMessage(), e.getMessage().contains(sealed) && e.getMessage().contains("'secret'"));
        Assert.assertTrue(e.getMessage(), e.getMessage().contains("this node's key can't open it"));

        // the key of another node.
        useKey(NodeKey.generate());
        e = refused(() -> Configurations.get(sealed, Settings.class), Reason.NOT_RECIPIENT);
        Assert.assertTrue(e.getMessage(), e.getMessage().contains(sealed) && e.getMessage().contains("'secret'"));
    }

    @Test
    public void emptyKeyFileIsNoKey() throws Exception {
        Path empty = DIR.resolve("empty.key");
        Files.writeString(empty, "");
        System.setProperty(PROPERTY_NODE_KEY, empty.toString());
        Assert.assertTrue(NodeKey.local().isEmpty());
    }

    @Test
    public void keyFileRoundTripAndRefusesOverwrite() throws Exception {
        Path path = DIR.resolve("keys/node.key");
        NodeKey node = NodeKey.generate();
        node.write(path, false);

        NodeKey read = NodeKey.read(path);
        Assert.assertEquals(node.publicKey(), read.publicKey());
        Assert.assertTrue(node.publicKey().startsWith("x25519:"));
        Assert.assertEquals(node.getPublic(), NodeKey.Public.parse(node.publicKey()));
        Assert.assertEquals(SECRET, EncryptedValue.decrypt(EncryptedValue.encrypt(SECRET, List.of(node.getPublic())), read));
        try {
            Assert.assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(path));
        } catch (UnsupportedOperationException e) {
            // not POSIX.
        }
        try {
            NodeKey.generate().write(path, false);
            Assert.fail("an existing key must not be replaced without force");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("--force"));
        }
        Assert.assertEquals(node.publicKey(), NodeKey.read(path).publicKey());
        NodeKey replacement = NodeKey.generate();
        replacement.write(path, true);
        Assert.assertEquals(replacement.publicKey(), NodeKey.read(path).publicKey());
    }

    private static NodeKey useKey(NodeKey key) {
        Path path = DIR.resolve("node-" + UUID.randomUUID() + ".key");
        key.write(path, false);
        System.setProperty(PROPERTY_NODE_KEY, path.toString());
        return key;
    }

    private static String write(String name, JsonObject json) {
        String path = DIR.resolve(name).toString().replace('\\', '/');
        ConfigurationFactory.writeObject(json, path);
        return path;
    }

    private static EncryptedValueException refused(Runnable action, Reason reason) {
        try {
            action.run();
        } catch (EncryptedValueException e) {
            Assert.assertEquals(e.getMessage(), reason, e.getReason());
            return e;
        }
        Assert.fail("expected " + reason);
        return null;
    }

    private static void clear() {
        if (Files.exists(DIR)) {
            try (var walk = Files.walk(DIR)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A configuration with plain and encrypted values.
     */
    public static class Settings extends BaseConfigurable {
        public String plain;
        public String secret;
        public JsonObject token;
        public JsonObject nested;
    }
}
