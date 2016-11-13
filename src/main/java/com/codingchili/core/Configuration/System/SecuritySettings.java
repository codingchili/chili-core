package com.codingchili.core.Configuration.System;


import java.util.HashMap;

import com.codingchili.core.Configuration.BaseConfigurable;
import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Maps services to authentication dependencies.
 */
public class SecuritySettings extends BaseConfigurable {
    private HashMap<String, AuthenticationDependency> dependencies = new HashMap<>();
    private int secretBytes = 64;

    public SecuritySettings() {
        super(Strings.PATH_SECURITY);
    }

    public int getSecretBytes() {
        return secretBytes;
    }

    public void setSecretBytes(int secretBytes) {
        this.secretBytes = secretBytes;
    }

    public HashMap<String, AuthenticationDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(HashMap<String, AuthenticationDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
