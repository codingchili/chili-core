package com.codingchili.core.configuration;

/**
 * Contains settings used in the CachedFileStore.
 */
public class CachedFileStoreSettings {
    private String directory;
    private boolean gzip;
    private boolean isAsynchronous;

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof CachedFileStoreSettings) {
            CachedFileStoreSettings other = (CachedFileStoreSettings) object;
            equals = (other.gzip == this.gzip && other.directory.equals(this.directory));
        }

        return equals;
    }

    /**
     * @return the directory that is being cached.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to cache.
     * @return fluent
     */
    public CachedFileStoreSettings setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    /**
     * @return true if gzip is used to compress data.
     */
    public boolean isGzip() {
        return gzip;
    }

    /**
     * @param gzip true if gzip is to be used to compress the loaded data.
     * @return fluent
     */
    public CachedFileStoreSettings setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    /**
     * @param is true if store is asynchronous, default is true.
     * @return fluent
     */
    public CachedFileStoreSettings setAsynchronous(boolean is) {
        this.isAsynchronous = is;
        return this;
    }

    public boolean isAsynchronous() {
        return isAsynchronous;
    }
}