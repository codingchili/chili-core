package com.codingchili.router.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.http.HttpServerOptions;

import java.util.HashMap;

import com.codingchili.router.model.Endpoint;
import com.codingchili.router.model.WireType;

/**
 * @author Robin Duda
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

    public HashMap<String, Endpoint> getApi() {
        return api;
    }

    public void setApi(HashMap<String, Endpoint> api) {
        this.api = api;
    }

    @JsonIgnore
    public HttpServerOptions getHttpOptions() {
        return httpOptions;
    }

    public void setHttpOptions(HttpServerOptions httpOptions) {
        this.httpOptions = httpOptions;
    }
}
