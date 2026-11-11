package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.exception.SecretRotationRefusedException;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.security;
import static com.codingchili.core.protocol.Serializer.json;
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

/**
 * Generates service security configuration based on system security mappings.
 * <p>
 * By default ({@link Mode#FILL}) existing secrets and preshared keys are kept and only missing ones are generated;
 * tokens are kept while they verify with the current secret of their issuer and are not close to expiry (they are
 * issued for {@link SecuritySettings#getServiceTokenttl()} and reissued in their last quarter; with a service token
 * lifetime of 0 they never expire and are reissued only when missing or no longer valid).
 * {@link Mode#ROTATE} generates new values for everything and keeps the old secrets and preshared keys under the
 * key with the suffix {@value CoreStrings#SECRET_PREVIOUS_SUFFIX}, which token factories still accept (see
 * {@link SecretRollover}); {@link Mode#RETIRE} removes those previous values. {@link Mode#REVOKE} is the emergency
 * rotation for a leaked key: new values for everything and no previous values at all, so tokens signed with the old
 * secrets stop verifying as soon as the services have the new files (restart them to be sure).
 * <p>
 * The retire window: a rotate writes its time ({@value CoreStrings#SECRETS_ROTATED}, epoch seconds) next to the
 * previous values. Until {@link SecuritySettings#getTokenttl()} has passed since then, tokens signed with the
 * previous secrets may still be in use: {@link Mode#RETIRE} and another {@link Mode#ROTATE} refuse unless forced,
 * and once it has passed {@link Mode#FILL} retires the previous values by itself.
 * <p>
 * Every configuration file is read once, all values are computed in memory and each changed file is written once
 * at the end (atomically, see {@link ConfigurationFactory#writeObject(JsonObject, String)}): a file is never read
 * while it is being written. Any failure fails the returned future and writes nothing.
 */
public class AuthenticationGenerator {
    // a token issued for longer than the configured lifetime (and this margin) is reissued.
    private static final long LIFETIME_MARGIN_SECONDS = 60;
    private final Logger logger;
    private final CoreContext core;
    private final String directory;
    private Mode mode = Mode.FILL;
    private boolean force = false;

