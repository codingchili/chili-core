package com.codingchili.core.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.security;
import static com.codingchili.core.protocol.Serializer.json;

/**
 * Generates service security configuration based on system security mappings.
 */
public class AuthenticationGenerator {
    private final Logger logger;
    private final SecuritySettings security;
    private CoreContext core;
    private String directory;

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
        this.logger = core.logger(getClass());
        this.security = Configurations.security();
        this.directory = directory;
    }

    /**
     * Generate new secrets, preshared secrets and tokens based on the
     * system security configuration.
     *
     * @return callback.
     */
    public Future<Void> all() {
        secrets();
        preshare();
        return tokens();
    }

    /**
     * Generates new preshared secrets based on the system security configuration.
     *
     * @return callback
     */
    public Future<Void> preshare() {
        HashMap<String, String> shared = new HashMap<>();

        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getPreshare().forEach(key -> {
                    if (!shared.containsKey(key)) {
                        shared.put(key, SecretFactory.generate(security.getSecretBytes()));
                    }

                    config.put(key, shared.get(key));
                    logger.log(CoreStrings.getGeneratingShared(key, path));
                });

                save.run();
            });
        });
        return Future.succeededFuture();
    }

    /**
     * Generates new secrets based on the system security configuration.
     *
     * @return callback
     */
    public Future<Void> secrets() {
        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getSecrets().forEach(entry -> {
                    logger.log(getGeneratingSecret(entry, path));
                    config.put(entry, SecretFactory.generate(security.getSecretBytes()));
                });

                save.run();
            });
        });
        return Future.succeededFuture();
    }

    /**
     * Generates new tokens based on the system security configuration.
     *
     * @return callback
     */
    public Future<Void> tokens() {
        Future<Void> future = Future.future();

        configurations((settings, config, path, save) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {
                AtomicInteger latch = new AtomicInteger(dependency.getTokens().size());

                dependency.getTokens().forEach((key, identifier) -> {
                    Optional<TokenFactory> factory = getFactory(identifier);

                    if (factory.isPresent()) {
                        Token token = new Token(getIdentity(config));

                        factory.get().hmac(token).setHandler(done -> {
                            if (done.succeeded()) {
                                logger.log(CoreStrings.generatedToken(identifier.getService(), key, path));

                                config.put(key, json(token));
                                save.run();

                                if (latch.decrementAndGet() == 0) {
                                    future.tryComplete();
                                }
                            } else {
                                future.fail(done.cause());
                            }
                        });
                    } else {
                        logger.onSecurityDependencyMissing(identifier.getService(), identifier.getSecret());
                        throw new SecurityMissingDependencyException(identifier.getService(), identifier.getSecret());
                    }
                });
            });
        });
        return future;
    }

    private Optional<TokenFactory> getFactory(TokenIdentifier identifier) {
        JsonObject issuer = ConfigurationFactory.readObject(getService(identifier.getService()));

        if (issuer.containsKey(identifier.getSecret())) {
            byte[] secret = Base64.getDecoder().decode(issuer.getString(identifier.getSecret()));
            return Optional.of(new TokenFactory(core, secret));
        } else {
            return Optional.empty();
        }
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
            identity = ID_UNDEFINED;
            config.put(ID_NODE, String.format("%s.%s", identity, ID_NODE));
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

        configured.forEach(path -> Configurations.get(path, BaseConfigurable.class));

        return configured;
    }

    @FunctionalInterface
    private interface TokenProcessor {
        void parse(Map<String, AuthenticationDependency> settings, JsonObject config, String key, Runnable done);
    }
}
