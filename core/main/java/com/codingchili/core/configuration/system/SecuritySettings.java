package com.codingchili.core.configuration.system;


import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.CoreStrings;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author Robin Duda
 *         <p>
 *         Contains mappings of security dependencies between services.
 *         <p>
 *         For example: service A may depend on a shared secret with service B,
 *         or depend on service B generating a token of its secret.
 *         <p>
 *         To establish such dependencies, a dependency is added to service A
 *         with a path or a regex to match the path of service B. The
 *         {@link AuthenticationDependency} class contains the type of
 *         configuration that is requested, for example a token or shared secret.
 */
public class SecuritySettings extends BaseConfigurable {
    private HashMap<String, AuthenticationDependency> dependencies = new HashMap<>();
    private int secretBytes = 64;
    private int tokenttl = 3600 * 24 * 7;

    public SecuritySettings() {
        super(CoreStrings.PATH_SECURITY);
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
    public HashMap<String, AuthenticationDependency> getDependencies() {
        return dependencies;
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
     * @param dependencies set the security configuration dependencies.
     */
    public void setDependencies(HashMap<String, AuthenticationDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public int getTokenttl() {
        return tokenttl;
    }

    public SecuritySettings setTokenttl(int tokenttl) {
        this.tokenttl = tokenttl;
        return this;
    }
}
