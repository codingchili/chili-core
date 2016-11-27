package com.codingchili.core.configuration.system;

/**
 * @author Robin Duda
 *         <p>
 *         configuration settings for a remote storage.
 */
public class RemoteStorage {
    private String host;
    private String db_name;
    private Integer port;

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
