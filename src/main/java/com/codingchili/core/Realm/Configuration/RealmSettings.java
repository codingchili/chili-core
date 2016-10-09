package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Configuration.JsonFileStore;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Instance.Model.Affliction;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Contains the settings for a realmName.
 */
@JsonIgnoreProperties({"instance"})
public class RealmSettings implements Serializable {
    private ArrayList<InstanceSettings> instances = new ArrayList<>();
    private ArrayList<PlayerClass> classes = new ArrayList<>();
    private ArrayList<Affliction> afflictions = new ArrayList<>();
    private PlayerCharacter template = new PlayerCharacter();
    private RemoteAuthentication authentication = new RemoteAuthentication();
    private String name;
    private String description;
    private String resources;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private double drop;
    private double leveling;
    private int players = 0;
    private Boolean trusted;
    private Boolean secure;

    public RealmSettings removeAuthentication() {
        return new RealmSettings()
                .setClasses(classes)
                .setAfflictions(afflictions)
                .setTemplate(template)
                .setName(name)
                .setDescription(description)
                .setResources(resources)
                .setVersion(version)
                .setSize(size)
                .setType(type)
                .setLifetime(lifetime)
                .setDrop(drop)
                .setLeveling(leveling)
                .setPlayers(players)
                .setTrusted(trusted)
                .setSecure(secure)
                .setAuthentication(null);
    }

    public RealmSettings load(EnabledRealm enabled) throws IOException {
        readInstances(enabled.getInstances());
        readPlayerClasses();
        readAfflictions();
        readTemplate();
        return this;
    }

    private void readInstances(List<String> enabled) throws IOException {
        String path = getConfigurationOverride(PATH_INSTANCE);
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(path);

        for (JsonObject configuration : configurations) {
            if (enabled.isEmpty() || enabled.contains(configuration.getString(ID_NAME))) {
                instances.add(Serializer.unpack(configuration, InstanceSettings.class));
            }
        }
    }

    private void readPlayerClasses() throws IOException {
        String path = getConfigurationOverride(PATH_CLASSES);
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(path);

        for (JsonObject configuration : configurations) {
            classes.add(Serializer.unpack(configuration, PlayerClass.class));
        }
    }

    private void readAfflictions() throws IOException {
        String path = getConfigurationOverride(PATH_AFFLICTIONS);
        JsonArray configurations = JsonFileStore.readList(path);

        for (int i = 0; i < configurations.size(); i++) {
            Affliction affliction = Serializer.unpack(configurations.getJsonObject(i), Affliction.class);
            afflictions.add(affliction);
        }
    }

    private String getConfigurationOverride(String path) {
        String overridePath = path.replace(PATH_GAME, PATH_GAME_OVERRIDE + name);
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

    private void readTemplate() throws IOException {
        String path = getConfigurationOverride(PATH_PLAYER_TEMPLATE);
        this.template = Serializer.unpack(JsonFileStore.readObject(path), PlayerCharacter.class);
    }

    public Boolean getSecure() {
        return secure;
    }

    public RealmSettings setSecure(Boolean secure) {
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

    public RealmSettings setAfflictions(ArrayList<Affliction> afflictions) {
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

    public RealmSettings setPlayers(int players) {
        this.players = players;
        return this;
    }

    public RealmSettings setAuthentication(RemoteAuthentication authentication) {
        this.authentication = authentication;
        return this;
    }

    public RemoteAuthentication getAuthentication() {
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

    protected RealmSettings setDescription(String description) {
        this.description = description;
        return this;
    }

    public double getVersion() {
        return version;
    }

    protected RealmSettings setVersion(double version) {
        this.version = version;
        return this;
    }

    public int getSize() {
        return size;
    }

    protected RealmSettings setSize(int size) {
        this.size = size;
        return this;
    }

    public String getType() {
        return type;
    }

    protected RealmSettings setType(String type) {
        this.type = type;
        return this;
    }

    public String getLifetime() {
        return lifetime;
    }

    protected RealmSettings setLifetime(String lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public double getDrop() {
        return drop;
    }

    protected RealmSettings setDrop(double drop) {
        this.drop = drop;
        return this;
    }

    public double getLeveling() {
        return leveling;
    }

    protected RealmSettings setLeveling(double leveling) {
        this.leveling = leveling;
        return this;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public RealmSettings setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    public ArrayList<InstanceSettings> getInstance() {
        return instances;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RealmSettings && (((RealmSettings) other).getName().equals(name));

    }
}
