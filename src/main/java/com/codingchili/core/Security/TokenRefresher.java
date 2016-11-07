package com.codingchili.core.Security;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.AuthenticationDependency;
import com.codingchili.core.Configuration.System.SecuritySettings;
import com.codingchili.core.Configuration.WritableConfigurable;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Files.JsonFileStore;
import com.codingchili.core.Logging.Logger;
import com.codingchili.core.Protocol.Serializer;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Files.Configurations.security;

/**
 * @author Robin Duda
 */
public class TokenRefresher {
    private static final String DIR = DIR_CONFIG;
    private final Logger logger;
    private final SecuritySettings security;

    public TokenRefresher(Logger logger) {
        this.logger = logger;
        this.security = Configurations.security();

    }

    public void all() {
        secrets();
        preshare();
        tokens();
    }

    public void preshare() {
        HashMap<String, byte[]> shared = new HashMap<>();

        configurations((settings, config, path) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getPreshare().forEach(key -> {
                    if (!shared.containsKey(key)) {
                        shared.put(key, SecretFactory.generate(security.getSecretBytes()));
                    }

                    config.put(key, Base64.getEncoder().encode(shared.get(key)));
                    logger.log(Strings.getGeneratingShared(key, path));
                });
            });
        });
    }


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

    public void tokens() {
        configurations((settings, config, path) -> {
            andPathMatchesKeyRegex(settings, path).forEach(dependency -> {

                dependency.getTokens().entrySet().forEach(entry -> {
                    TokenIdentifier identifier = entry.getValue();
                    logger.log(Strings.getGeneratingToken(identifier.getService(), entry.getKey(), path));
                    config.put(entry.getKey(), Serializer.json(new Token(getFactory(identifier), getIdentity(config))));
                });
            });
        });
    }

    private TokenFactory getFactory(TokenIdentifier identifier) {
        try {
            JsonObject issuer = JsonFileStore.readObject(getService(identifier.getService()));
            byte[] secret = Base64.getDecoder().decode(issuer.getString(identifier.getSecret()));
            return new TokenFactory(secret);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RemoteIdentity getIdentity(JsonObject config) {
        return Serializer.unpack(config.getJsonObject(Strings.ID_IDENTITY), RemoteIdentity.class);
    }

    private List<AuthenticationDependency> andPathMatchesKeyRegex(HashMap<String, AuthenticationDependency> configured, String path) {
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
        HashMap<String, AuthenticationDependency> settings = security().getDependencies();

        getConfigurationsReferencedBySecurity(settings).forEach(path -> {
            try {
                JsonObject config = JsonFileStore.readObject(path);
                processor.parse(settings, config, path);
                JsonFileStore.writeObject(config, path);
            } catch (IOException e) {
                logger.onFileLoadError(path);
            }
        });
    }

    private List<String> getConfigurationsReferencedBySecurity(HashMap<String, AuthenticationDependency> settings) {
        List<String> configured = new ArrayList<>();

        Configurations.available(DIR).forEach(available -> {
            configured.addAll(settings.keySet().stream()
                    .filter(key -> isKeyMatchingPath(key, available))
                    .map(key -> available)
                    .distinct()
                    .collect(Collectors.toList()));
        });

        configured.forEach(path -> Configurations.get(path, WritableConfigurable.class));

        return configured;
    }

    @FunctionalInterface
    private interface TokenProcessor {
        void parse(HashMap<String, AuthenticationDependency> settings, JsonObject config, String key);
    }
}
