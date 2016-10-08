package com.codingchili.core.Authentication.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;

import static com.codingchili.core.Configuration.Strings.PATH_AUTHSERVER;


public class AuthServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private byte[] clientSecret;
    private byte[] realmSecret;
    private DatabaseSettings database;
    private String[] realms;

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


    public String[] getRealms() {
        return realms;
    }

    protected void setRealms(String[] realms) {
        this.realms = realms;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    public DatabaseSettings getDatabase() {
        return database;
    }

    protected void setDatabase(DatabaseSettings database) {
        this.database = database;
    }

    public byte[] getRealmSecret() {
        return realmSecret;
    }

    public void setRealmSecret(byte[] realmSecret) {
        this.realmSecret = realmSecret;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }
}
