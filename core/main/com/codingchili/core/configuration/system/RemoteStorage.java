package com.codingchili.core.configuration.system;

/**
 * @author Robin Duda
 *         <p>
 *         configuration settings for a remote storage.
 */
public class RemoteStorage {
    private String host = "localhost";
    private String db_name = "DEFAULT_DB";
    private Integer port = 27017;

    public RemoteStorage(String host, int port, String db_name) {
        this.host = host;
        this.port = port;
        this.db_name = db_name;
    }

    public RemoteStorage() {}

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
