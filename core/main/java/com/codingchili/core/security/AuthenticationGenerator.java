package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.core.files.Configurations.security;
import static com.codingchili.core.protocol.Serializer.json;
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

/**
 * Generates service security configuration based on system security mappings.
 * <p>
 * By default ({@link Mode#FILL}) existing secrets, preshared keys and key pairs are kept and only missing ones are
 * generated; tokens are kept while they verify with the current secret of their issuer and are not close to expiry
 * (they are issued for {@link SecuritySettings#getServiceTokenttl()} and reissued in their last quarter; with a
 * service token lifetime of 0 they never expire and are reissued only when missing or no longer valid).
 * {@link Mode#ROTATE} generates new values for everything and keeps nothing old: every token issued before is
 * rejected once the services have the new files (restart them).
 * <p>
 * Key pairs ({@code keypairs} and {@code publickeys} of a dependency): the files that sign get the private and the
 * public key ({@code <name>PrivateKey}, {@code <name>PublicKey}), the files that verify only the public key, see
 * {@link TokenFactory#signer} and {@link TokenFactory#verifier}.
 * <p>
 * Every configuration file is read once, all values are computed in memory and each changed file is written once
 * at the end (atomically, see {@link ConfigurationFactory#writeObject(JsonObject, String)}): a file is never read
 * while it is being written. Any failure fails the returned future and writes nothing.
 * <p>
 * A secrets store ({@link #setStore(Path)}, {@code generate --store <dir>}) keeps the generated values apart from
 * the configuration: {@code <store>/<path in the configuration directory>} (for example
 * {@code <store>/service/authentication.yaml}) holds only the generated keys of that file, plain. The values of the
 * configuration files themselves (development secrets) are never used then, and the configuration files are not
 * written: the values are read from the store and written back to it. {@code bake} merges a store into the
 * configuration of a release.
 * <p>
 * Encryption ({@link #setEncrypt(boolean)}, {@code generate --encrypt}): the secrets, preshared keys, private keys and
 * tokens are written as encrypted values ({@link EncryptedValue}, {@code enc:v1:...}), public keys stay plain. The
 * values of a file are encrypted to the nodes that run a block that reads the file, see {@link SecretRecipients}
 * (the public keys in {@link #setNodes(Path)}, by default {@code <store>/nodes}, or {@code conf/system/nodes}
 * without a store). With a store the encrypted values go to {@code <store>/encrypted/} and the store keeps the plain
 * values (the generator needs them for the next run); without {@code --encrypt} that folder is removed. Without a
 * store the configuration files get the encrypted values; values encrypted before are opened with this node's key.
 */
public class AuthenticationGenerator {
    // a token issued for longer than the configured lifetime (and this margin) is reissued.
    private static final long LIFETIME_MARGIN_SECONDS = 60;
    private final Logger logger;
    private final CoreContext core;
    private final String directory;
    private Mode mode = Mode.FILL;
    private Path store;
    private Path nodes;
    private boolean encrypt = false;

    /**
     * What the generator does with the secrets, preshared keys, key pairs and tokens that already exist.
     */
    public enum Mode {
        /**
         * keeps existing secrets, preshared keys, key pairs and valid tokens, generates the missing ones.
         */
        FILL,
        /**
         * generates new values for everything, nothing old is kept: revokes every token issued before.
         */
        ROTATE
    }

    /**
     * @param core the core context to run on.
     */
    public AuthenticationGenerator(CoreContext core) {
        this(core, DIR_CONFIG);
    }

    /**
     * @param core      the core context to run on.
     * @param directory the directory to search for configurations.
     */
    public AuthenticationGenerator(CoreContext core, String directory) {
        this.core = core;
        this.logger = new ConsoleLogger(getClass());
        this.directory = directory;
    }

    /**
     * @param mode what to do with existing values, default {@link Mode#FILL}.
     * @return fluent
     */
    public AuthenticationGenerator setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * @param store the secrets store to read the values from and write them to, null for the configuration files.
     * @return fluent
     */
    public AuthenticationGenerator setStore(Path store) {
        this.store = store;
        return this;
    }

