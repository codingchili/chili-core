package com.codingchili.logging.configuration;

import com.codingchili.common.Strings;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.JsonMap;

/**
 * @author Robin Duda
 *         Contains settings for the logserver.
 */
public class LogServerSettings extends ServiceConfigurable {
    public static final String PATH_LOGSERVER = Strings.getService("logserver");
    private byte[] secret = new byte[] {0x53,0x48};
    private Boolean console = true;
    private String db = "logging";
    private String collection = "events";
    private String plugin = JsonMap.class.getCanonicalName();

    public LogServerSettings() {
        this.path = PATH_LOGSERVER;
    }

    public Boolean getConsole() {
        return console;
    }

    public void setConsole(Boolean console) {
        this.console = console;
    }

    public byte[] getSecret() {
        return secret;
    }

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}