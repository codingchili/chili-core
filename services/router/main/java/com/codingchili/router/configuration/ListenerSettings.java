package com.codingchili.router.configuration;

import com.codingchili.router.model.Endpoint;
import com.codingchili.router.model.WireType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.http.HttpServerOptions;

import java.util.*;

/**
 * @author Robin Duda
 *         <p>
 *         Settings for transport listeners.
 */
public class ListenerSettings {
    private HttpServerOptions httpOptions = new HttpServerOptions();
    private Map<String, Endpoint> api = new HashMap<>();
    private WireType type = WireType.REST;
    private Set<Integer> actualPorts = new HashSet<>();
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

    public Map<String, Endpoint> getApi() {
        return api;
    }

    public ListenerSettings setApi(Map<String, Endpoint> api) {
        this.api = api;
        return this;
    }

    @JsonIgnore
    public HttpServerOptions getHttpOptions() {
        return httpOptions;
    }

    public ListenerSettings setHttpOptions(HttpServerOptions httpOptions) {
        this.httpOptions = httpOptions;
        return this;
    }

    /**
     * Adds a new mapping from the request target to another endpoint.
     *
     * @param route    the request target to match for this mapping to apply
     * @param endpoint the endpoint to set the request to
     * @return fluent
     */
    public ListenerSettings addMapping(String route, Endpoint endpoint) {
        api.put(route, endpoint);
        return this;
    }

    /**
     * @param port adds a port that the server is listening to. useful if the
     *             port is set to 0.
     */
    public void addListenPort(int port) {
        actualPorts.add(port);
    }

    /**
     * @return a list of ports the listener is listening to. this list contains
     * all ports that are being listened to for the configuration, which may
     * differ from the requested listening port.
     */
    @JsonIgnore
    public Set<Integer> getListenPorts() {
        return actualPorts;
    }
}