    /**
     * @param encrypt true: writes the values as encrypted values to the nodes that read each file.
     * @return fluent
     */
    public AuthenticationGenerator setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
        return this;
    }

    /**
     * @param nodes the directory with the public keys of the nodes ({@code <host>.pub}), null for the default:
     *              {@code <store>/nodes}, or {@code conf/system/nodes} without a store.
     * @return fluent
     */
    public AuthenticationGenerator setNodes(Path nodes) {
        this.nodes = nodes;
        return this;
    }

    /**
     * @return the mode of the generator.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Generates secrets, preshared secrets, key pairs and tokens based on the system security configuration, as
     * the mode of the generator says.
     *
     * @return completed when the files are written, failed when nothing was written.
     */
    public Future<Void> all() {
        return run(true, true, true);
    }

    /**
     * Generates preshared secrets based on the system security configuration. A preshared key has the same value
     * in every file that lists it: when kept and the files disagree, the most common value is kept.
     *
     * @return completed when the files are written.
     */
    public Future<Void> preshare() {
        return run(false, true, false);
    }

    /**
     * Generates secrets and key pairs based on the system security configuration.
     *
     * @return completed when the files are written.
     */
    public Future<Void> secrets() {
        return run(true, false, false);
    }

    /**
     * Generates tokens based on the system security configuration. Unless rotating, a token is kept when it
     * verifies with the current secret of its issuer, belongs to the node and has more than a quarter of the
     * service token lifetime left (and not more than that lifetime); without a lifetime (0) it never expires.
     *
     * @return completed when the files are written.
     */
    public Future<Void> tokens() {
        return run(false, false, true);
    }

    private Future<Void> run(boolean secrets, boolean preshare, boolean tokens) {
        try {
            Instant now = Instant.now();
            // a store made before a configuration file was renamed: its values move to the new name first.
            SecretStoreRenames.apply(store, security().getRenames(), logger);
            // the nodes first: a missing public key fails before anything is generated.
            SecretRecipients recipients = (encrypt) ? new SecretRecipients(launcher(), directory, nodes()) : null;
            List<ConfigFile> files = load();

            if (mode == Mode.ROTATE) {
                removePrevious(files);
            }
            if (secrets) {
                secrets(files);
                keypairs(files);
            }
            if (preshare) {
                preshare(files);
            }
            return (tokens ? tokens(files, now) : Future.<Void>succeededFuture())
                    .compose(done -> save(files, recipients));
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    private void secrets(List<ConfigFile> files) {
        files.forEach(file -> file.secrets().forEach(entry -> {
            JsonObject config = file.json;

            if (mode == Mode.FILL && isSet(config, entry)) {
                logger.log(getKeptSecret(entry, file.path));
            } else {
                if (mode == Mode.ROTATE && isSet(config, entry)) {
                    logger.log(getRotatedSecret(entry, file.path));
                } else {
                    logger.log(getGeneratingSecret(entry, file.path));
                }
                config.put(entry, SecretFactory.generate(security().getSecretBytes()));
            }
        }));
    }

    private void preshare(List<ConfigFile> files) {
        // the existing values of each preshared key, in the order of the files.
        Map<String, Map<String, String>> existing = new HashMap<>();

        files.forEach(file -> file.preshared().forEach(key -> {
            var values = existing.computeIfAbsent(key, k -> new LinkedHashMap<>());
            if (isSet(file.json, key)) {
                values.put(file.path, file.json.getString(key));
            }
        }));

        Map<String, String> chosen = new HashMap<>();

        existing.forEach((key, values) -> {
            Optional<String> common = mostCommon(values.values());
            if (mode == Mode.ROTATE) {
                chosen.put(key, SecretFactory.generate(security().getSecretBytes()));
            } else {
                chosen.put(key, common.orElseGet(() -> SecretFactory.generate(security().getSecretBytes())));
                common.ifPresent(value -> {
                    var replaced = values.entrySet().stream()
                            .filter(entry -> !entry.getValue().equals(value))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    if (!replaced.isEmpty()) {
                        logger.log(getPreshareConflict(key, replaced));
                    }
                });
            }
        });

        files.forEach(file -> file.preshared().forEach(key -> {
            String value = chosen.get(key);

            if (mode == Mode.ROTATE && isSet(file.json, key)) {
                logger.log(getRotatedSecret(key, file.path));
            } else if (value.equals(file.json.getValue(key))) {
                logger.log(getKeptSecret(key, file.path));
            } else {
                logger.log(CoreStrings.getGeneratingShared(key, file.path));
            }
            file.json.put(key, value);
        }));
    }

    // values that an earlier 1.4.9 generator kept for its rollover: a rotate removes them.
    private void removePrevious(List<ConfigFile> files) {
        files.forEach(file -> {
            file.rotatable().forEach(key -> {
                if (file.json.remove(key + SECRET_PREVIOUS_SUFFIX) != null) {
                    logger.log(getRemovedPrevious(key + SECRET_PREVIOUS_SUFFIX, file.path));
                }
            });
            file.json.remove(SECRETS_ROTATED);
        });
    }

    // a key pair: generated once for the files that sign with it (private and public key), the files that verify get
    // its public key. Kept unless rotating.
    private void keypairs(List<ConfigFile> files) {
        Set<String> names = new TreeSet<>();
        files.forEach(file -> {
            names.addAll(file.keypairs());
            names.addAll(file.publickeys());
        });

        for (String name : names) {
            String privateKey = name + PRIVATE_KEY_SUFFIX;
            String publicKey = name + PUBLIC_KEY_SUFFIX;
            var signers = files.stream().filter(file -> file.keypairs().contains(name)).toList();
            var verifiers = files.stream()
                    .filter(file -> file.publickeys().contains(name) && !file.keypairs().contains(name))
                    .toList();

            if (signers.isEmpty()) {
                // no private key here: the public key of the verifiers is kept as it is.
                logger.log(getKeyPairWithoutSigner(name, verifiers.stream().map(file -> file.path).toList()));
                continue;
            }
            Optional<ConfigFile> existing = signers.stream()
                    .filter(file -> isSet(file.json, privateKey) && isSet(file.json, publicKey))
                    .findFirst();
            String chosenPrivate;
            String chosenPublic;

            if (mode == Mode.FILL && existing.isPresent()) {
                chosenPrivate = existing.get().json.getString(privateKey);
                chosenPublic = existing.get().json.getString(publicKey);
            } else {
                KeyPair pair = SecretFactory.keyPair();
                chosenPrivate = SecretFactory.encode(pair.getPrivate());
                chosenPublic = SecretFactory.encode(pair.getPublic());
            }
            signers.forEach(file -> {
                boolean changed = !chosenPrivate.equals(file.json.getValue(privateKey)) ||
                        !chosenPublic.equals(file.json.getValue(publicKey));
                logger.log(changed ? getGeneratingKeyPair(name, file.path, true) : getKeptSecret(privateKey, file.path));
                file.json.put(privateKey, chosenPrivate);
                file.json.put(publicKey, chosenPublic);
            });
            verifiers.forEach(file -> {
                boolean changed = !chosenPublic.equals(file.json.getValue(publicKey));
                logger.log(changed ? getGeneratingKeyPair(name, file.path, false) : getKeptSecret(publicKey, file.path));
                file.json.put(publicKey, chosenPublic);
                // a verifier never holds the private key.
                file.json.remove(privateKey);
            });
        }
    }

    private Future<Void> tokens(List<ConfigFile> files, Instant now) {
        // the tokens are put into the configurations once all are done, from one thread.
        var issued = new ArrayList<Future<Runnable>>();

        files.forEach(file -> file.dependencies.forEach(dependency ->
                dependency.getTokens().forEach((key, identifier) -> {
                    TokenFactory factory = getFactory(files, identifier).orElseThrow(() -> {
                        logger.onSecurityDependencyMissing(identifier.getService(), identifier.getSecret());
                        return new SecurityMissingDependencyException(identifier.getService(), identifier.getSecret());
                    });
                    String identity = getIdentity(file.json);

                    issued.add(keep(factory, file.json, key, identity, now).<Runnable>map(kept ->
                            () -> logger.log(getKeptSecret(key, file.path))
                    ).recover(invalid -> {
                        long lifetime = security().getServiceTokenttl().toSeconds();
                        Token token = new Token(identity)
                                .setExpiry((lifetime > 0) ? now.getEpochSecond() + lifetime : Token.NEVER);

                        return factory.hmac(token).map(signed -> () -> {
                            logger.log(CoreStrings.generatedToken(identifier.getService(), key, file.path));
                            file.json.put(key, json(token));
                        });
                    }));
                })));

        return Future.all(issued).map(done -> {
            issued.forEach(token -> token.result().run());
            return null;
        });
    }

    // succeeds when the existing token is kept, fails when a token must be issued.
    private Future<Void> keep(TokenFactory factory, JsonObject config, String key, String identity, Instant now) {
        if (mode == Mode.ROTATE || !(config.getValue(key) instanceof JsonObject existing)) {
            return Future.failedFuture("issue");
        }
        try {
            Token token = Serializer.unpack(existing, Token.class);
            long remaining = token.getExpiry() - now.getEpochSecond();
            long lifetime = security().getServiceTokenttl().toSeconds();

            if (!identity.equals(token.getDomain())) {
                return Future.failedFuture("domain");
            } else if (lifetime <= 0 && token.getExpiry() < Token.NEVER) {
                // tokens never expire: one issued with a lifetime (an earlier setting) is replaced once.
                return Future.failedFuture("expires");
            } else if (lifetime > 0 && (remaining < lifetime / 4 || remaining > lifetime + LIFETIME_MARGIN_SECONDS)) {
                return Future.failedFuture("expires");
            }
            return factory.verify(token);
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    // the secret of the issuer: from the files of this run when it is one of them, it may be new.
    private Optional<TokenFactory> getFactory(List<ConfigFile> files, TokenIdentifier identifier) {
        String path = getService(identifier.getService());
        JsonObject issuer = files.stream()
                .filter(file -> samePath(file.path, path))
                .map(file -> file.json)
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return ConfigurationFactory.readObject(path);
                    } catch (NoSuchResourceException e) {
                        return new JsonObject();
                    }
                });

        if (isSet(issuer, identifier.getSecret())) {
            byte[] secret = BASE64_DECODER.decode(issuer.getString(identifier.getSecret()));
            // the generator signs and verifies with the current secret only.
            return Optional.of(new TokenFactory(core, secret));
        } else {
            return Optional.empty();
        }
    }

    private Future<Void> save(List<ConfigFile> files, SecretRecipients recipients) {
        return core.blocking(() -> {
            if (store != null) {
                saveStore(files, recipients);
            } else {
                files.stream()
                        .filter(file -> encrypt || file.changed())
                        .forEach(file -> {
                            JsonObject json = file.json;
                            if (encrypt) {
                                List<NodeKey.Public> keys = recipients.keys(file.path);
                                if (keys.isEmpty()) {
                                    logger.log(getNoRecipients(file.path, false));
                                    if (!file.changed()) {
                                        return;
                                    }
                                } else {
                                    json = seal(file, json.copy(), keys);
                                }
                            }
                            ConfigurationFactory.writeObject(json, file.path);
                        });
            }
        });
    }

    // the plain values to the store, the encrypted ones (--encrypt) to <store>/encrypted; the configuration is not
    // written.
    private void saveStore(List<ConfigFile> files, SecretRecipients recipients) {
        Path encrypted = store.resolve(DIR_ENCRYPTED);
        try {
            if (!Files.exists(store)) {
                Files.createDirectories(store);
                permissions(store, "rwx------");
            }
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
        for (ConfigFile file : files) {
            JsonObject values = file.generated();
            String relative = SecretRecipients.relative(directory, file.path);
            writePrivate(values, store.resolve(relative));

            if (encrypt) {
                List<NodeKey.Public> keys = recipients.keys(file.path);
                if (keys.isEmpty()) {
                    logger.log(getNoRecipients(file.path, true));
                    deleteIfExists(encrypted.resolve(relative));
                } else {
                    logger.log(getEncryptedTo(file.path, recipients.hosts(file.path)));
                    writePrivate(seal(file, values, keys), encrypted.resolve(relative));
                }
            }
        }
        if (encrypt) {
            writeText(encrypted.resolve(NODES_RECORD), recipients.record(files.stream().map(file -> file.path).toList()));
        } else if (Files.exists(encrypted)) {
            delete(encrypted);
            logger.log(getEncryptionRemoved(encrypted.toString()));
        }
        logger.log(getStoreSaved(files.size(), store.toString(), encrypt));
    }

    // the values to encrypt: everything generated but the public keys.
    private static JsonObject seal(ConfigFile file, JsonObject json, List<NodeKey.Public> keys) {
        file.secretKeys().forEach(key -> {
            Object value = json.getValue(key);
            if (value != null && !EncryptedValue.isEncrypted(value)) {
                json.put(key, EncryptedValue.encrypt(value, keys));
            }
        });
        return json;
    }

    private Path nodes() {
        if (nodes != null) {
            return nodes;
        } else if (store != null) {
            return store.resolve(DIR_NODES);
        } else {
            return Paths.get(directory, "system", DIR_NODES);
        }
    }

    /**
     * Writes a file that holds secrets: created readable by its owner only where the file system supports it, its
     * directories too, then written atomically (which keeps the permissions).
     *
     * @param json the values to write.
     * @param path the file.
     */
    static void writePrivate(JsonObject json, Path path) {
        try {
            createPrivate(path);
            ConfigurationFactory.writeObject(json, path.toString());
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    static void writeText(Path path, String text) {
        try {
            createPrivate(path);
            Files.writeString(path, text);
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private static void createPrivate(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
            permissions(parent, "rwx------");
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
            permissions(path, "rw-------");
        }
    }

    private static void permissions(Path path, String permissions) throws IOException {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
        } catch (UnsupportedOperationException e) {
            // not a POSIX file system: the permissions of the parent apply.
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    static void delete(Path directory) {
        try (var walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder()).forEach(AuthenticationGenerator::deleteIfExists);
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private List<ConfigFile> load() {
        Map<String, AuthenticationDependency> settings = security().getDependencies();
        List<ConfigFile> files = new ArrayList<>();

        getConfigurationsReferencedBySecurity(settings).forEach(path -> {
            try {
                var dependencies = andPathMatchesKeyRegex(settings, path);
                JsonObject json = ConfigurationFactory.readObject(path);

                if (store != null) {
                    // the values of the store only: the configuration's own (development) values are never kept.
                    generatedKeys(dependencies).forEach(json::remove);
                    Path saved = store.resolve(SecretRecipients.relative(directory, path));
                    if (Files.exists(saved)) {
                        JsonObject values = ConfigurationFactory.readObject(saved.toString());
                        values.forEach(entry -> {
                            if (EncryptedValue.isEncrypted(entry.getValue())) {
                                throw new CoreRuntimeException(getStoreEncrypted(saved.toString(), entry.getKey()));
                            }
                        });
                        json.mergeIn(values);
                    }
                } else {
                    // values encrypted by an earlier run: opened with the key of this node, or the run fails.
                    EncryptedValue.open(path, json);
                }
                files.add(new ConfigFile(path, json, dependencies));
            } catch (NoSuchResourceException e) {
                logger.onError(e);
            }
        });
        return files;
    }

    /**
     * @param dependencies the security dependencies of a file.
     * @return the keys the generator writes into the file: secrets, preshared keys, tokens and the keys of key pairs.
     */
    static Set<String> generatedKeys(Collection<AuthenticationDependency> dependencies) {
        Set<String> keys = new TreeSet<>();
        dependencies.forEach(dependency -> {
            keys.addAll(dependency.getSecrets());
            keys.addAll(dependency.getPreshare());
            keys.addAll(dependency.getTokens().keySet());
            dependency.getKeypairs().forEach(name -> {
                keys.add(name + PRIVATE_KEY_SUFFIX);
                keys.add(name + PUBLIC_KEY_SUFFIX);
            });
            dependency.getPublickeys().forEach(name -> keys.add(name + PUBLIC_KEY_SUFFIX));
        });
        return keys;
    }

    /**
     * @param path a configuration file.
     * @return the keys the generator writes into the file by the security dependencies, empty when it has none.
     */
    public static Set<String> generatedKeys(String path) {
        var settings = security().getDependencies();
        var generator = new AuthenticationGenerator(null, DIR_CONFIG);
        return generatedKeys(generator.andPathMatchesKeyRegex(settings, path));
    }

    /**
     * @param directory the configuration directory.
     * @return the configuration files that have security dependencies, sorted, with their dependencies.
     */
    static Map<String, List<AuthenticationDependency>> referenced(String directory) {
        var settings = security().getDependencies();
        var generator = new AuthenticationGenerator(null, directory);
        Map<String, List<AuthenticationDependency>> files = new LinkedHashMap<>();
        generator.getConfigurationsReferencedBySecurity(settings)
                .forEach(path -> files.put(path, generator.andPathMatchesKeyRegex(settings, path)));
        return files;
    }

    private static boolean isSet(JsonObject config, String key) {
        return config.getValue(key) instanceof String value && !value.isBlank();
    }

    private static Optional<String> mostCommon(Collection<String> values) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        values.forEach(value -> counts.merge(value, 1, Integer::sum));
        // the first of the most common values: the files are visited in a stable order.
        return counts.entrySet().stream()
                .reduce((a, b) -> (b.getValue() > a.getValue()) ? b : a)
                .map(Map.Entry::getKey);
    }

    private String getService(String name) {
        if (directory.equals(DIR_CONFIG)) {
            return CoreStrings.getService(name);
        } else {
            return directory + DIR_ROOT + name;
        }
    }

    // the same file: the extension of either may be left out.
    private static boolean samePath(String file, String reference) {
        return Paths.get(withoutExtension(file)).normalize().equals(Paths.get(withoutExtension(reference)).normalize());
    }

    private static String withoutExtension(String path) {
        int dot = path.lastIndexOf('.');
        int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return (dot > separator + 1) ? path.substring(0, dot) : path;
    }

    private String getIdentity(JsonObject config) {
        String identity;

        if (config.containsKey(ID_NODE)) {
            identity = config.getString(ID_NODE);
            if (!identity.endsWith(ID_NODE)) {
                identity += "." + ID_NODE;
            }
        } else {
            // the same identity as the next run reads from the node that is written here.
            identity = String.format("%s.%s", ID_UNDEFINED, ID_NODE);
            config.put(ID_NODE, identity);
            logger.log(getIdentityNotConfigured(getClass().getSimpleName()));
        }
        return identity;
    }

    private List<AuthenticationDependency> andPathMatchesKeyRegex(Map<String, AuthenticationDependency> configured, String path) {
        return configured.entrySet().stream()
                .filter(entry -> isKeyMatchingPath(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private boolean isKeyMatchingPath(String fileName, String path) {
        // make sure to allow any file with an extension that is registered in the configuration factory.
        String supportedExtensions = "(" + ConfigurationFactory.supported().stream()
                .collect(Collectors.joining("|")) + ")";

        Pattern pattern = Pattern.compile(".*" + fileName + supportedExtensions);
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    private List<String> getConfigurationsReferencedBySecurity(Map<String, AuthenticationDependency> settings) {
        List<String> configured = new ArrayList<>();

        Configurations.available(directory).forEach(available -> {
            configured.addAll(settings.keySet().stream()
                    .filter(key -> isKeyMatchingPath(key, available))
                    .map(key -> available)
                    .distinct()
                    .collect(Collectors.toList()));
        });
        // the files in a stable order: the most common preshared key wins ties by the first file.
        return configured.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * A configuration file of the run: read once, changed in memory, written at the end if changed.
     */
    private static final class ConfigFile {
        final String path;
        final JsonObject json;
        final JsonObject original;
        final List<AuthenticationDependency> dependencies;

        ConfigFile(String path, JsonObject json, List<AuthenticationDependency> dependencies) {
            this.path = path;
            this.json = json;
            this.original = json.copy();
            this.dependencies = dependencies;
        }

        // each key once, when more than one dependency of the file lists it.
        Set<String> secrets() {
            Set<String> keys = new LinkedHashSet<>();
            dependencies.forEach(dependency -> keys.addAll(new TreeSet<>(dependency.getSecrets())));
            return keys;
        }

        Set<String> preshared() {
            Set<String> keys = new LinkedHashSet<>();
            dependencies.forEach(dependency -> keys.addAll(new TreeSet<>(dependency.getPreshare())));
            return keys;
        }

        Set<String> keypairs() {
            Set<String> keys = new TreeSet<>();
            dependencies.forEach(dependency -> keys.addAll(dependency.getKeypairs()));
            return keys;
        }

        Set<String> publickeys() {
            Set<String> keys = new TreeSet<>();
            dependencies.forEach(dependency -> keys.addAll(dependency.getPublickeys()));
            return keys;
        }

        Set<String> rotatable() {
            Set<String> keys = secrets();
            keys.addAll(preshared());
            return keys;
        }

        boolean changed() {
            return !json.equals(original);
        }

        // the generated keys of the file that are set, with their values.
        JsonObject generated() {
            JsonObject values = new JsonObject();
            generatedKeys(dependencies).forEach(key -> {
                if (json.containsKey(key)) {
                    values.put(key, json.getValue(key));
                }
            });
            return values;
        }

        // the generated keys that are secret: all but the public keys.
        Set<String> secretKeys() {
            Set<String> keys = generatedKeys(dependencies);
            dependencies.forEach(dependency -> {
                dependency.getKeypairs().forEach(name -> keys.remove(name + PUBLIC_KEY_SUFFIX));
                dependency.getPublickeys().forEach(name -> keys.remove(name + PUBLIC_KEY_SUFFIX));
            });
            return keys;
        }
    }
}
