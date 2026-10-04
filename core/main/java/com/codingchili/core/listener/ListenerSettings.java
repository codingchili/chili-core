package com.codingchili.core.listener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.json.JsonObject;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.TrustAndKeyProvider;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.QuicServerConfig;
import io.vertx.core.net.ServerSSLOptions;
import io.vertx.core.net.TcpServerConfig;

import static com.codingchili.core.files.Configurations.security;

/**
 * Settings for transport listeners.
 */
public class ListenerSettings {

    /**
     * The http configuration has two setters for the versions, which cannot both be used to deserialize it.
     */
    private abstract static class HttpServerConfigMixin {
        @JsonProperty("versions")
        abstract HttpServerConfig setVersions(Set<HttpVersion> versions);

        @JsonIgnore
        abstract HttpServerConfig setVersions(HttpVersion... versions);
    }

    // unknown properties are rejected: a misspelled property in a configuration file is not silently ignored.
    private static final ObjectMapper STRICT = Serializer.json.rebuild()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addMixIn(HttpServerConfig.class, HttpServerConfigMixin.class)
            .build();
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_MAX_REQUEST_BYTES = 64 * 1024;
    private JsonObject config = null;
    private HttpServerConfig httpOptions = null;
    private ServerSSLOptions security = null;
    private TcpServerConfig tcp = null;
    private QuicServerConfig quic = null;
    private String quicProtocol = CoreStrings.DEFAULT_QUIC_PROTOCOL;
    private Map<String, Endpoint> api = new HashMap<>();
    private final Set<Integer> actualPorts = new HashSet<>();
    private String defaultTarget = "default";
    private String keystore = CoreStrings.DEFAULT_KEYSTORE;
    private String basePath = null;
    private boolean binaryWebsockets = true;
    private boolean secure = true;
    private boolean alpn = true;
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
     * @return true if ALPN is enabled - this is required to support HTTP/2 over TLS, enabled by default.
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
     * The configuration of the transport of the listener, in the format of the configuration class of the
     * listener: {@link HttpServerConfig} for the REST and websocket listeners, {@link TcpServerConfig} for the TCP
     * listener, {@link QuicServerConfig} for the QUIC listener and {@link DatagramSocketOptions} for the UDP listener,
     * see {@link CoreListener#configType()}. A property that is not part of the configuration class is an error.
     * <pre>{@code
     * port: 8080
     * secure: true
     * config:
     *   versions: [HTTP_1_1, HTTP_2, HTTP_3]
     * }</pre>
     * A configuration object that is set programmatically, for example with {@link #setHttpOptions(HttpServerConfig)},
     * is used instead of this.
     *
     * @return the configuration of the listener as it is read from the configuration file, or null.
     */
    public JsonObject getConfig() {
        return config;
    }

    /**
     * @param config the configuration of the transport of the listener, see {@link #getConfig()}.
     * @return fluent
     */
    public ListenerSettings setConfig(JsonObject config) {
        this.config = config;
        return this;
    }

    /**
     * Reads the configuration of the transport of the listener.
     *
     * @param type     the configuration class of the listener, see {@link CoreListener#configType()}.
     * @param defaults creates the default configuration, used when the listener has no configuration.
     * @param <T>      the type of the configuration.
     * @return the configuration, converted from {@link #getConfig()}.
     * @throws CoreRuntimeException if the configuration has properties that are not part of the configuration class, or
     *                              values that are not valid.
     */
    public <T> T config(Class<T> type, Supplier<T> defaults) {
        if (config == null || config.isEmpty()) {
            return defaults.get();
        }
        try {
            return STRICT.readValue(config.encode(), type);
        } catch (JacksonException e) {
            throw new CoreRuntimeException(CoreStrings.getInvalidListenerConfig(type.getSimpleName(), e.getOriginalMessage()));
        }
    }

    /**
     * @return the configuration of the TCP listener.
     */
    @JsonIgnore
    public TcpServerConfig getTcp() {
        if (tcp != null) {
            return tcp;
        } else {
            return config(TcpServerConfig.class, TcpServerConfig::new);
        }
    }

    public ListenerSettings setTcp(TcpServerConfig tcp) {
        this.tcp = tcp;
        return this;
    }

    /**
     * @return the configuration of the UDP listener.
     */
    @JsonIgnore
    public DatagramSocketOptions getUdp() {
        return config(DatagramSocketOptions.class, DatagramSocketOptions::new);
    }

    /**
     * @return the QUIC server configuration used by the QUIC listener. Port and host are taken
     * from these settings, set {@link QuicServerConfig#setLoadBalanced(boolean)} to deploy one
     * listener instance per configured listener (requires SO_REUSEPORT: Linux or macOS).
     */
    @JsonIgnore
    public QuicServerConfig getQuic() {
        if (quic == null) {
            quic = config(QuicServerConfig.class, QuicServerConfig::new);
        }
        return quic;
    }

    /**
     * @param quic the QUIC server configuration to use for the QUIC listener.
     * @return fluent
     */
    public ListenerSettings setQuic(QuicServerConfig quic) {
        this.quic = quic;
        return this;
    }

    /**
     * @return the application protocol negotiated with ALPN by QUIC clients and servers.
     */
    public String getQuicProtocol() {
        return quicProtocol;
    }

    /**
     * @param quicProtocol the application protocol negotiated with ALPN by QUIC clients and servers,
     *                     clients must offer the same protocol to connect.
     * @return fluent
     */
    public ListenerSettings setQuicProtocol(String quicProtocol) {
        this.quicProtocol = quicProtocol;
        return this;
    }

    /**
     * The configuration of the HTTP listeners. HTTP/1.1 and HTTP/2 are enabled by default. HTTP/3 is enabled as well
     * when the listener is secure, as it requires TLS. HTTP/3 uses QUIC (UDP) and clients first connect with HTTP/1.1 or
     * HTTP/2, and are told about HTTP/3. The versions that are enabled can be set in the
     * configuration, in that case HTTP/3 is not added.
     *
     * @return HttpOptions created from the listeners settings.
     * @throws CoreRuntimeException if HTTP/3 is enabled and the listener is not secure.
     */
    @JsonIgnore
    public HttpServerConfig getHttpOptions() {
        HttpServerConfig http = httpOptions;

        if (http == null) {
            http = config(HttpServerConfig.class, HttpServerConfig::new);

            boolean versionsConfigured = config != null && config.containsKey("versions");
            if (secure && !versionsConfigured) {
                Set<HttpVersion> versions = EnumSet.copyOf(http.getVersions());
                versions.add(HttpVersion.HTTP_3);
                http.setVersions(versions);
            }
        }
        if (!secure && http.getVersions().contains(HttpVersion.HTTP_3)) {
            throw new CoreRuntimeException(CoreStrings.getHttp3RequiresTls());
        }
        return http;
    }

    /**
     * @param httpOptions sets the HttpOptions for the listener if applicable
     * @return fluent
     */
    public ListenerSettings setHttpOptions(HttpServerConfig httpOptions) {
        this.httpOptions = httpOptions;
        return this;
    }

    /**
     * @return the TLS configuration of the listener: the options that have been set, or created from the keystore
     * of the listener, null if the listener is not secure.
     */
    @JsonIgnore
    public ServerSSLOptions getSecurity() {
        if (secure && security != null) {
            return security;
        } else if (secure) {
            ServerSSLOptions ssl = new ServerSSLOptions();
            TrustAndKeyProvider provider = security().getKeystore(keystore);
            ssl.setTrustOptions(provider.trustOptions())
                    .setKeyCertOptions(provider.keyCertOptions())
                    .setUseAlpn(alpn);
            return ssl;
        } else {
            // a non-null ssl options instance enables ssl on the server.
            return null;
        }
    }

    /**
     * @param ssl the TLS configuration to use instead of the keystore of the listener. The listener must be secure.
     * @return fluent
     */
    public ListenerSettings setSecurity(ServerSSLOptions ssl) {
        this.security = ssl;
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
