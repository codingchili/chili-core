package com.codingchili.core.listener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.http.HttpServerOptions;

import java.util.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.security.TrustAndKeyProvider;

import static com.codingchili.core.files.Configurations.security;

/**
 * Settings for transport listeners.
 */
public class ListenerSettings {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_MAX_REQUEST_BYTES = 1024;
    private HttpServerOptions httpOptions = null;
    private Map<String, Endpoint> api = new HashMap<>();
    private WireType type = WireType.REST;
    private final Set<Integer> actualPorts = new HashSet<>();
    private String defaultTarget = "default";
    private String keystore = CoreStrings.DEFAULT_KEYSTORE;
    private String basePath = null;
    private boolean binaryWebsockets = true;
    private boolean secure = true;
    private boolean alpn = false;
    private int port = 8080;
    private int timeout = DEFAULT_TIMEOUT;
    private int maxRequestBytes = DEFAULT_MAX_REQUEST_BYTES;

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
     * @return the name of the keystore to use if security is enabled.
     */
    public String getKeystore() {
        return keystore;
    }

    /**
     * @param keystore the name of the keystore to use, sets secure to true when called.
     *                 The certificate must be added using #{@link SecuritySettings#addKeystore()}
     *                 before it is available. if not added will throw an exception.
     * @return fluent.
     */
    public ListenerSettings setKeystore(String keystore) {
        this.keystore = keystore;
        this.secure = true;
        return this;
    }

    /**
     * @return if true indicates that websocket frames should be written as binary.
     */
    public boolean isBinaryWebsockets() {
        return binaryWebsockets;
    }

    /**
     * @param binaryWebsockets set to true to write websocket frames as binary,
     *                         otherwise writes a text frame.
     * @return fluent.
     */
    public ListenerSettings setBinaryWebsockets(boolean binaryWebsockets) {
        this.binaryWebsockets = binaryWebsockets;
        return this;
    }

    /**
     * @return true if TLS security is enabled on listeners that supports it.
     * Security options are set on the default httpClientOptions.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @param secure if set to false disables transport security for the
     *               listeners that supports it.
     * @return fluent
     */
    public ListenerSettings setSecure(boolean secure) {
        this.secure = secure;
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
     * @return true if ALPN is enabled - this is required to support HTTP/2.
     */
    public boolean isAlpn() {
        return alpn;
    }

    /**
     * @param alpn if true attempt to use ALPN - also requires that secure is set to true.
     */
    public void setAlpn(boolean alpn) {
        this.alpn = alpn;
    }

    /**
     * @return HttpOptions created from the listeners settings.
     */
    @JsonIgnore
    public HttpServerOptions getHttpOptions() {

        if (httpOptions == null) {
            httpOptions = new HttpServerOptions()
                    .setUseAlpn(alpn)
                    .setCompressionSupported(true)
                    .setSsl(secure);

            if (secure) {
                TrustAndKeyProvider provider = security().getKeystore(keystore);
                httpOptions.setTrustOptions(provider.trustOptions())
                        .setKeyCertOptions(provider.keyCertOptions());
            }
        }
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

    private static ListenerSettings defaultSettings = new ListenerSettings();

    /**
     * @return static supplier of the default settings; used to avoid instantiating
     * a new settings object on every read of setting properties in listeners
     * where no settings has been configured.
     */
    public static ListenerSettings getDefaultSettings() {
        return defaultSettings;
    }

    /**
     * @return a regex that matches the basePath for received calls. When using
     * #{@link com.codingchili.core.listener.transport.RestRequest} the basePath will
     * not be considered when mapping the URL to target/route.
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * @param basePath see #{@link #getBasePath()}
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
