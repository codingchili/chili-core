package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.*;
import java.util.*;

import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.exception.EncryptedValueException;
import com.codingchili.core.testing.ContextMock;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.security.AuthenticationGenerator.Mode.*;

/**
 * Tests the generator with a secrets store (generate --store), encryption per file to the nodes that read it
 * (generate --encrypt) and the bake of a store into the configuration (bake), per block too.
 */
@RunWith(VertxUnitRunner.class)
public class SecretStoreTest {
    private static final String DIR = "build/tmp/SecretStore/conf/";
    // absolute: a store lives outside the application.
    private static final Path STORE = Paths.get("build/tmp/SecretStore/store").toAbsolutePath();
    private static final Path OUT = Paths.get("build/tmp/SecretStore/out");
    private static final String DEV = "ZGV2ZWxvcG1lbnQgc2VjcmV0";
    private static final String LOGGING = "logging", AUTH = "auth", SOCIAL = "social", BANK = "bank",
            REALM_1 = "realm1", REALM_2 = "realm2", ARENA = "arena", DESIGNER = "designer", PVP = "pvp";
    private static final String HUB = "hub", REALM = "realm";
    private final Map<String, NodeKey> nodes = new HashMap<>();
    private SystemContext context;

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Before
    public void setUp() {
        context = new ContextMock();
        delete(Paths.get("build/tmp/SecretStore"));
        Configurations.put(security());
        Configurations.put(launcher());
        // the configuration of the release, with development secrets.
        for (String file : List.of(LOGGING, AUTH, SOCIAL, BANK, REALM_1, REALM_2, ARENA, DESIGNER)) {
            write(file, new JsonObject().put("name", file).put("loggingSecret", DEV).put("bankSecret", DEV)
                    .put("secret", DEV).put("global", DEV).put("node", file));
        }
    }

    @After
    public void tearDown(TestContext test) {
        System.clearProperty(PROPERTY_NODE_KEY);
        delete(Paths.get("build/tmp/SecretStore"));
        Configurations.reset();
        context.close().onComplete(test.asyncAssertSuccess());
    }

    private static SecuritySettings security() {
        var security = new SecuritySettings();
        security.setPath(PATH_SECURITY);
        security.setSecretBytes(32);
        var dependencies = new HashMap<String, AuthenticationDependency>();
        dependencies.put("[^/]*", new AuthenticationDependency().addToken(LOGGING, "service/" + LOGGING, "loggingSecret"));
        dependencies.put(LOGGING, new AuthenticationDependency().addSecret("loggingSecret"));
        dependencies.put(AUTH, new AuthenticationDependency().addKeypair("client"));
        dependencies.put(SOCIAL, new AuthenticationDependency().addPublickey("client"));
        dependencies.put(BANK, new AuthenticationDependency().addSecret("bankSecret"));
        dependencies.put("realm[^/]*", new AuthenticationDependency().addPreshare("global").addSecret("secret")
                .addToken(BANK, "service/" + BANK, "bankSecret"));
        security.setDependencies(dependencies);
        return security;
    }

    private static LauncherSettings launcher() {
        var launcher = new LauncherSettings();
        var blocks = new HashMap<String, List<String>>();
        // the files of each deployable are named after its package (ServiceFiles): service/<name>, <name>/.
        blocks.put(HUB, List.of(service(LOGGING), service(AUTH), service(SOCIAL), service(BANK)));
        blocks.put(REALM, List.of(service(REALM)));
        blocks.put(ARENA, List.of(service(ARENA)));
        blocks.put("designer", List.of(service(DESIGNER)));
        launcher.setBlocks(blocks);
        launcher.setHosts(new HashMap<>(Map.of("node-a", REALM)));
        return launcher;
    }

