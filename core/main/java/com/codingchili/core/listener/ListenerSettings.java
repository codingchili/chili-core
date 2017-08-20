package com.codingchili.core.listener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.http.HttpServerOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private String defaultTarget = "default";
    private int port = 8080;
    private int timeout = 3000;
    private int maxRequestBytes = 256;

    /**
     * @return timeout in MS after the router times out the request.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout in MS which the router times out the request.
     * @return fluent
     */
    public ListenerSettings setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return the maximum number of bytes in a request.
     */
    public int getMaxRequestBytes() {
        return maxRequestBytes;
    }

    /**
     * @param maxRequestBytes sets the maximum number of bytes in a single request.
     * @return fluent
     */
    public ListenerSettings setMaxRequestBytes(int maxRequestBytes) {
        this.maxRequestBytes = maxRequestBytes;
        return this;
    }

    /**
     * @return the type of the listener, for example tcp or udp.
     */
    public WireType getType() {
        return type;
    }

    /**
     * @param type the type to set for the listener.
     * @return fluent
     */
    public ListenerSettings setType(WireType type) {
        this.type = type;
        return this;
    }

    /**
     * @return the port the listener is to be activated on.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port the listener is to be activated on.
     * @return fluent
     */
    public ListenerSettings setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return api mappings for this listener
     */
    public Map<String, Endpoint> getApi() {
        return api;
    }

    /**
     * @param api the api mappings to set for the listener
     * @return fluent
     */
    public ListenerSettings setApi(Map<String, Endpoint> api) {
        this.api = api;
        return this;
    }

    /**
     * @param route the handler of the route/identity/target to map
     * @param api   the endpoint the request is mapped to.
     * @return fluent
     */
    public ListenerSettings addApi(String route, Endpoint api) {
        this.api.put(route, api);
        return this;
    }

    /**
     * @return HttpOptions created from the listeners settings.
     */
    @JsonIgnore
    public HttpServerOptions getHttpOptions() {
        return httpOptions;
    }

    /**
     * @param httpOptions sets the HttpOptions for the listener if applicable
     * @return fluent
     */
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

    /**
     * @return get the default target to use if unspecified.
     */
    public String getDefaultTarget() {
        return defaultTarget;
    }

    /**
     * @param defaultTarget sets the default target where target is unspecified.
     * @return fluent
     */
    public ListenerSettings setDefaultTarget(String defaultTarget) {
        this.defaultTarget = defaultTarget;
        return this;
    }
}
