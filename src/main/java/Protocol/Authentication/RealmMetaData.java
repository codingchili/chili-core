package Protocol.Authentication;

import Configuration.RealmSettings;
import Game.Model.PlayerClass;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-05-06.
 */
public class RealmMetaData {
    private ArrayList<String> classes = new ArrayList<>();
    private String name;
    private String description;
    private String remote;
    private String resources;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private double drop;
    private double leveling;
    private int port;
    private int players = 0;
    private Boolean trusted;

    public RealmMetaData(RealmSettings settings) {
        this.name = settings.getName();
        this.description = settings.getDescription();
        this.remote = settings.getRemote();
        this.resources = settings.getResources();
        this.version = settings.getVersion();
        this.size = settings.getSize();
        this.type = settings.getType();
        this.lifetime = settings.getLifetime();
        this.drop = settings.getDrop();
        this.leveling = settings.getLeveling();
        this.port = settings.getPort();
        this.players = settings.getPlayers();
        this.trusted = settings.getTrusted();

        for (PlayerClass pc : settings.getClasses())
            classes.add(pc.getName());
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<String> classes) {
        this.classes = classes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
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

    public double getDrop() {
        return drop;
    }

    public void setDrop(double drop) {
        this.drop = drop;
    }

    public double getLeveling() {
        return leveling;
    }

    public void setLeveling(double leveling) {
        this.leveling = leveling;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
}
