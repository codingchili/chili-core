package com.codingchili.logging.configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Robin Duda
 *         Contains configuration for the elastic cluster when logging is enabled.
 */
public class ElasticSettings {
    private String remote;
    private String index;
    private Integer port;
    private JsonNode template;
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public ElasticSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getRemote() {
        return remote;
    }

    protected void setRemote(String remote) {
        this.remote = remote;
    }

    public String getIndex() {
        return index;
    }

    protected void setIndex(String index) {
        this.index = index;
    }

    public Integer getPort() {
        return port;
    }

    protected void setPort(Integer port) {
        this.port = port;
    }

    public JsonNode getTemplate() {
        return template;
    }

    protected void setTemplate(JsonNode template) {
        this.template = template;
    }
}
