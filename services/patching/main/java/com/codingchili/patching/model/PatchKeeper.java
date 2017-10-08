package com.codingchili.patching.model;

import com.codingchili.core.files.CachedFile;
import com.codingchili.core.files.CachedFileStore;
import com.codingchili.core.files.FileStoreListener;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.configuration.PatchNotes;
import io.vertx.core.buffer.Buffer;


/**
 * @author Robin Duda
 * <p>
 * Handles patch files.
 */
public class PatchKeeper implements FileStoreListener {
    private CachedFileStore store;
    private PatchContext context;

    public PatchKeeper(PatchContext context) {
        store = new CachedFileStore(context, context.fileStoreSettings());
        this.context = context;
        context.onPatchLoaded(getName(), getVersion());
    }

    public CachedFile getFile(String path, String version) throws FileMissingException, PatchReloadedException {
        if (version.compareTo(getVersion()) < 0) {
            throw new PatchReloadedException();
        } else {
            return store.getFile(path);
        }
    }

    public Buffer getBuffer(String path, int start, int end) throws FileMissingException {
        return store.getFile(path).getBuffer().getBuffer(start, end);
    }

    public PatchDetails getDetails() {
        return new PatchDetails(store.getFiles(), getPatchNotes());
    }

    private PatchNotes getPatchNotes() {
        return context.notes();
    }

    private String getVersion() {
        return getPatchNotes().getVersion();
    }

    private String getName() {
        return getPatchNotes().getName();
    }
}
