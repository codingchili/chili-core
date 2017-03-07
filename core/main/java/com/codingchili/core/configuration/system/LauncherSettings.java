package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

import com.codingchili.core.configuration.BaseConfigurable;

import static com.codingchili.core.configuration.CoreStrings.PATH_LAUNCHER;

/**
 * @author Robin Duda
 *
 * Contains the settings for the launcher.
 */
public class LauncherSettings extends BaseConfigurable {
    private String version = "CORE-1.0.0-PR";
    private HashMap<String, List<String>> blocks = defaultBlockConfiguration();
    private HashMap<String, String> hosts = defaultHostConfiguration();

    public LauncherSettings() {
        super(PATH_LAUNCHER);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, List<String>> getBlocks() {
        return blocks;
    }

    public void setBlocks(HashMap<String, List<String>> blocks) {
        this.blocks = blocks;
    }

    public HashMap<String, String> getHosts() {
        return hosts;
    }

    public void setHosts(HashMap<String, String> hosts) {
        this.hosts = hosts;
    }

    @JsonIgnore
    public HashMap<String, String> hosts() {
        return getHosts();
    }

    @JsonIgnore
    public HashMap<String, List<String>> blocks() {
        return getBlocks();
    }

    private HashMap<String, List<String>> defaultBlockConfiguration() {
        HashMap<String, List<String>> map = new HashMap<>();
        map.put("default", new ArrayList<>());
        return map;
    }

    private HashMap<String, String> defaultHostConfiguration() {
        HashMap<String, String> map = new HashMap<>();
        map.put("localhost", "default");
        return map;
    }

}
