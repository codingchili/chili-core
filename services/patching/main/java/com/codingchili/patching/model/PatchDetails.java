package com.codingchili.patching.model;

import com.codingchili.core.files.CachedFile;
import com.codingchili.patching.configuration.PatchNotes;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 * <p>
 * Contains information about files in a patch version.
 */
class PatchDetails {
    private Collection<CachedFile> files = new ArrayList<>();
    private String name;
    private String version;

    PatchDetails(Collection<CachedFile> files, PatchNotes notes) {
        this.name = notes.getName();
        this.version = notes.getVersion();

        // remove the byte contents of the file.
        files.forEach(file -> this.files.add(new CachedFile(file).setBytes(new byte[]{})));
    }

    public Collection<CachedFile> getFiles() {
        return files;
    }

    public void setFiles(Collection<CachedFile> files) {
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
