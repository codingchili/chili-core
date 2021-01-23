package com.codingchili.core.configuration.system;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.configuration.CoreStrings;

/**
 * configuration settings for a remote storage.
 */
public class RemoteStorage {
    private JsonObject properties = new JsonObject();
    private String host = CoreStrings.LOCALHOST;
    private String database = CoreStrings.DEFAULT_DB;
    private Integer port = 27017;
    private int persistInterval = 3000;
    private boolean persisted = false;
    private boolean secure = false;

    /**
     * @param host     the hostname of the remote storage
     * @param port     the port of the remote storage
     * @param database the database identifier of the remote storage.
     */
    public RemoteStorage(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }

    public RemoteStorage() {
    }

    /**
     * @return true if the contents of the database is persisted to disk.
     * does not apply to storages that are persisted to disk by default.
     * May not be supported.
     */
    public boolean isPersisted() {
        return persisted;
    }

    /**
     * @param persisted indicates that the contents of volatile storages should
     *                  be flushed to disk in some manner. Does not apply to
     *                  storages that store their content on disk.
     *                  May not be supported.
     * @return fluent
     */
    public RemoteStorage setPersisted(boolean persisted) {
        this.persisted = persisted;
        return this;
    }

    /**
     * @return the database identifier of the remote configuration.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @param database sets the database identifier of the remote storage.
     * @return fluent
     */
    public RemoteStorage setDatabase(String database) {
        this.database = database;
        return this;
    }

    /**
     * @return Returns the hostname of the remote storage.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host sets the hostname of the remote storage.
     * @return fluent
     */
    public RemoteStorage setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * @return the port number of the remote storage.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port sets the port number of the remote storage.
     * @return fluent
     */
    public RemoteStorage setPort(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * @return the interval in MS which a database should be persisted for non-disk
     * storages. Only applies if persist is set. May not be supported.
     */
    public int getPersistInterval() {
        return persistInterval;
    }

    /**
     * @param persistInterval sets the interval in ms which a database should be persisted for
     *                        non-disk storages. Only applies if persist is set.
     *                        May not be supported.
     * @return fluent.
     */
    public RemoteStorage setPersistInterval(int persistInterval) {
        this.persistInterval = persistInterval;
        return this;
    }

    /**
     * @return true if the storage plugin should attempt to use a secure connection.
     * If the plugin supports security and fails to enable it an error must be thrown
     * and storage initialization fail.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @param secure indicates that this storage must be loaded with a secure
     *               connection.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @return extra configuration properties used to customize the database.
     */
    public JsonObject getProperties() {
        return properties;
    }

    /**
     * @param properties extra configuration properties used to customize the database.
     */
    public void setProperties(JsonObject properties) {
        this.properties = properties;
    }


}
