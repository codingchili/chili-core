package com.codingchili.core.configuration.system;

import com.codingchili.core.configuration.Configurable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Contains the settings for the launcher.
 */
public class LauncherSettings implements Configurable {
    private String application = "launcher";
    private String version = "CORE-1.0.6-SNAPSHOT";
    private String author = "Robin Duda \u00a9 2017";
    private boolean clustered;
    private HashMap<String, List<String>> blocks = defaultBlockConfiguration();
    private HashMap<String, String> hosts = defaultHostConfiguration();

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
     */
    public LauncherSettings setVersion(String version) {
        this.version = version;
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
     */
    public LauncherSettings setHosts(HashMap<String, String> hosts) {
        this.hosts = hosts;
        return this;
    }

    /**
     * @param host  the host that should be mapped to a service block
     * @param block the block that the host should be mapped to
     */
    public LauncherSettings addHost(String host, String block) {
        if (blocks.containsKey(block)) {
            this.hosts.put(block, host);
        } else {
            throw new IllegalArgumentException(getBlockNotConfigured(block));
        }
        return this;
    }

    /**
     * @return the configured application name.
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application set the name of the application.
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
        map.put("localhost", "default");
        return map;
    }

    /**
     * @return true if clustering is enabled.
     */
    public boolean isClustered() {
        return clustered;
    }

    /**
     * @param clustering enables or disables clustering.
     * @return fluent
     */
    public LauncherSettings setClustered(boolean clustering) {
        this.clustered = clustering;
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
