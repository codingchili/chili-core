package com.codingchili.logging.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.JsonMap;

/**
 * @author Robin Duda
 * Contains settings for the logserver.
 */
public class LogServerSettings extends ServiceConfigurable {
    static final String PATH_LOGSERVER = Strings.getService("logserver");
    private byte[] secret = new byte[]{0x53, 0x48};
    private Boolean console = true;
    private String db = "logging";
    private String collection = "events";
    private String plugin = JsonMap.class.getCanonicalName();

    public LogServerSettings() {
        this.path = PATH_LOGSERVER;
    }

    /**
     * @return true if received messages are to be printed to console.
     */
    public Boolean getConsole() {
        return console;
    }

    /**
     * @param console indicates whether received messages are printed to console.
     */
    public void setConsole(Boolean console) {
        this.console = console;
    }

    /**
     * @return the secret key for logging tokens.
     */
    public byte[] getSecret() {
        return secret;
    }

    /**
     * @param secret sets the secret key for logging tokens.
     */
    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    /**
     * @return the database that logs are stored in.
     */
    public String getDb() {
        return db;
    }

    /**
     * @param db sets the database that logs are stored in.
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @return the handler of the collection that messages are stored in.
     */
    public String getCollection() {
        return collection;
    }

    /**
     * @param collection sets the handler of the collection where messages are stored.
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * @return the plugin to use for storing received log messages.
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * @param plugin sets the plugin to use for storing received log messages.
     */
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}