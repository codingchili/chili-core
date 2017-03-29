package com.codingchili.realm.configuration;

import com.codingchili.realm.instance.configuration.InstanceSettings;
import com.codingchili.realm.instance.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.AttributeConfigurable;
import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.RemoteIdentity;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.files.Configurations.*;

/**
 * @author Robin Duda
 *         <p>
 *         Contains the settings for a realmName.
 */
@JsonIgnoreProperties({"instances"})
public class RealmSettings extends AttributeConfigurable {
    private final ArrayList<InstanceSettings> instances = new ArrayList<>();
    private RemoteIdentity identity;
    private ArrayList<PlayerClass> classes = new ArrayList<>();
    private ArrayList<Affliction> afflictions = new ArrayList<>();
    private PlayerCharacter template = new PlayerCharacter();
    private Token authentication;
    private String description;
    private String resources;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private int players = 0;
    private Boolean trusted;
    private Boolean secure;
    private long updated;
    private String name;

    public RealmSettings removeAuthentication() {
        RealmSettings copy = new RealmSettings()
                .setClasses(classes)
                .setAfflictions(afflictions)
                .setTemplate(template)
                .setDescription(description)
                .setResources(resources)
                .setVersion(version)
                .setName(name)
                .setSize(size)
                .setType(type)
                .setLifetime(lifetime)
                .setPlayers(players)
                .setTrusted(trusted)
                .setSecure(secure)
                .setAuthentication(null);

        copy.setAttributes(attributes);

        return copy;
    }

    public void load(List<String> instances) {
        readInstances(instances);
        readPlayerClasses();
        readAfflictions();
        readTemplate();
    }

    private void readInstances(List<String> enabled) {
        available(PATH_INSTANCE).stream()
                .map(path -> override(path, name))
                .map(path -> get(path, InstanceSettings.class))
                .filter(instance -> enabled.contains(instance.getName()) || enabled.isEmpty())
                .forEach(instances::add);
    }

    private void readPlayerClasses() {
        available(PATH_GAME_CLASSES).stream()
                .map(path -> override(path, name))
                .map(path -> get(path, PlayerClass.class))
                .forEach(classes::add);
    }

    private void readAfflictions() {
        available(PATH_GAME_AFFLICTIONS).stream()
                .map(path -> override(path, name))
                .map(JsonFileStore::readList)
                .flatMap(JsonArray::stream)
                .map(json -> (JsonObject) json)
                .forEach(affliction -> afflictions.add(Serializer.unpack(affliction, Affliction.class)));
    }

    private void readTemplate() {
        this.template = get(override(PATH_GAME_PLAYERTEMPLATE, name), PlayerCharacter.class);
    }

    /**
     * Checks if an overridden resource exist in PATH_GAME_OVERRIDE for the
     * specified realm.
     *
     * @param path  to the file to look if overridden.
     * @param realm the name of the realm.
     * @return a path to the overridden resource or the path itself.
     */
    private static String override(String path, String realm) {
        String overridePath = path.replace(PATH_GAME, PATH_GAME_OVERRIDE + realm);
        File override = new File(overridePath);

        if (override.exists()) {
            return overridePath;
        } else {
            return path;
        }
    }

    public String getRemote() {
        return name + NODE_REALM;
    }

    public Boolean getSecure() {
        return secure;
    }

    private RealmSettings setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    public PlayerCharacter getTemplate() {
        return template;
    }

    public RealmSettings setTemplate(PlayerCharacter template) {
        this.template = template;
        return this;
    }

    public ArrayList<Affliction> getAfflictions() {
        return afflictions;
    }

    private RealmSettings setAfflictions(ArrayList<Affliction> afflictions) {
        this.afflictions = afflictions;
        return this;
    }

    public ArrayList<PlayerClass> getClasses() {
        return classes;
    }

    public RealmSettings setClasses(ArrayList<PlayerClass> classes) {
        this.classes = classes;
        return this;
    }

    public String getResources() {
        return resources;
    }

    public RealmSettings setResources(String resources) {
        this.resources = resources;
        return this;
    }

    public int getPlayers() {
        return players;
    }

    private RealmSettings setPlayers(int players) {
        this.players = players;
        return this;
    }

    public RealmSettings setAuthentication(Token authentication) {
        this.authentication = authentication;
        return this;
    }

    public Token getAuthentication() {
        return authentication;
    }

    public String getName() {
        return name;
    }

    public RealmSettings setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    private RealmSettings setDescription(String description) {
        this.description = description;
        return this;
    }

    public double getVersion() {
        return version;
    }

    private RealmSettings setVersion(double version) {
        this.version = version;
        return this;
    }

    public int getSize() {
        return size;
    }

    private RealmSettings setSize(int size) {
        this.size = size;
        return this;
    }

    public String getType() {
        return type;
    }

    public RealmSettings setType(String type) {
        this.type = type;
        return this;
    }

    public String getLifetime() {
        return lifetime;
    }

    private RealmSettings setLifetime(String lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public RealmSettings setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    public ArrayList<InstanceSettings> getInstances() {
        return instances;
    }

    public RemoteIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(RemoteIdentity identity) {
        this.identity = identity;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RealmSettings && (((RealmSettings) other).getName().equals(name));
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = Serializer.json(this);

        json.remove(ID_AFFLICTIONS);
        json.remove(ID_CLASSES);
        json.remove(ID_TEMPLATE);
        json.remove(ID_INSTANCES);

        return json;
    }

    public RealmSettings setUpdated(long updated) {
        this.updated = updated;
        return this;
    }

    @JsonIgnore
    public long getUpdated() {
        return updated;
    }

    @JsonIgnore
    public byte[] getTokenBytes() {
        return getAuthentication().getKey().getBytes();
    }
}
