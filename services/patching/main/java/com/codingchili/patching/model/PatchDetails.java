package com.codingchili.patching.model;

import com.codingchili.patching.configuration.PatchNotes;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 */
class PatchDetails {
    private ArrayList<PatchFile> files = new ArrayList<>();
    private String name;
    private String version;

    PatchDetails(Collection<PatchFile> files, PatchNotes notes) {
        this.name = notes.getName();
        this.version = notes.getVersion();

        files.stream().forEach(file -> {
            this.files.add(new PatchFile(file.getPath(), file.getSize(), file.getModified()));
        });
    }

    public ArrayList<PatchFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<PatchFile> files) {
        this.files = files;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
