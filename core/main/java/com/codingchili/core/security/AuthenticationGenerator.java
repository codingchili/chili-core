package com.codingchili.core.security;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.BaseConfigurable;
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
import com.codingchili.core.security.exception.SecurityMissingDependencyException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.security;
import static com.codingchili.core.protocol.Serializer.json;
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

/**
 * Generates service security configuration based on system security mappings.
 * <p>
 * By default ({@link Mode#FILL}) existing secrets and preshared keys are kept and only missing ones are generated;
 * tokens are kept while they verify with the current secret of their issuer and are not close to expiry.
 * {@link Mode#ROTATE} generates new values for everything and keeps the old secrets and preshared keys under the
 * key with the suffix {@value CoreStrings#SECRET_PREVIOUS_SUFFIX}, which token factories still accept (see
 * {@link SecretRollover}); {@link Mode#RETIRE} removes those previous values.
 */
public class AuthenticationGenerator {
    private final Logger logger;
    private final SecuritySettings security;
    private CoreContext core;
    private String directory;
    private Mode mode = Mode.FILL;

    /**
     * What the generator does with the secrets, preshared keys and tokens that already exist.
     */
    public enum Mode {
        /**
         * keeps existing secrets, preshared keys and valid tokens, generates the missing ones.
         */
        FILL,
        /**
         * generates new values for everything, the old secrets and preshared keys are kept as previous values.
         */
        ROTATE,
        /**
         * removes the previous values of the secrets and preshared keys, changes nothing else.
         */
        RETIRE
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
        this.security = Configurations.security();
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
     * Generates secrets, preshared secrets and tokens based on the system security configuration, as the
     * mode of the generator says; in {@link Mode#RETIRE} removes the previous secrets.
     *
     * @return callback.
     */
    public Future<Void> all() {
        if (mode == Mode.RETIRE) {
            return retire();
        }
        secrets();
        preshare();
        return tokens();
    }

    /**
     * Generates preshared secrets based on the system security configuration. A preshared key has the same value
     * in every file that lists it: when kept and the files disagree, the most common value is kept.
     *
     * @return callback
     */
    public Future<Void> preshare() {
        if (mode == Mode.RETIRE) {
            return retire();
        }
        // the existing values of each preshared key, in the order of the files.
        Map<String, Map<String, String>> existing = new HashMap<>();

        configurations((settings, config, path, save) ->
                andPathMatchesKeyRegex(settings, path).forEach(dependency ->
                        dependency.getPreshare().forEach(key -> {
                            var values = existing.computeIfAbsent(key, k -> new LinkedHashMap<>());
                            if (isSet(config, key)) {
                                values.put(path, config.getString(key));
                            }
                        })));

        Map<String, String> chosen = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        existing.forEach((key, values) -> {
            Optional<String> common = mostCommon(values.values());
            if (mode == Mode.ROTATE) {
                chosen.put(key, SecretFactory.generate(security.getSecretBytes()));
                common.ifPresent(value -> previous.put(key, value));
            } else {
                chosen.put(key, common.orElseGet(() -> SecretFactory.generate(security.getSecretBytes())));
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

        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {
                dependency.getPreshare().forEach(key -> {
                    String value = chosen.get(key);

                    if (mode == Mode.ROTATE && previous.containsKey(key)) {
                        config.put(key + SECRET_PREVIOUS_SUFFIX, previous.get(key));
                        logger.log(getRotatedSecret(key, path));
                    } else if (value.equals(config.getValue(key))) {
                        logger.log(getKeptSecret(key, path));
                    } else {
                        logger.log(CoreStrings.getGeneratingShared(key, path));
                    }
                    config.put(key, value);
                });

                save.run();
            });
        });
        return Future.succeededFuture();
    }

    /**
     * Generates secrets based on the system security configuration.
     *
     * @return callback
     */
    public Future<Void> secrets() {
        if (mode == Mode.RETIRE) {
            return retire();
        }
        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getSecrets().forEach(entry -> {
                    if (mode == Mode.FILL && isSet(config, entry)) {
                        logger.log(getKeptSecret(entry, path));
                    } else {
                        if (mode == Mode.ROTATE && isSet(config, entry)) {
                            config.put(entry + SECRET_PREVIOUS_SUFFIX, config.getString(entry));
                            logger.log(getRotatedSecret(entry, path));
                        } else {
                            logger.log(getGeneratingSecret(entry, path));
                        }
                        config.put(entry, SecretFactory.generate(security.getSecretBytes()));
                    }
                });

                save.run();
            });
        });
        return Future.succeededFuture();
    }

    /**
     * Removes the previous values of the secrets and preshared keys that a rotation kept.
     *
     * @return callback
     */
    public Future<Void> retire() {
        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {
                var keys = new ArrayList<>(dependency.getSecrets());
                keys.addAll(dependency.getPreshare());

                keys.forEach(key -> {
                    if (config.remove(key + SECRET_PREVIOUS_SUFFIX) != null) {
                        logger.log(getRetiredSecret(key + SECRET_PREVIOUS_SUFFIX, path));
                    }
                });
                save.run();
            });
        });
        return Future.succeededFuture();
    }

    /**
     * Generates tokens based on the system security configuration. Unless rotating, a token is kept when it
     * verifies with the current secret of its issuer, belongs to the node and has more than a quarter of the
     * token lifetime left.
     *
     * @return callback
     */
    public Future<Void> tokens() {
        if (mode == Mode.RETIRE) {
            return retire();
        }
        var all = new ArrayList<Future<?>>();

        configurations((settings, config, path, save) -> {
            var file = new ArrayList<Future<?>>();

            andPathMatchesKeyRegex(settings, path).forEach(dependency ->
                    file.add(generateTokens(dependency, config, path)));

            all.add(Future.all(file).onComplete(done -> save.run()));
        });

        return Future.all(all).map(v -> null);
    }

    private Future<Void> generateTokens(AuthenticationDependency dependant, JsonObject config, String path) {
        var all = new ArrayList<Future<?>>();

        dependant.getTokens().forEach((key, identifier) -> {
            Optional<TokenFactory> factory = getFactory(identifier);

            if (factory.isPresent()) {
                String identity = getIdentity(config);

                all.add(keep(factory.get(), config, key, identity).map(kept -> {
                    logger.log(getKeptSecret(key, path));
                    return kept;
                }).recover(invalid -> {
                    Token token = new Token(identity);

                    return factory.get().hmac(token).onSuccess(done -> {
                        logger.log(CoreStrings.generatedToken(identifier.getService(), key, path));
                        config.put(key, json(token));
                    });
                }));
            } else {
                logger.onSecurityDependencyMissing(identifier.getService(), identifier.getSecret());
                throw new SecurityMissingDependencyException(identifier.getService(), identifier.getSecret());
            }
        });
        return Future.all(all).mapEmpty();
    }

    // succeeds when the existing token is kept, fails when a token must be issued.
    private Future<Void> keep(TokenFactory factory, JsonObject config, String key, String identity) {
        if (mode == Mode.ROTATE || !(config.getValue(key) instanceof JsonObject existing)) {
            return Future.failedFuture("issue");
        }
        try {
            Token token = Serializer.unpack(existing, Token.class);
            long remaining = token.getExpiry() - Instant.now().getEpochSecond();

            if (!identity.equals(token.getDomain()) || remaining < security.getTokenttl() / 4) {
                return Future.failedFuture("expires");
            }
            return factory.verify(token);
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    private Optional<TokenFactory> getFactory(TokenIdentifier identifier) {
        JsonObject issuer = ConfigurationFactory.readObject(getService(identifier.getService()));

        if (isSet(issuer, identifier.getSecret())) {
            byte[] secret = BASE64_DECODER.decode(issuer.getString(identifier.getSecret()));
            // the generator signs and verifies with the current secret only.
            return Optional.of(TokenFactory.strict(core, secret));
        } else {
            return Optional.empty();
        }
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

    private void configurations(TokenProcessor processor) {
        Map<String, AuthenticationDependency> settings = security().getDependencies();

        getConfigurationsReferencedBySecurity(settings).forEach(path -> {
            try {
                JsonObject config = ConfigurationFactory.readObject(path);
                processor.parse(settings, config, path, () -> {
                    ConfigurationFactory.writeObject(config, path);
                });
            } catch (NoSuchResourceException e) {
                logger.onError(e);
            }
        });
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
        Collections.sort(configured);
        configured.forEach(path -> Configurations.get(path, BaseConfigurable.class));

        return configured;
    }

    @FunctionalInterface
    private interface TokenProcessor {
        void parse(Map<String, AuthenticationDependency> settings, JsonObject config, String key, Runnable done);
    }
}
