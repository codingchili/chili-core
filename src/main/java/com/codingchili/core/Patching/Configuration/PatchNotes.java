package com.codingchili.core.Patching.Configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains a list of patch notes, version and name for the website.
 */
public class PatchNotes {
    private String version;
    private String name;
    private String date;
    private ArrayList<PatchChange> changes;

    public String getVersion() {
        return version;
    }

    public PatchNotes setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getName() {
        return name;
    }

    public PatchNotes setName(String name) {
        this.name = name;
        return this;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<PatchChange> getChanges() {
        return changes;
    }

    public void setChanges(ArrayList<PatchChange> changes) {
        this.changes = changes;
    }
}