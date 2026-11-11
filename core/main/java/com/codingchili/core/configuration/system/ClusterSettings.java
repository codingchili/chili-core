package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import io.vertx.core.eventbus.EventBusOptions;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Clustering of the launcher (launcher.yaml, {@code cluster}): whether the launcher starts a clustered Vert.x, the
 * name of the Hazelcast cluster, how members find each other and the address other members reach this one at.
 * <p>
 * Discovery: a member list joins by TCP/IP to those hosts only. Without one, cloud (Hazelcast's auto-detection) looks for
 * a cloud environment (AWS, GCP, Azure, Kubernetes) and uses its discovery, and Hazelcast falls back to multicast
 * when it detects none: on a LAN members find each other by multicast. With cloud off, multicast alone
 * (no cloud probes). With both off and no members, the join of the Hazelcast config (cluster.xml) is kept as is.
 * <p>
 * Every setting can be overridden by a system property ({@code chili.cluster.<setting>}) and then an environment
 * variable ({@code CHILI_CLUSTER_<SETTING>}), blank values are ignored. Setting members by an override also
 * turns clustering on, unless enabled is overridden too.
 */
public class ClusterSettings {
    /**
     * the address that the event bus binds to when an advertised host is set: every interface.
     */
    public static final String ANY_ADDRESS = "0.0.0.0";
    private boolean enabled = false;
    private String name = null;
    private boolean multicast = true;
    private boolean cloud = true;
    private List<String> members = new ArrayList<>();
    private String host = null;
    private String bind = null;
    private int port = 0;
    private UnaryOperator<String> environment = System::getenv;

    /**
     * @return true if the launcher starts a clustered Vert.x, with overrides applied.
     */
    @JsonIgnore
    public boolean isActive() {
        Optional<String> override = override(PROPERTY_CLUSTER_ENABLED, ENV_CLUSTER_ENABLED);

        if (override.isPresent()) {
            return parseBoolean(override.get(), PROPERTY_CLUSTER_ENABLED);
        } else if (override(PROPERTY_CLUSTER_MEMBERS, ENV_CLUSTER_MEMBERS).isPresent()) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * @return true if clustering is enabled in the configuration, without overrides: see {@link #isActive()}.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled true to start a clustered Vert.x when the launcher deploys.
     * @return fluent
     */
    public ClusterSettings setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return the configured name of the cluster, null when not set.
     */
    public String getName() {
        return name;
    }

    /**
     * Members only join a cluster with the same name: give every deploy on a network its own.
     *
     * @param name the name of the Hazelcast cluster, null for the application name of the launcher.
     * @return fluent
     */
    public ClusterSettings setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param fallback the name used when none is configured, the application name of the launcher.
     * @return the name of the cluster with overrides applied.
     */
    public String resolveName(String fallback) {
        return override(PROPERTY_CLUSTER_NAME, ENV_CLUSTER_NAME)
                .orElse((name == null || name.isBlank()) ? fallback : name.trim());
    }

    /**
     * @return true if members are found by multicast (a LAN) when no member list is set.
     */
    public boolean isMulticast() {
        return multicast;
    }

    /**
     * @param multicast true to find members by multicast when no member list is set.
     * @return fluent
     */
    public ClusterSettings setMulticast(boolean multicast) {
        this.multicast = multicast;
        return this;
    }

    /**
     * @return true if Hazelcast's auto-detection of cloud environments is used when no member list is set.
     */
    public boolean isCloud() {
        return cloud;
    }

    /**
     * @param cloud true to use the discovery of a detected cloud environment (AWS, GCP, Azure, Kubernetes)
     *                      when no member list is set. Hazelcast falls back to multicast when none is detected.
     * @return fluent
     */
    public ClusterSettings setCloud(boolean cloud) {
        this.cloud = cloud;
        return this;
    }

    /**
     * @return the configured members, hosts optionally with a port.
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * @param members the hosts of the members to join by TCP/IP (optionally host:port), for networks without
     *                multicast. Entries may hold several hosts separated by commas. Empty: discovery.
     * @return fluent
     */
    public ClusterSettings setMembers(List<String> members) {
        this.members = (members == null) ? new ArrayList<>() : members;
        return this;
    }

    /**
     * @return the members to join with overrides applied, an empty list for discovery.
     */
    public List<String> resolveMembers() {
        List<String> list = override(PROPERTY_CLUSTER_MEMBERS, ENV_CLUSTER_MEMBERS)
                .map(List::of)
                .orElse(members);
        List<String> resolved = new ArrayList<>();

        for (String entry : list) {
            if (entry != null) {
                Arrays.stream(entry.split(","))
                        .map(String::trim)
                        .filter(member -> !member.isEmpty())
                        .forEach(resolved::add);
            }
        }
        return resolved;
    }

    /**
     * @return the address that other members reach this one at, null when not set.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the address that other members reach this one at, when it is not an address of this machine that
     * Hazelcast picks itself: NAT, containers on separate hosts that publish their ports. It is Hazelcast's
     * public address and the advertised host of the event bus, which then binds to every interface.
     * Unset: Hazelcast's own address for both, right on one network (a LAN, one Docker network).
     *
     * @param host the advertised address of this member.
     * @return fluent
     */
    public ClusterSettings setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * @return the configured address that the event bus listens on, null when not set.
     */
    public String getBind() {
        return bind;
    }

    /**
     * @param bind the address (interface) that the clustered event bus listens on, for example 0.0.0.0 for every
     *             interface. Unset: every interface when a host is set, otherwise Hazelcast's member address.
     * @return fluent
     */
    public ClusterSettings setBind(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * @return the configured port of the clustered event bus, 0 for a random port.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port a fixed port for the clustered event bus, to open in a firewall or publish from a
     *             container. Other members connect to the same port. Default 0: a random port.
     * @return fluent
     */
    public ClusterSettings setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @param environment looks up environment variables, {@link System#getenv(String)} outside of tests.
     * @return fluent
     */
    ClusterSettings setEnvironment(UnaryOperator<String> environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Applies the cluster name, the join and the public address to a Hazelcast configuration.
     *
     * @param config       the Hazelcast configuration to update, as loaded from cluster.xml.
     * @param fallbackName the name of the cluster when none is configured.
     * @return the given configuration.
     */
    public Config apply(Config config, String fallbackName) {
        config.setClusterName(resolveName(fallbackName));

        JoinConfig join = config.getNetworkConfig().getJoin();
        List<String> members = resolveMembers();
        boolean multicast = resolveBoolean(PROPERTY_CLUSTER_MULTICAST, ENV_CLUSTER_MULTICAST, this.multicast);
        boolean cloud = resolveBoolean(PROPERTY_CLUSTER_CLOUD, ENV_CLUSTER_CLOUD, this.cloud);

        if (!members.isEmpty()) {
            join.getTcpIpConfig().setEnabled(true).setMembers(members);
            join.getMulticastConfig().setEnabled(false);
            join.getAutoDetectionConfig().setEnabled(false);
        } else if (cloud) {
            // Hazelcast only uses auto-detection when no other join is enabled, and falls back to multicast
            // when it finds no cloud environment: multicast must be off here for the cloud discovery to run.
            join.getTcpIpConfig().setEnabled(false);
            join.getMulticastConfig().setEnabled(false);
            join.getAutoDetectionConfig().setEnabled(true);
        } else if (multicast) {
            join.getTcpIpConfig().setEnabled(false);
            join.getMulticastConfig().setEnabled(true);
            join.getAutoDetectionConfig().setEnabled(false);
        }

        resolveHost().ifPresent(address -> config.getNetworkConfig().setPublicAddress(address));
        return config;
    }

    /**
     * Applies the advertised host, the bind address and the port to the options of the clustered event bus. Unset,
     * Vert.x takes the hosts from the cluster manager (Hazelcast's address) and picks a random port.
     *
     * @param eventBus the event bus options to update.
     * @return the given options.
     */
    public EventBusOptions apply(EventBusOptions eventBus) {
        Optional<String> host = resolveHost();
        Optional<String> bind = override(PROPERTY_CLUSTER_BIND, ENV_CLUSTER_BIND)
                .or(() -> Optional.ofNullable(this.bind).map(String::trim).filter(value -> !value.isEmpty()))
                .or(() -> host.map(address -> ANY_ADDRESS));

        bind.ifPresent(eventBus::setHost);
        host.ifPresent(eventBus::setClusterPublicHost);

        Optional<String> port = override(PROPERTY_CLUSTER_PORT, ENV_CLUSTER_PORT);
        int resolved = this.port;

        if (port.isPresent()) {
            try {
                resolved = Integer.parseInt(port.get());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("event bus port '%s' (%s/%s) is not a number.",
                        port.get(), PROPERTY_CLUSTER_PORT, ENV_CLUSTER_PORT), e);
            }
        }
        if (resolved > 0) {
            eventBus.setPort(resolved);
        }
        return eventBus;
    }

    private Optional<String> resolveHost() {
        return override(PROPERTY_CLUSTER_HOST, ENV_CLUSTER_HOST)
                .or(() -> Optional.ofNullable(host).map(String::trim).filter(value -> !value.isEmpty()));
    }

    private boolean resolveBoolean(String property, String env, boolean configured) {
        return override(property, env).map(value -> parseBoolean(value, property)).orElse(configured);
    }

    private Optional<String> override(String property, String env) {
        for (String value : new String[]{System.getProperty(property), environment.apply(env)}) {
            if (value != null && !value.isBlank()) {
                return Optional.of(value.trim());
            }
        }
        return Optional.empty();
    }

    private static boolean parseBoolean(String value, String property) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "on", "yes", "1" -> true;
            case "false", "off", "no", "0" -> false;
            default -> throw new IllegalArgumentException(
                    String.format("'%s' (%s) is not on/off or true/false.", value, property));
        };
    }
}
