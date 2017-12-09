package com.codingchili.core.configuration.system;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.SelfSignedCertificate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Contains mappings of security dependencies between services.
 * <p>
 * For example: service A may depend on a shared secret with service B,
 * or depend on service B generating a token of its secret.
 * <p>
 * To establish such dependencies, a dependency is added to service A
 * with a path or a regex to match the path of service B. The
 * {@link AuthenticationDependency} class contains the type of
 * configuration that is requested, for example a token or shared secret.
 */
public class SecuritySettings implements Configurable {
    private Map<String, AuthenticationDependency> dependencies = new HashMap<>();
    private Map<String, TrustAndKeyProvider> loadedKeyStores = new HashMap<>();
    private Set<KeyStore> keystores = new HashSet<>();
    private int secretBytes = 64;
    private int tokenttl = 3600 * 24 * 7;

    @Override
    public String getPath() {
        return PATH_SECURITY;
    }

    /**
     * Loads a certificate from disk and saves it with the given name.
     * Requires manual input to enter the secret for the keystore.
     *
     * @return a keystore builder.
     */
    public KeyStoreBuilder<SecuritySettings> addKeystore() {
        return new KeyStoreBuilder<>(this, store -> {
            keystores.add(store);
        });
    }

    /**
     * @param storeId name of the keystore to retrieve: the mapped shortname of the filename with extension.
     * @param core    core context used when loading.
     * @return a keystore if it is loaded, if no keystore is added with the given shortname uses a
     * self signed certificate. If it fails to load a keystore then the application shuts down.
     */
    @JsonIgnore
    public synchronized TrustAndKeyProvider getKeystore(CoreContext core, String storeId) {
        if (!loadedKeyStores.containsKey(storeId)) {
            Optional<KeyStore> store = getByName(storeId);

            if (store.isPresent()) {
                loadKeystore(core, store.get().setShortName(storeId));
            } else {
                loadedKeyStores.put(storeId, generateSelfSigned(core, storeId));
            }
        }
        return loadedKeyStores.get(storeId);
    }

    public Optional<KeyStore> getByName(String storeId) {
        for (KeyStore keystore : keystores) {
            if (keystore.getShortName().equals(storeId)) {
                return Optional.of(keystore);
            }
        }
        return Optional.empty();
    }

    private void loadKeystore(CoreContext core, KeyStore store) {
        try {
            loadedKeyStores.put(store.getShortName(),
                    TrustAndKeyProvider.of(new JksOptions()
                            .setValue(Buffer.buffer(Files.readAllBytes(Paths.get(store.getPath()))))
                            .setPassword(store.getPassword())));
        } catch (Throwable e) {
            // failed to load keystore due to wrong password or missing file etc.
            // cannot recover from this in a safe manner: shut down.
            core.logger(getClass()).onError(e);
            System.exit(0);
        }
    }

    private TrustAndKeyProvider generateSelfSigned(CoreContext core, String storeId) {
        core.logger(getClass())
                .event(LOG_SECURITY, Level.WARNING).send(getMissingKeyStore());

        return TrustAndKeyProvider.of(SelfSignedCertificate.create(CoreStrings.GITHUB));
    }

    /**
     * @return a list of configured keystores.
     */
    public Set<KeyStore> getKeystores() {
        return keystores;
    }

    /**
     * @param keystores keystores to  set
     * @return fluent
     */
    public SecuritySettings setKeystores(Set<KeyStore> keystores) {
        this.keystores = keystores;
        return this;
    }

    /**
     * @return the number of bytes a secret must have at a minimum.
     */
    public int getSecretBytes() {
        return secretBytes;
    }

    /**
     * @param secretBytes the number of bytes generated secrets consists of.
     */
    public void setSecretBytes(int secretBytes) {
        this.secretBytes = secretBytes;
    }

    /**
     * @return a map of dependencies, where the key is the regex that match other
     * configurations. The value contains the actual security configuration to be applied.
     */
    public Map<String, AuthenticationDependency> getDependencies() {
        return dependencies;
    }

    /**
     * @param dependencies set the security configuration dependencies.
     */
    public void setDependencies(Map<String, AuthenticationDependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * @param path the regex (compared as text) dependency identifier.
     * @return the defined dependency with the given dependency path.
     */
    public Optional<AuthenticationDependency> getDependency(String path) {
        if (dependencies.containsKey(path)) {
            return Optional.of(dependencies.get(path));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param path       a regex to match the path of configuration files that the dependency applies to.
     * @param dependency the dependency that requires security parameters from files that the path mataches.
     * @return fluent
     */
    public SecuritySettings addDependency(String path, AuthenticationDependency dependency) {
        this.dependencies.put(path, dependency);
        return this;
    }

    public int getTokenttl() {
        return tokenttl;
    }

    public SecuritySettings setTokenttl(int tokenttl) {
        this.tokenttl = tokenttl;
        return this;
    }
}
