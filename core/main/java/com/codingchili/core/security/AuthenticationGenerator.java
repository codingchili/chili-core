package com.codingchili.core.security;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.security;
import static com.codingchili.core.protocol.Serializer.json;

/**
 * @author Robin Duda
 * <p>
 * Generates service security configuration based on system security mappings.
 */
public class AuthenticationGenerator {
    private final Logger logger;
    private final SecuritySettings security;
    private String directory;

    public AuthenticationGenerator(Logger logger) {
        this(DIR_CONFIG, logger);
    }

    public AuthenticationGenerator(String directory, Logger logger) {
        this.logger = logger;
        this.security = Configurations.security();
        this.directory = directory;
    }

    /**
     * Generate new secrets, preshared secrets and tokens based on the
     * system security configuration.
     */
    public void all() {
        secrets();
        preshare();
        tokens();
    }

    /**
     * Generates new preshared secrets based on the system security configuration.
     */
    public void preshare() {
        HashMap<String, String> shared = new HashMap<>();

        configurations((settings, config, path) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getPreshare().forEach(key -> {
                    if (!shared.containsKey(key)) {
                        shared.put(key, SecretFactory.generate(security.getSecretBytes()));
                    }

                    config.put(key, shared.get(key));
                    logger.log(CoreStrings.getGeneratingShared(key, path));
                });
            });
        });
    }

    /**
     * Generates new secrets based on the system security configuration.
     */
    public void secrets() {
        configurations((settings, config, path) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getSecrets().forEach(entry -> {
                    logger.log(getGeneratingSecret(entry, path));
                    config.put(entry, SecretFactory.generate(security.getSecretBytes()));
                });
            });
        });
    }

    /**
     * Generates new tokens based on the system security configuration.
     */
    public void tokens() {
        configurations((settings, config, path) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getTokens().forEach((key, identifier) -> {
                    logger.log(CoreStrings.getGeneratingToken(identifier.getService(), key, path));
                    config.put(key, json(new Token(getFactory(identifier), getIdentity(config))));
                });
            });
        });
    }

    private TokenFactory getFactory(TokenIdentifier identifier) {
        JsonObject issuer = ConfigurationFactory.readObject(getService(identifier.getService()));

        if (issuer.containsKey(identifier.getSecret())) {
            byte[] secret = Base64.getDecoder().decode(issuer.getString(identifier.getSecret()));
            return new TokenFactory(secret);
        } else {
            logger.onSecurityDependencyMissing(identifier.getService(), identifier.getSecret());
            throw new SecurityMissingDependencyException(identifier.getService(), identifier.getSecret());
        }
    }

    private String getService(String name) {
        if (directory.equals(DIR_CONFIG)) {
            return CoreStrings.getService(name);
        } else {
            return directory + DIR_ROOT + name + EXT_JSON;
        }
    }

    private String getIdentity(JsonObject config) {
        String identity;

        if (config.containsKey(ID_NODE)) {
            identity = config.getString(ID_NODE);
        } else {
            identity = ID_UNDEFINED;
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
        Pattern pattern = Pattern.compile(".*" + fileName + EXT_JSON);
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    private void configurations(TokenProcessor processor) {
        Map<String, AuthenticationDependency> settings = security().getDependencies();

        getConfigurationsReferencedBySecurity(settings).forEach(path -> {
            try {
                JsonObject config = ConfigurationFactory.readObject(path);
                processor.parse(settings, config, path);
                ConfigurationFactory.writeObject(config, path);
            } catch (NoSuchResourceException e) {
                logger.onFileLoadError(path);
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
        void parse(Map<String, AuthenticationDependency> settings, JsonObject config, String key);
    }
}
