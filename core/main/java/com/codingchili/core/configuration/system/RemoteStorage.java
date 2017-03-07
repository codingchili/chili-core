package com.codingchili.core.configuration.system;

/**
 * @author Robin Duda
 *         <p>
 *         configuration settings for a remote storage.
 */
public class RemoteStorage {
    private String host = "localhost";
    private String database = "CORE_DB";
    private Integer port = 27017;

    // Indicates that a jsonmap should persist its contents to disk.
    private boolean persisted = true;
    private int persistInterval = 250;

    public RemoteStorage(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }

    public RemoteStorage() {
    }

    public boolean isPersisted() {
        return persisted;
    }

    public RemoteStorage setPersisted(boolean persisted) {
        this.persisted = persisted;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public RemoteStorage setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getHost() {
        return host;
    }

    public RemoteStorage setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public RemoteStorage setPort(Integer port) {
        this.port = port;
        return this;
    }

    public int getPersistInterval() {
        return persistInterval;
    }

    public RemoteStorage setPersistInterval(int persistInterval) {
        this.persistInterval = persistInterval;
        return this;
    }
}