    @Test
    public void storeIgnoresDevelopmentValuesAndKeepsTheConfiguration(TestContext test) {
        var before = readAll();
        generate(FILL, false).compose(v -> {
            test.assertEquals(before, readAll());
            var logging = store(LOGGING);
            var realm = store(REALM_1);
            test.assertNotEquals(DEV, logging.getString("loggingSecret"));
            test.assertNotEquals(DEV, realm.getString("global"));
            test.assertEquals(realm.getString("global"), store(REALM_2).getString("global"));
            // only the generated keys of each file.
            test.assertEquals(Set.of("loggingSecret", LOGGING), logging.fieldNames());
            test.assertEquals(Set.of("global", "secret", LOGGING, BANK), realm.fieldNames());
            test.assertEquals(Set.of("clientPrivateKey", "clientPublicKey", LOGGING), store(AUTH).fieldNames());
            test.assertEquals(Set.of("clientPublicKey", LOGGING), store(SOCIAL).fieldNames());
            var saved = storeAll();
            return generate(FILL, false).compose(again -> {
                test.assertEquals(saved, storeAll());
                return generate(ROTATE, false);
            }).map(rotated -> {
                test.assertNotEquals(logging.getString("loggingSecret"), store(LOGGING).getString("loggingSecret"));
                test.assertNotEquals(store(AUTH).getString("clientPrivateKey"), saved.get(AUTH).getString("clientPrivateKey"));
                test.assertEquals(before, readAll());
                return rotated;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void encryptToTheNodesThatReadEachFile(TestContext test) {
        keys(HUB, REALM, ARENA, "node-a");
        generate(FILL, true).map(v -> {
            // the auth private key only for the hub, the public key plain.
            var auth = encrypted(AUTH);
            test.assertEquals(store(AUTH).getString("clientPrivateKey"), open(auth, "clientPrivateKey", HUB));
            test.assertEquals(store(AUTH).getString("clientPublicKey"), auth.getString("clientPublicKey"));
            notRecipient(auth, "clientPrivateKey", REALM);
            notRecipient(auth, "clientPrivateKey", ARENA);
            notRecipient(encrypted(BANK), "bankSecret", REALM);
            // the realms for the realm nodes: a host named as a block and a host mapped to it.
            var realm = encrypted(REALM_1);
            test.assertEquals(store(REALM_1).getString("secret"), open(realm, "secret", REALM));
            test.assertEquals(store(REALM_1).getString("secret"), open(realm, "secret", "node-a"));
            test.assertEquals(store(REALM_1).getJsonObject(BANK), open(realm, BANK, REALM));
            notRecipient(realm, "secret", HUB);
            test.assertEquals(store(ARENA).getJsonObject(LOGGING), open(encrypted(ARENA), LOGGING, ARENA));
            // no node runs the designer: no encrypted values.
            test.assertFalse(Files.exists(STORE.resolve(DIR_ENCRYPTED).resolve(file(DESIGNER))));
            test.assertTrue(Files.exists(STORE.resolve(DIR_ENCRYPTED).resolve(NODES_RECORD)));
            // the store keeps the plain values.
            test.assertFalse(EncryptedValue.isEncrypted(store(AUTH).getString("clientPrivateKey")));
            return v;
        }).compose(v -> generate(FILL, false)).map(v -> {
            test.assertFalse(Files.exists(STORE.resolve(DIR_ENCRYPTED)));
            return v;
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void encryptWithoutNodeKeysFailsAndWritesNothing(TestContext test) {
        generate(FILL, true).onComplete(test.asyncAssertFailure(e -> {
            test.assertTrue(e.getMessage().contains("keygen"), e.getMessage());
            test.assertFalse(Files.exists(STORE.resolve(file(LOGGING))));
        }));
    }

    @Test
    public void bakeWithoutStoreHasNoSecrets() {
        var mode = baker(null).bake();
        Assert.assertEquals(SecretBaker.Secrets.NONE, mode);
        var logging = baked(LOGGING);
        Assert.assertEquals(LOGGING, logging.getString("name"));
        Assert.assertFalse(logging.containsKey("loggingSecret"));
        Assert.assertFalse(baked(REALM_1).containsKey("global"));
        // a key that is not generated stays.
        Assert.assertEquals(DEV, baked(LOGGING).getString("bankSecret"));
    }

    @Test
    public void bakePlainAndPerBlock(TestContext test) {
        generate(FILL, false).map(v -> {
            Assert.assertEquals(SecretBaker.Secrets.PLAIN, baker(STORE).bake());
            Assert.assertEquals(store(REALM_1).getString("secret"), baked(REALM_1).getString("secret"));
            Assert.assertEquals(REALM_1, baked(REALM_1).getString("name"));
            Assert.assertEquals(Set.of("realm/realm1.json", "realm/realm2.json"), files(REALM));
            Assert.assertEquals(Set.of("service/logging.json", "service/auth.json", "service/social.json",
                    "service/bank.json"), files(HUB));
            Assert.assertEquals(Set.of("service/arena.json"), files(ARENA));
            Assert.assertEquals(Set.of("service/designer.json"), files("designer"));
            return v;
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void bakeEncrypted(TestContext test) {
        keys(HUB, REALM, ARENA);
        generate(FILL, true).map(v -> {
            Assert.assertEquals(SecretBaker.Secrets.ENCRYPTED, baker(STORE).bake());
            var realm = baked(REALM_1);
            Assert.assertTrue(EncryptedValue.isEncrypted(realm.getValue("secret")));
            Assert.assertEquals(store(REALM_1).getString("secret"), open(realm, "secret", REALM));
            // no node reads the designer's file: without secrets.
            Assert.assertFalse(baked(DESIGNER).containsKey(LOGGING));
            // the arena block runs the realm service too since: it reads the realm files, encrypt again.
            Configurations.launcher().getBlocks().put(ARENA, List.of(service(ARENA), service(REALM)));
            try {
                baker(STORE).bake();
                Assert.fail("the configs changed");
            } catch (CoreRuntimeException e) {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains(GENERATE_ENCRYPT));
            }
            Configurations.launcher().getBlocks().put(ARENA, List.of(service(ARENA)));
            Assert.assertEquals(SecretBaker.Secrets.ENCRYPTED, baker(STORE).bake());
            // a node added since: encrypt again.
            keys("node-a");
            try {
                baker(STORE).bake();
                Assert.fail("the nodes changed");
            } catch (CoreRuntimeException e) {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains(GENERATE_ENCRYPT));
            }
            return v;
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void renamedFileKeepsItsValuesInTheStore(TestContext test) {
        keys(HUB, REALM, ARENA);
        generate(FILL, true).compose(v -> {
            var saved = store(ARENA);
            var sealed = encrypted(ARENA);
            // a release renames the arena service and its file (config file = service name).
            Path from = Paths.get(DIR + file(ARENA));
            try {
                Files.move(from, Paths.get(DIR + file(PVP)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Configurations.launcher().getBlocks().put(ARENA, List.of(service(PVP)));
            Configurations.security().setRenames(Map.of("service/" + ARENA, "service/" + PVP));
            // the bake moves the values once: the record of the encrypted values still matches, nothing to encrypt.
            Assert.assertEquals(SecretBaker.Secrets.ENCRYPTED, baker(STORE).bake());
            Assert.assertFalse(Files.exists(STORE.resolve(file(ARENA))));
            Assert.assertFalse(Files.exists(STORE.resolve(DIR_ENCRYPTED).resolve(file(ARENA))));
            Assert.assertEquals(saved, store(PVP));
            Assert.assertEquals(sealed, encrypted(PVP));
            Assert.assertEquals(store(PVP).getJsonObject(LOGGING), open(baked(PVP), LOGGING, ARENA));
            Assert.assertEquals(0, SecretStoreRenames.apply(STORE, Configurations.security().getRenames(),
                    new com.codingchili.core.logging.ConsoleLogger(getClass())));
            // generate keeps the moved values (the token of the old file is still valid).
            return generate(FILL, true).map(again -> {
                test.assertEquals(saved, store(PVP));
                return again;
            });
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void renamedFileInTheStoreWithANewFileKeepsTheNewOne() {
        write(PVP, new JsonObject().put("name", PVP));
        AuthenticationGenerator.writePrivate(new JsonObject().put("old", true), STORE.resolve(file(ARENA)));
        AuthenticationGenerator.writePrivate(new JsonObject().put("new", true), STORE.resolve(file(PVP)));
        int moved = SecretStoreRenames.apply(STORE, Map.of("service/" + ARENA, "service/" + PVP),
                new com.codingchili.core.logging.ConsoleLogger(getClass()));
        Assert.assertEquals(0, moved);
        Assert.assertTrue(store(PVP).getBoolean("new"));
        Assert.assertTrue(store(ARENA).getBoolean("old"));
    }

    @Test
    public void bakeFailsWhenTheStoreMissesValues(TestContext test) {
        generate(FILL, false).map(v -> {
            // a file added by a release.
            write("realm3", new JsonObject().put("name", "realm3"));
            try {
                baker(STORE).bake();
                Assert.fail("the store has no values for realm3");
            } catch (CoreRuntimeException e) {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains("realm3") && e.getMessage().contains(GENERATE_STORE));
            }
            return v;
        }).onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void inPlaceEncryptThenFillNeedsTheKey(TestContext test) {
        keys(HUB, REALM, ARENA);
        new AuthenticationGenerator(context, DIR).setEncrypt(true).setNodes(STORE.resolve(DIR_NODES)).all()
                .compose(v -> {
                    var realm = read(REALM_1);
                    test.assertTrue(EncryptedValue.isEncrypted(realm.getValue("secret")));
                    // in place, the existing values are kept (and encrypted).
                    test.assertEquals(DEV, open(realm, "secret", REALM));
                    notRecipient(realm, "secret", HUB);
                    // a later run without the keys of every node can't open the values: it fails, writes nothing.
                    System.setProperty(PROPERTY_NODE_KEY, STORE.resolve("none.key").toString());
                    return new AuthenticationGenerator(context, DIR).all();
                }).onComplete(test.asyncAssertFailure(e -> {
                    test.assertTrue(e instanceof EncryptedValueException, e.toString());
                    test.assertTrue(e.getMessage().contains("this node's key can't open it"), e.getMessage());
                }));
    }

    private Future<Void> generate(AuthenticationGenerator.Mode mode, boolean encrypt) {
        return new AuthenticationGenerator(context, DIR).setMode(mode).setStore(STORE).setEncrypt(encrypt).all();
    }

    private static SecretBaker baker(Path store) {
        return new SecretBaker(DIR).setStore(store).setOut(OUT);
    }

    private void keys(String... hosts) {
        for (String host : hosts) {
            NodeKey key = NodeKey.generate();
            nodes.put(host, key);
            AuthenticationGenerator.writeText(STORE.resolve(DIR_NODES).resolve(host + ".pub"), key.publicKey() + "\n");
        }
    }

    private Object open(JsonObject json, String key, String host) {
        return EncryptedValue.decrypt(json.getString(key), nodes.get(host));
    }

    private void notRecipient(JsonObject json, String key, String host) {
        try {
            open(json, key, host);
            Assert.fail(host + " must not open " + key);
        } catch (EncryptedValueException e) {
            Assert.assertEquals(EncryptedValueException.Reason.NOT_RECIPIENT, e.getReason());
        }
    }

    // the deployable class of a name: its files are service/<name> and <name>/ (ServiceFiles).
    private static String service(String name) {
        return "test." + name + ".Service";
    }

    // the file of a name in the configuration directory: realm/<realm> for the realms, else service/<name>.
    private static String file(String name) {
        return (name.startsWith(REALM) ? "realm/" : "service/") + name + ".json";
    }

    private static Set<String> files(String block) {
        Path root = OUT.resolve(SecretBaker.DIR_BLOCKS).resolve(block);
        try (var list = Files.walk(root)) {
            return new HashSet<>(list.filter(Files::isRegularFile)
                    .map(path -> root.relativize(path).toString().replace('\\', '/')).toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, JsonObject> readAll() {
        var all = new HashMap<String, JsonObject>();
        List.of(LOGGING, AUTH, SOCIAL, BANK, REALM_1, REALM_2, ARENA, DESIGNER).forEach(name -> all.put(name, read(name)));
        return all;
    }

    private static Map<String, JsonObject> storeAll() {
        var all = new HashMap<String, JsonObject>();
        List.of(LOGGING, AUTH, SOCIAL, BANK, REALM_1, REALM_2, ARENA, DESIGNER).forEach(name -> all.put(name, store(name)));
        return all;
    }

    private static JsonObject read(String name) {
        return ConfigurationFactory.readObject(DIR + file(name));
    }

    private static JsonObject store(String name) {
        return ConfigurationFactory.readObject(STORE.resolve(file(name)).toString());
    }

    private static JsonObject encrypted(String name) {
        return ConfigurationFactory.readObject(STORE.resolve(DIR_ENCRYPTED).resolve(file(name)).toString());
    }

    private static JsonObject baked(String name) {
        return ConfigurationFactory.readObject(OUT.resolve(file(name)).toString());
    }

    private static void write(String name, JsonObject json) {
        ConfigurationFactory.writeObject(json, DIR + file(name));
    }

    private static void delete(Path path) {
        if (Files.exists(path)) {
            AuthenticationGenerator.delete(path);
        }
    }
}
