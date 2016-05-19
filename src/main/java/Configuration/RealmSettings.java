package Configuration;

import Game.Model.Affliction;
import Game.Model.Binding;
import Game.Model.PlayerCharacter;
import Game.Model.PlayerClass;
import Utilities.JsonFileStore;
import Utilities.RemoteAuthentication;
import Utilities.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties({"instance"})
public class RealmSettings {
    private static final String INSTANCE_PATH = "conf/game/world";
    private static final String CLASSES_PATH = "conf/game/class";
    private static final String AFFLICTION_PATH = "conf/game/player/affliction.json";
    private static final String PLAYER_TEMPLATE_PATH = "conf/game/player/character.json";
    private ArrayList<InstanceSettings> instances = new ArrayList<>();
    private ArrayList<PlayerClass> classes = new ArrayList<>();
    private ArrayList<Affliction> afflictions = new ArrayList<>();
    private PlayerCharacter template;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private RemoteAuthentication authentication = new RemoteAuthentication();
    private Binding binding;
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

    public RealmSettings() throws IOException {
        readInstances();
        readPlayerClasses();
        readAfflictions();
        readTemplate();
    }

    private void readInstances() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(INSTANCE_PATH);

        for (JsonObject configuration : configurations) {
            instances.add((InstanceSettings) Serializer.unpack(configuration, InstanceSettings.class));
        }
    }

    private void readPlayerClasses() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(CLASSES_PATH);

        for (JsonObject configuration : configurations) {
            classes.add((PlayerClass) Serializer.unpack(configuration, PlayerClass.class));
        }
    }

    private void readAfflictions() throws IOException {
        JsonArray configurations = JsonFileStore.readList(AFFLICTION_PATH);

        for (int i = 0; i < configurations.size(); i++) {
            Affliction affliction = (Affliction) Serializer.unpack(configurations.getJsonObject(i), Affliction.class);
            afflictions.add(affliction);
        }
    }

    private void readTemplate() throws IOException {
        this.template = (PlayerCharacter) Serializer.unpack(JsonFileStore.readObject(PLAYER_TEMPLATE_PATH), PlayerCharacter.class);
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public PlayerCharacter getTemplate() {
        return template;
    }

    public void setTemplate(PlayerCharacter template) {
        this.template = template;
    }

    public ArrayList<Affliction> getAfflictions() {
        return afflictions;
    }

    public void setAfflictions(ArrayList<Affliction> afflictions) {
        this.afflictions = afflictions;
    }

    public ArrayList<PlayerClass> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<PlayerClass> classes) {
        this.classes = classes;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
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

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public double getVersion() {
        return version;
    }

    protected void setVersion(double version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public String getLifetime() {
        return lifetime;
    }

    protected void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }

    public double getDrop() {
        return drop;
    }

    protected void setDrop(double drop) {
        this.drop = drop;
    }

    public double getLeveling() {
        return leveling;
    }

    protected void setLeveling(double leveling) {
        this.leveling = leveling;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public ArrayList<InstanceSettings> getInstance() {
        return instances;
    }
}
