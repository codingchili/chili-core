package com.codingchili.realm.configuration;

import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.model.afflictions.Affliction;
import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.*;

import com.codingchili.core.configuration.AttributeConfigurable;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.files.Configurations.*;

/**
 * @author Robin Duda
 * <p>
 * Contains the settings for a realmName.
 */
@JsonIgnoreProperties({"instances"})
public class RealmSettings extends AttributeConfigurable {
    private final List<InstanceSettings> instances = new ArrayList<>();
    private List<PlayableClass> classes = new ArrayList<>();
    private List<Affliction> afflictions = new ArrayList<>();
    private Token authentication;
    private String description;
    private String resources;
    private String version;
    private String host;
    private int port;
    private int size;
    private String type;
    private String lifetime;
    private int players = 0;
    private Boolean trusted;
    private Boolean secure;
    private long updated;
    private String name;

    /**
     * Checks if an overridden resource exist in PATH_GAME_OVERRIDE for the
     * specified realm.
     *
     * @param path  to the file to look if overridden.
     * @param realm the handler of the realm.
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

    public RealmSettings removeAuthentication() {
        RealmSettings copy = new RealmSettings()
                .setPort(port)
                .setHost(host)
                .setClasses(classes)
                .setAfflictions(afflictions)
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

    /**
     * @param instances loads configuration for the given instance.
     */
    public void load(List<String> instances) {
        readInstances(instances);
        readPlayerClasses();
        readAfflictions();
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
                .map(ConfigurationFactory::readObject)
                .map(json -> Serializer.unpack(json, PlayableClass.class))
                .forEach(classes::add);
    }

    private void readAfflictions() {
        available(PATH_GAME_AFFLICTIONS).stream()
                .map(path -> override(path, name))
                .map(ConfigurationFactory::readObject)
                .forEach(affliction -> afflictions.add(Serializer.unpack(affliction, Affliction.class)));
    }

    /**
     * @return the remote host of the realm.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host set the remote host for this realm.
     * @return fluent
     */
    public RealmSettings setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * @return the port of this realm.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port the realm is listening on.
     * @return fluent
     */
    public RealmSettings setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return returns true if the connection must be secure.
     */
    public Boolean getSecure() {
        return secure;
    }

    /**
     * @param secure if true then all connections must be secure
     * @return fluent
     */
    private RealmSettings setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * @return a list of all afflictions configured.
     */
    public List<Affliction> getAfflictions() {
        return afflictions;
    }

    /**
     * @param afflictions sets a list of available afflictions.
     * @return fluent
     */
    private RealmSettings setAfflictions(List<Affliction> afflictions) {
        this.afflictions = afflictions;
        return this;
    }

    /**
     * @param affliction an affliction to add
     * @return fluent
     */
    private RealmSettings addAffliction(Affliction affliction) {
        this.afflictions.add(affliction);
        return this;
    }

    /**
     * @return return all available player classes.
     */
    public List<PlayableClass> getClasses() {
        return classes;
    }

    /**
     * @param classes sets a list of available player classes.
     * @return fluent
     */
    public RealmSettings setClasses(List<PlayableClass> classes) {
        this.classes = classes;
        return this;
    }

    /**
     * @param klass the playerclass to add to available.
     * @return fluent
     */
    public RealmSettings addClass(PlayableClass klass) {
        this.classes.add(klass);
        return this;
    }

    /**
     * @return the resource server where game resources are downloaded from..
     */
    public String getResources() {
        return resources;
    }

    /**
     * @param resources the resource server where resources are downloaded from.
     * @return fluent
     */
    public RealmSettings setResources(String resources) {
        this.resources = resources;
        return this;
    }

    /**
     * @return get the number of players connected.
     */
    public int getPlayers() {
        return players;
    }

    /**
     * @param players set the current number of players connected.
     * @return fluent
     */
    private RealmSettings setPlayers(int players) {
        this.players = players;
        return this;
    }

    /**
     * @return authentication token used to authenticate against the realm-registry.
     */
    public Token getAuthentication() {
        return authentication;
    }

    /**
     * @param authentication the authentication to use against the realm-registry.
     * @return fluent.
     */
    public RealmSettings setAuthentication(Token authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * @return get the handler of this realm.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name set the handler of this realm
     * @return fluent
     */
    public RealmSettings setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return get the realm description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description set the realm description.
     * @return fluent
     */
    private RealmSettings setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return get the version of the realm.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set for the realm.
     * @return fluent
     */
    private RealmSettings setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the maximum number of players that may connect.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the maximum number players that may connect
     * @return fluent
     */
    private RealmSettings setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * @return the type of the realm.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type of realm,  may be any string.
     * @return fluent
     */
    public RealmSettings setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @return get the lifetime of the realm as a description.
     */
    public String getLifetime() {
        return lifetime;
    }

    /**
     * @param lifetime set the lifetime as a string.
     * @return fluent
     */
    private RealmSettings setLifetime(String lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /**
     * @return true if this realm is not a third-party server.
     */
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * @param trusted indicates if this realm is a thirdparty server or not.
     * @return fluent
     */
    public RealmSettings setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    /**
     * @return a list of instances configured for the realm.
     */
    public List<InstanceSettings> getInstances() {
        return instances;
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

    @JsonIgnore
    public long getUpdated() {
        return updated;
    }

    /**
     * @param updated set the last time in MS when the realm was updated in the registry.
     * @return fluent
     */
    public RealmSettings setUpdated(long updated) {
        this.updated = updated;
        return this;
    }

    @JsonIgnore
    public byte[] getTokenBytes() {
        return getAuthentication().getKey().getBytes();
    }
}
