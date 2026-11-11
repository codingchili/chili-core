package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Contains the settings for the launcher.
 */
public class LauncherSettings implements Configurable {
    private String application = "launcher";
    private String version = CoreStrings.VERSION;
    private String author = CoreStrings.AUTHOR;
    private boolean warnOnDefaultsLoaded = false;
    private String configurationDirectory = CoreStrings.DIR_CONFIG;
    private ClusterSettings cluster = new ClusterSettings();
    private HashMap<String, List<String>> blocks = defaultBlockConfiguration();
    private HashMap<String, String> hosts = defaultHostConfiguration();
    private HashMap<String, List<String>> configs = new HashMap<>();

    @Override
    public String getPath() {
        return PATH_LAUNCHER;
    }

    /**
     * @return the launcher version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version sets the launcher version.
     * @return fluent
     */
    public LauncherSettings setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return true if a warning should be logged when a configuration file is not found
     * and the configurable is loaded from the java class. To avoid these errors
     * add your java configurable to the Configuration.
     */
    public boolean isWarnOnDefaultsLoaded() {
        return warnOnDefaultsLoaded;
    }

    /**
     * @param warnOnDefaultsLoaded set to true to disable warnings when default settings
     *                             are loaded. Default settings will be loaded when the
     *                             path to a configurable is not available.
     * @return fluent
     */
    public LauncherSettings setWarnOnDefaultsLoaded(boolean warnOnDefaultsLoaded) {
        this.warnOnDefaultsLoaded = warnOnDefaultsLoaded;
        return this;
    }

    /**
     * @return get the configured deployment blocks.
     */
    public HashMap<String, List<String>> getBlocks() {
        return blocks;
    }

    /**
     * @param blocks set the configured deployment blocks.
     * @return fluent
     */
    public LauncherSettings setBlocks(HashMap<String, List<String>> blocks) {
        this.blocks = blocks;
        return this;
    }

    /**
     * @param block name of the block to retrieve
     * @return a list of services attached to the block
     */
    @JsonIgnore
    public List<String> getBlock(String block) {
        if (blocks.containsKey(block)) {
            return blocks.get(block);
        } else {
            throw new IllegalArgumentException(getBlockNotConfigured(block));
        }
    }

    /**
     * @param name   the name of the service block to add
     * @param blocks a list of nodes to be deployed
     * @return fluent
     */
    public LauncherSettings addBlock(String name, List<String> blocks) {
        this.blocks.put(name, blocks);
        return this;
    }

    /**
     * @return a list of hosts mapped to blocks.
     */
    public HashMap<String, String> getHosts() {
        return hosts;
    }

    /**
     * @param hosts sets the host to block mapping.
     * @return fluent
     */
    public LauncherSettings setHosts(HashMap<String, String> hosts) {
        this.hosts = hosts;
        return this;
    }

    /**
     * @param host  the host that should be mapped to a service block
     * @param block the block that the host should be mapped to
     * @return fluent
     */
    public LauncherSettings addHost(String host, String block) {
        if (blocks.containsKey(block)) {
            this.hosts.put(host, block);
        } else {
            throw new IllegalArgumentException(getBlockNotConfigured(block));
        }
        return this;
    }

    /**
     * @return the configuration files each deployable reads, by its class name: path patterns relative to the
     * configuration directory without the extension, as the keys of the security dependencies (for example
     * {@code service/authentication} or {@code realm/[^/]*}). {@code generate --encrypt} encrypts the secrets of a
     * file to the nodes that run a block with a deployable that reads it, {@code bake} writes the files of each block.
     */
    public HashMap<String, List<String>> getConfigs() {
        return configs;
    }

    /**
     * @param configs the configuration files of each deployable, see {@link #getConfigs()}.
     * @return fluent
     */
    public LauncherSettings setConfigs(HashMap<String, List<String>> configs) {
        this.configs = configs;
        return this;
    }

    /**
     * @param deployable the class name of a deployable.
     * @param patterns   the configuration files it reads, see {@link #getConfigs()}.
     * @return fluent
     */
    public LauncherSettings addConfigs(String deployable, String... patterns) {
        configs.put(deployable, new ArrayList<>(List.of(patterns)));
        return this;
    }

    /**
     * @return the path to the configuration directory to use.
     */
    public String getConfigurationDirectory() {
        return configurationDirectory;
    }

    /**
     * @param configurationDirectory the path to the configuration directory to use.
     */
    public void setConfigurationDirectory(String configurationDirectory) {
        this.configurationDirectory = configurationDirectory;
    }

    /**
     * @return the configured application name.
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application set the name of the application.
     * @return fluent
     */
    public LauncherSettings setApplication(String application) {
        this.application = application;
        return this;
    }

    /**
     * @return a list of hosts mapped to blocks.
     */
    @JsonIgnore
    public HashMap<String, String> hosts() {
        return getHosts();
    }

    /**
     * @return names of blocks, each with a list of services attached.
     */
    @JsonIgnore
    public HashMap<String, List<String>> blocks() {
        return getBlocks();
    }

    /**
     * Sets the given service class as the default block to be deployed.
     *
     * @param service the service to be deployed
     * @return fluent
     */
    @JsonIgnore
    public LauncherSettings deployable(Class service) {
        blocks.put(ID_DEFAULT, Collections.singletonList(service.getName()));
        return this;
    }

    /**
     * @return adds configuration for the default block.
     */
    private HashMap<String, List<String>> defaultBlockConfiguration() {
        HashMap<String, List<String>> map = new HashMap<>();
        map.put("default", new ArrayList<>());
        return map;
    }

    /**
     * @return adds configuration for the default host.
     */
    private HashMap<String, String> defaultHostConfiguration() {
        HashMap<String, String> map = new HashMap<>();
        // map.put("localhost", "default");
        return map;
    }

    /**
     * @return the clustering settings: whether to cluster, the cluster name, discovery and the advertised address.
     */
    public ClusterSettings getCluster() {
        return cluster;
    }

    /**
     * @param cluster the clustering settings.
     * @return fluent
     */
    public LauncherSettings setCluster(ClusterSettings cluster) {
        this.cluster = (cluster == null) ? new ClusterSettings() : cluster;
        return this;
    }

    /**
     * @return true if the launcher starts a clustered Vert.x: {@link ClusterSettings#isActive()}, which applies
     * the system property and environment overrides.
     */
    @JsonIgnore
    public boolean isClustered() {
        return cluster.isActive();
    }

    /**
     * @param clustering enables or disables clustering.
     * @return fluent
     * @deprecated use {@code cluster.enabled}: {@link #getCluster()} and {@link ClusterSettings#setEnabled(boolean)}.
     * Still read from configuration files as the top level key {@code clustered}.
     */
    @Deprecated
    @JsonProperty("clustered")
    public LauncherSettings setClustered(boolean clustering) {
        cluster.setEnabled(clustering);
        return this;
    }

    /**
     * @return author string used in startup text.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author set the author string visible in the startup text.
     * @return the author of the application
     */
    public LauncherSettings setAuthor(String author) {
        this.author = author;
        return this;
    }
}
