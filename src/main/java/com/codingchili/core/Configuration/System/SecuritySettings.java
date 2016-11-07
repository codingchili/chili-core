package com.codingchili.core.Configuration.System;


import java.util.HashMap;

import com.codingchili.core.Configuration.WritableConfigurable;

/**
 * @author Robin Duda
 */
public class SecuritySettings extends WritableConfigurable {
    private HashMap<String, AuthenticationDependency> dependencies = new HashMap<>();
    private int secretBytes;

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
