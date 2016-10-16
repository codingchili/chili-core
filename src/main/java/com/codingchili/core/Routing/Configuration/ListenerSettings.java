package com.codingchili.core.Routing.Configuration;

import com.codingchili.core.Routing.Model.WireType;

/**
 * @author Robin Duda
 */
public class ListenerSettings extends RoutingSettings {
    private WireType type;
    private int port;
    private int timeout;
    private int maxRequestBytes;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRequestBytes() {
        return maxRequestBytes;
    }

    public void setMaxRequestBytes(int maxRequestBytes) {
        this.maxRequestBytes = maxRequestBytes;
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
