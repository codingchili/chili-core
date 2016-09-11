package Realm.Configuration;

import Configuration.JsonFileStore;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import Protocols.Serializer;
import Realm.Model.Affliction;
import Realm.Model.PlayerCharacter;
import Realm.Model.PlayerClass;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

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
    private int port;
    private int proxy;
    private String remote;
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
                .setPort(port)
                .setClasses(classes)
                .setAfflictions(afflictions)
                .setTemplate(template)
                .setProxy(proxy)
                .setRemote(remote)
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

    public RealmSettings load() throws IOException {
        readInstances();
        readPlayerClasses();
        readAfflictions();
        readTemplate();
        return this;
    }

    private void readInstances() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(Strings.PATH_INSTANCE);

        for (JsonObject configuration : configurations) {
            instances.add((InstanceSettings) Serializer.unpack(configuration, InstanceSettings.class));
        }
    }

    private void readPlayerClasses() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(Strings.PATH_CLASSES);

        for (JsonObject configuration : configurations) {
            classes.add((PlayerClass) Serializer.unpack(configuration, PlayerClass.class));
        }
    }

    private void readAfflictions() throws IOException {
        JsonArray configurations = JsonFileStore.readList(Strings.PATH_AFFLICTIONS);

        for (int i = 0; i < configurations.size(); i++) {
            Affliction affliction = (Affliction) Serializer.unpack(configurations.getJsonObject(i), Affliction.class);
            afflictions.add(affliction);
        }
    }

    public RealmSettings setProxy(int proxy) {
        this.proxy = proxy;
        return this;
    }

    public RealmSettings setRemote(String remote) {
        this.remote = remote;
        return this;
    }

    public int getProxy() {
        return proxy;
    }

    public String getRemote() {
        return remote;
    }

    private void readTemplate() throws IOException {
        this.template = (PlayerCharacter) Serializer.unpack(JsonFileStore.readObject(Strings.PATH_PLAYER_TEMPLATE), PlayerCharacter.class);
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

    public int getPort() {
        return port;
    }

    public RealmSettings setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RealmSettings && (((RealmSettings) other).getName().equals(name));

    }
}
