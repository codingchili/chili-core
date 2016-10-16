package com.codingchili.core.Authentication.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.core.Configuration.Strings.PATH_AUTHSERVER;


public class AuthServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private byte[] clientSecret;
    private byte[] realmSecret;
    private List<String> realms = new ArrayList<>();

    public boolean isTrustedRealm(String name) {
        boolean result = false;

        for (Object object : realms) {
            if (object.equals(name))
                result = true;
        }

        return result;
    }

    @Override
    public String getPath() {
        return PATH_AUTHSERVER;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }


    public List<String> getRealms() {
        return realms;
    }

    protected void setRealms(List<String> realms) {
        this.realms = realms;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    public byte[] getRealmSecret() {
        return realmSecret;
    }

    public AuthServerSettings setRealmSecret(byte[] realmSecret) {
        this.realmSecret = realmSecret;
        return this;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public AuthServerSettings setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
}
