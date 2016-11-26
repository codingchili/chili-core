package com.codingchili.services.patching.model;

/**
 * @author Robin Duda
 */
public class PatchFile {
    private String path;
    private Long modified;
    private long size;
    private byte[] bytes;

    PatchFile(String path, long size, long modified) {
        this(path, size, modified, null);
    }

    PatchFile(String path, long size, long modified, byte[] bytes) {
        this.path = path;
        this.size = size;
        this.bytes = bytes;
        this.modified = modified;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public PatchFile setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
