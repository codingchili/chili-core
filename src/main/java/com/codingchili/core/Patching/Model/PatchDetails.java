package com.codingchili.core.Patching.Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class PatchDetails {
    private ArrayList<PatchFile> files = new ArrayList<>();
    private String name;
    private String version;

    public PatchDetails(HashMap<String, PatchFile> list, String name, String build) {
        this.name = name;
        this.version = build;

        for (String key : list.keySet()) {
            PatchFile file = list.get(key);

            files.add(new PatchFile(file.getPath(), file.getSize(), file.getModified(), null));
        }
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