    /**
     * What the generator does with the secrets, preshared keys and tokens that already exist.
     */
    public enum Mode {
        /**
         * keeps existing secrets, preshared keys and valid tokens, generates the missing ones; retires the previous
         * values of a rotate once its retire window has passed.
         */
        FILL,
        /**
         * generates new values for everything, the old secrets and preshared keys are kept as previous values.
         */
        ROTATE,
        /**
         * removes the previous values of the secrets and preshared keys, changes nothing else.
         */
        RETIRE,
        /**
         * a key leaked: generates new values for everything like {@link #ROTATE}, but keeps no previous values and
         * drops the existing ones (and the time of the last rotate): every token signed before is rejected.
         * Not bound to the retire window.
         */
        REVOKE
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
     * @return the mode of the generator.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param force true to retire or rotate inside the retire window of the last rotate: tokens signed with the
     *              secrets that are replaced or removed are no longer accepted.
     * @return fluent
     */
    public AuthenticationGenerator setForce(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Generates secrets, preshared secrets and tokens based on the system security configuration, as the
     * mode of the generator says; in {@link Mode#RETIRE} removes the previous secrets.
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
     * Generates secrets based on the system security configuration.
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

    /**
     * Removes the previous values of the secrets and preshared keys that a rotation kept, unless inside the
     * retire window of the rotate and not forced.
     *
     * @return completed when the files are written.
     */
    public Future<Void> retire() {
        return setMode(Mode.RETIRE).all();
    }

    private Future<Void> run(boolean secrets, boolean preshare, boolean tokens) {
        try {
            Instant now = Instant.now();
            List<ConfigFile> files = load();
            Rotation rotation = rotation(files);

            if (mode == Mode.RETIRE) {
                if (!rotation.hasPrevious()) {
                    logger.log(getNothingToRetire());
                } else if (rotation.inWindow(now) && !force) {
                    throw new SecretRotationRefusedException(getRetireRefused(rotation.rotated, rotation.until()));
                } else {
                    retirePrevious(files);
                }
                return save(files);
            }

            if (mode == Mode.REVOKE) {
                // nothing old stays accepted: the previous values of an earlier rotate go too.
                retirePrevious(files);
            } else if (rotation.hasPrevious()) {
                if (mode == Mode.ROTATE && rotation.inWindow(now) && !force) {
                    throw new SecretRotationRefusedException(getRotateRefused(rotation.rotated, rotation.until()));
                } else if (mode == Mode.FILL && rotation.rotated == null) {
                    logger.log(getRotationStamped(now));
                    rotation.files.forEach(file -> file.json.put(SECRETS_ROTATED, now.getEpochSecond()));
                } else if (mode == Mode.FILL && !rotation.inWindow(now)) {
                    logger.log(getAutoRetire(rotation.rotated, rotation.window));
                    retirePrevious(files);
                }
            }

            if (secrets) {
                secrets(files, now);
            }
            if (preshare) {
                preshare(files, now);
            }
            return (tokens ? tokens(files, now) : Future.<Void>succeededFuture())
                    .compose(done -> save(files));
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    private void secrets(List<ConfigFile> files, Instant now) {
        files.forEach(file -> file.secrets().forEach(entry -> {
            JsonObject config = file.json;

            if (mode == Mode.FILL && isSet(config, entry)) {
                logger.log(getKeptSecret(entry, file.path));
            } else {
                if (mode == Mode.ROTATE && isSet(config, entry)) {
                    keepPrevious(file, entry, config.getString(entry), now);
                } else if (mode == Mode.REVOKE && isSet(config, entry)) {
                    logger.log(getRevokedSecret(entry, file.path));
                } else {
                    logger.log(getGeneratingSecret(entry, file.path));
                }
                config.put(entry, SecretFactory.generate(security().getSecretBytes()));
            }
        }));
    }

    private void preshare(List<ConfigFile> files, Instant now) {
        // the existing values of each preshared key, in the order of the files.
        Map<String, Map<String, String>> existing = new HashMap<>();

        files.forEach(file -> file.preshared().forEach(key -> {
            var values = existing.computeIfAbsent(key, k -> new LinkedHashMap<>());
            if (isSet(file.json, key)) {
                values.put(file.path, file.json.getString(key));
            }
        }));

        Map<String, String> chosen = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        existing.forEach((key, values) -> {
            Optional<String> common = mostCommon(values.values());
            if (mode == Mode.ROTATE || mode == Mode.REVOKE) {
                chosen.put(key, SecretFactory.generate(security().getSecretBytes()));
                common.ifPresent(value -> previous.put(key, value));
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

            if (mode == Mode.ROTATE && previous.containsKey(key)) {
                keepPrevious(file, key, previous.get(key), now);
            } else if (mode == Mode.REVOKE && isSet(file.json, key)) {
                logger.log(getRevokedSecret(key, file.path));
            } else if (value.equals(file.json.getValue(key))) {
                logger.log(getKeptSecret(key, file.path));
            } else {
                logger.log(CoreStrings.getGeneratingShared(key, file.path));
            }
            file.json.put(key, value);
        }));
    }

    // a rotate keeps the replaced value as the previous one and records when.
    private void keepPrevious(ConfigFile file, String key, String value, Instant now) {
        file.json.put(key + SECRET_PREVIOUS_SUFFIX, value);
        file.json.put(SECRETS_ROTATED, now.getEpochSecond());
        logger.log(getRotatedSecret(key, file.path));
    }

    private void retirePrevious(List<ConfigFile> files) {
        files.forEach(file -> {
            file.rotatable().forEach(key -> {
                if (file.json.remove(key + SECRET_PREVIOUS_SUFFIX) != null) {
                    logger.log(getRetiredSecret(key + SECRET_PREVIOUS_SUFFIX, file.path));
                }
            });
            file.json.remove(SECRETS_ROTATED);
        });
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
                        long lifetime = security().getServiceTokenttl();
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
        if (mode == Mode.ROTATE || mode == Mode.REVOKE || !(config.getValue(key) instanceof JsonObject existing)) {
            return Future.failedFuture("issue");
        }
        try {
            Token token = Serializer.unpack(existing, Token.class);
            long remaining = token.getExpiry() - now.getEpochSecond();
            long lifetime = security().getServiceTokenttl();

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
            return Optional.of(TokenFactory.strict(core, secret));
        } else {
            return Optional.empty();
        }
    }

    private Rotation rotation(List<ConfigFile> files) {
        var rotated = files.stream()
                .filter(ConfigFile::hasPrevious)
                .collect(Collectors.toList());
        Instant last = null;

        for (ConfigFile file : rotated) {
            if (file.json.getValue(SECRETS_ROTATED) instanceof Number stamp) {
                Instant time = Instant.ofEpochSecond(stamp.longValue());
                last = (last == null || time.isAfter(last)) ? time : last;
            } else {
                // a file rotated by an older version: the time is unknown.
                last = null;
                break;
            }
        }
        return new Rotation(rotated, last, security().getTokenttl());
    }

    private Future<Void> save(List<ConfigFile> files) {
        return core.blocking(() -> files.stream()
                .filter(ConfigFile::changed)
                .forEach(file -> ConfigurationFactory.writeObject(file.json, file.path)));
    }

    private List<ConfigFile> load() {
        Map<String, AuthenticationDependency> settings = security().getDependencies();
        List<ConfigFile> files = new ArrayList<>();

        getConfigurationsReferencedBySecurity(settings).forEach(path -> {
            try {
                files.add(new ConfigFile(path, ConfigurationFactory.readObject(path),
                        andPathMatchesKeyRegex(settings, path)));
            } catch (NoSuchResourceException e) {
                logger.onError(e);
            }
        });
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

        Set<String> rotatable() {
            Set<String> keys = secrets();
            keys.addAll(preshared());
            return keys;
        }

        boolean hasPrevious() {
            return rotatable().stream().anyMatch(key -> json.containsKey(key + SECRET_PREVIOUS_SUFFIX));
        }

        boolean changed() {
            return !json.equals(original);
        }
    }

    /**
     * The previous values of the last rotate.
     *
     * @param files   the files with previous values.
     * @param rotated the time of the rotate, null when unknown (a file without the time).
     * @param window  the retire window in seconds.
     */
    private record Rotation(List<ConfigFile> files, Instant rotated, long window) {
        boolean hasPrevious() {
            return !files.isEmpty();
        }

        // an unknown time is inside the window.
        boolean inWindow(Instant now) {
            return rotated == null || now.isBefore(until());
        }

        Instant until() {
            return (rotated == null) ? null : rotated.plusSeconds(window);
        }
    }
}
