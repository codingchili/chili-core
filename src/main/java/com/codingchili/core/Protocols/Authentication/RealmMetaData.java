package com.codingchili.core.Protocols.Authentication;

import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.Attributes;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains realmName metadata used in the realmName-list.
 */
public class RealmMetaData extends Attributes {
    private ArrayList<String> classes;
    private String name;
    private String description;
    private String remote;
    private String resources;
    private double version;
    private int size;
    private String type;
    private String lifetime;
    private int players = 0;
    private Boolean trusted;
    private Boolean secure;

    public RealmMetaData(RealmSettings settings) {

        this.setName(settings.getName())
                .setDescription(settings.getDescription())
                .setRemote(settings.getRemote())
                .setResources(settings.getResources())
                .setVersion(settings.getVersion())
                .setSize(settings.getSize())
                .setType(settings.getType())
                .setLifetime(settings.getLifetime())
                .setPlayers(settings.getPlayers())
                .setTrusted(settings.getTrusted())
                .setSecure(settings.getSecure());

        this.setAttributes(settings.getAttributes());

        for (PlayerClass pc : settings.getClasses()) {
            if (classes == null) {
                classes = new ArrayList<>();
            }
            classes.add(pc.getName());
        }
    }

    public Boolean getSecure() {
        return secure;
    }

    public RealmMetaData setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public RealmMetaData setClasses(ArrayList<String> classes) {
        this.classes = classes;
        return this;
    }

    public String getName() {
        return name;
    }

    public RealmMetaData setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RealmMetaData setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getRemote() {
        return remote;
    }

    public RealmMetaData setRemote(String remote) {
        this.remote = remote;
        return this;
    }

    public String getResources() {
        return resources;
    }

    public RealmMetaData setResources(String resources) {
        this.resources = resources;
        return this;
    }

    public double getVersion() {
        return version;
    }

    public RealmMetaData setVersion(double version) {
        this.version = version;
        return this;
    }

    public int getSize() {
        return size;
    }

    public RealmMetaData setSize(int size) {
        this.size = size;
        return this;
    }

    public String getType() {
        return type;
    }

    public RealmMetaData setType(String type) {
        this.type = type;
        return this;
    }

    public String getLifetime() {
        return lifetime;
    }

    public RealmMetaData setLifetime(String lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public int getPlayers() {
        return players;
    }

    public RealmMetaData setPlayers(int players) {
        this.players = players;
        return this;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public RealmMetaData setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }
}
