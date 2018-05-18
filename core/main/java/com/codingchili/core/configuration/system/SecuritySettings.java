package com.codingchili.core.configuration.system;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.JksOptions;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.StartupListener;
import com.codingchili.core.logging.*;
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
    private static Logger logger = new ConsoleLogger(SecuritySettings.class);
    private Map<String, AuthenticationDependency> dependencies = new HashMap<>();
    private Map<String, TrustAndKeyProvider> loadedKeyStores = new HashMap<>();
    private Set<KeyStoreReference> keystores = new HashSet<>();
    private ArgonSettings argon = new ArgonSettings();
    private String hmacAlgorithm = "HmacSHA512";
    private String signatureAlgorithm = "SHA256withRSA";

    private int secretBytes = 64;
    private int tokenttl = 3600 * 24 * 7;

    static {
        StartupListener.subscibe(core -> {
            logger = core.logger(SecuritySettings.class);
        });
    }

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
            keystores.remove(store);
            keystores.add(store);
        });
    }

    /**
     * @param storeId name of the keystore to retrieve: the mapped shortname of the filename with extension.
     * @return a keystore if it is loaded, if no keystore is added with the given shortname uses a
     * self signed certificate. If it fails to load a keystore then the application shuts down.
     */
    @JsonIgnore
    public synchronized TrustAndKeyProvider getKeystore(String storeId) {
        if (!loadedKeyStores.containsKey(storeId)) {
            Optional<KeyStoreReference> store = getByName(storeId);

            if (store.isPresent()) {
                loadKeystore(store.get().setShortName(storeId));
            } else {
                loadedKeyStores.put(storeId, generateSelfSigned());
            }
        }
        return loadedKeyStores.get(storeId);
    }

    /**
     * Retrieve a keystore given its short name (ID).
     *
     * @param storeId the store id to retrieve.
     * @return a keystore that matches the given id, empty otherwise.
     */
    public Optional<KeyStoreReference> getByName(String storeId) {
        for (KeyStoreReference keystore : keystores) {
            if (keystore.getShortName().equals(storeId)) {
                return Optional.of(keystore);
            }
        }
        return Optional.empty();
    }

    private void loadKeystore(KeyStoreReference store) {
        try {
            loadedKeyStores.put(store.getShortName(),
                    TrustAndKeyProvider.of(new JksOptions()
                            .setPath(store.getPath())
                            .setValue(Buffer.buffer(Files.readAllBytes(Paths.get(store.getPath()).toAbsolutePath())))
                            .setPassword(store.getPassword())));
        } catch (Throwable e) {
            // failed to load keystore due to wrong password or missing file etc.
            // cannot recover from this in a safe manner: shut down.
            logger.onError(e);
            System.exit(0);
        }
    }

    private TrustAndKeyProvider generateSelfSigned() {
        logger.event(LOG_SECURITY, Level.WARNING).send(getMissingKeyStore());
        return TrustAndKeyProvider.of(new TestCertificate(CoreStrings.GITHUB));
    }

    /**
     * @return a list of configured keystores.
     */
    public Set<KeyStoreReference> getKeystores() {
        return keystores;
    }

    /**
     * @param keystores keystores to  set
     * @return fluent
     */
    public SecuritySettings setKeystores(Set<KeyStoreReference> keystores) {
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
     * @return argon2 parameters used for password hashing.
     */
    public ArgonSettings getArgon() {
        return argon;
    }

    /**
     * @param argon the argon2 parameters used for password hashing.
     */
    public void setArgon(ArgonSettings argon) {
        this.argon = argon;
    }

    /**
     * @return the signature algorithm to use for signatures.
     */
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Sets the signature algorithm to use for signing.
     * <p>
     * supported by the default provider:
     * - SHA1withDSA
     * - SHA1withRSA
     * - SHA256withRSA
     *
     * @param signatureAlgorithm the algorithm identifier to use.
     */
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    /**
     * @return the HMAC algorithm identifier used to create HMAC tokens.
     */
    public String getHmacAlgorithm() {
        return hmacAlgorithm;
    }

    /**
     * @param hmacAlgorithm the HMAC algorithm used to create HMAC tokens,
     *                      the specified algorithm must be available in the JVM.
     */
    public void setHmacAlgorithm(String hmacAlgorithm) {
        this.hmacAlgorithm = hmacAlgorithm;
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

    /**
     * @return the time to live for generated tokens in seconds.
     */
    public int getTokenttl() {
        return tokenttl;
    }

    /**
     * @param tokenttl the time to live for generated tokens in seconds.
     * @return fluent.
     */
    public SecuritySettings setTokenttl(int tokenttl) {
        this.tokenttl = tokenttl;
        return this;
    }
}
