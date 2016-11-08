package com.codingchili.core.Configuration.System;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;

import com.codingchili.core.Configuration.Configurable;

import static com.codingchili.core.Configuration.Strings.PATH_LAUNCHER;

/**
 * @author Robin Duda
 */
public class LauncherSettings implements Configurable {
    private String version;
    private String path = PATH_LAUNCHER;
    private HashMap<String, List<String>> blocks;
    private HashMap<String, String> hosts = new HashMap<>();

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

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }
}
