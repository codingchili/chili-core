package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 *
 * Contains settings used in the CachedFileStore.
 */
public class CachedFileStoreSettings {
    private String directory;
    private boolean gzip;

    public CachedFileStoreSettings setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public CachedFileStoreSettings setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof CachedFileStoreSettings) {
            CachedFileStoreSettings other = (CachedFileStoreSettings) object;
            equals = (other.gzip == this.gzip && other.directory.equals(this.directory));
        }

        return equals;
    }

    public String getDirectory() {
        return directory;
    }

    public boolean isGzip() {
        return gzip;
    }
}