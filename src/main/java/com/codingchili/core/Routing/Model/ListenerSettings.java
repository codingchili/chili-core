package com.codingchili.core.Routing.Model;

/**
 * @author Robin Duda
 */
public class ListenerSettings {
    private WireType type;
    private int port;
    private int timeout;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public WireType getType() {
        return type;
    }

    public void setType(WireType type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
