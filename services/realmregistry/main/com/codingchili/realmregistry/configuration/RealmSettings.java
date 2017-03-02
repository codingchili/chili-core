package com.codingchili.realmregistry.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import com.codingchili.core.configuration.AttributeConfigurable;
import com.codingchili.core.security.RemoteIdentity;
import com.codingchili.core.security.Token;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *         <p>
 *         Contains the settings for a realmName.
 */
@JsonIgnoreProperties({"instances"})
public class RealmSettings extends AttributeConfigurable implements Storable {
    private RemoteIdentity identity;
    private ArrayList<String> classes = new ArrayList<>();
    private ArrayList<String> afflictions = new ArrayList<>();
    private Token authentication;
    private String description;
    private String resources;
    private String remote;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private int players = 0;
    private Boolean trusted;
    private Boolean secure;
    private long updated;
    private String name;

    @Override
    public String id() {
        return name;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public RemoteIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(RemoteIdentity identity) {
        this.identity = identity;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<String> classes) {
        this.classes = classes;
    }

    public ArrayList<String> getAfflictions() {
        return afflictions;
    }

    public void setAfflictions(ArrayList<String> afflictions) {
        this.afflictions = afflictions;
    }

    public Token getAuthentication() {
        return authentication;
    }

    public RealmSettings setAuthentication(Token authentication) {
        this.authentication = authentication;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public long getUpdated() {
        return updated;
    }

    public RealmSettings setUpdated(long updated) {
        this.updated = updated;
        return this;
    }

    public String getName() {
        return name;
    }

    public RealmSettings setName(String name) {
        this.name = name;
        return this;
    }
}
