package com.codingchili.router.configuration;

import com.codingchili.router.model.Endpoint;
import com.codingchili.router.model.WireType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.http.HttpServerOptions;

import java.util.HashMap;

/**
 * @author Robin Duda
 *
 * Settings for transport listeners.
 */
public class ListenerSettings {
    private HttpServerOptions httpOptions = new HttpServerOptions();
    private HashMap<String, Endpoint> api = new HashMap<>();
    private WireType type = WireType.REST;
    private int port = 8080;
    private int timeout = 3000;
    private int maxRequestBytes = 64;

    public int getTimeout() {
        return timeout;
    }

    public ListenerSettings setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getMaxRequestBytes() {
        return maxRequestBytes;
    }

    public ListenerSettings setMaxRequestBytes(int maxRequestBytes) {
        this.maxRequestBytes = maxRequestBytes;
        return this;
    }

    public WireType getType() {
        return type;
    }

    public ListenerSettings setType(WireType type) {
        this.type = type;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ListenerSettings setPort(int port) {
        this.port = port;
        return this;
    }

    public HashMap<String, Endpoint> getApi() {
        return api;
    }

    public ListenerSettings setApi(HashMap<String, Endpoint> api) {
        this.api = api;
        return this;
    }

    @JsonIgnore
    public HttpServerOptions getHttpOptions() {
        return httpOptions;
    }

    public void setHttpOptions(HttpServerOptions httpOptions) {
        this.httpOptions = httpOptions;
    }
}
