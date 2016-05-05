package Configuration;

import Utilities.JsonFileStore;
import Utilities.RemoteAuthentication;
import Utilities.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties("instance")
public class RealmSettings {
    private static final String INSTANCE_PATH = "conf/game/world";
    private ArrayList<InstanceSettings> instances = new ArrayList<>();
    private RemoteAuthentication authentication;
    private String name;
    private String description;
    private String remote;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private double drop;
    private double leveling;
    private int port;
    private int players = 0;
    private Boolean trusted;

    public RealmSettings() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(INSTANCE_PATH);

        for (JsonObject configuration : configurations) {
            instances.add((InstanceSettings) Serializer.unpack(configuration, InstanceSettings.class));
        }
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    protected void setAuthentication(RemoteAuthentication authentication) {
        this.authentication = authentication;
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

    public String getRemote() {
        return remote;
    }

    protected void setRemote(String remote) {
        this.remote = remote;
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

    public int getPort() {
        return port;
    }

    protected void setPort(int port) {
        this.port = port;
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
