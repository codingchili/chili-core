package com.codingchili.core.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.buffer.Buffer;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Robin Duda
 * <p>
 * Represents a file loaded from disk into main memory.
 */
public class CachedFile {
    private String path;
    private Long modified;
    private long size;
    private byte[] bytes;

    /**
     * Copy constructor.
     *
     * @param file the file to copy from.
     */
    public CachedFile(CachedFile file) {
        this.path = file.path;
        this.modified = file.modified;
        this.size = file.size;
        this.bytes = file.bytes;
    }

    /**
     * @param bytes    the contents of the cached file.
     * @param path     a path to the file on disk for reading metadata.
     * @param fileName the name of the file
     */
    public CachedFile(byte[] bytes, Path path, String fileName) {
        File file = path.toFile();
        this.bytes = bytes;
        this.modified = file.lastModified();
        this.size = file.length();
        this.path = fileName;
    }

    @JsonIgnore
    public Buffer getBuffer() {
        return Buffer.buffer(bytes);
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

    public byte[] getBytes() {
        return bytes;
    }

    public CachedFile setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }
}
