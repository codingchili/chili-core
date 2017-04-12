package com.codingchili.patching.model;

import com.codingchili.core.files.*;
import com.codingchili.core.files.exception.*;
import com.codingchili.core.logging.*;
import com.codingchili.core.files.CachedFile;
import com.codingchili.patching.configuration.*;

import io.vertx.core.buffer.*;


/**
 * @author Robin Duda
 *         <p>
 *         Handles patch files.
 */
public class PatchKeeper implements FileStoreListener {
    private CachedFileStore store;
    private PatchContext context;
    private Logger logger;

    public PatchKeeper(PatchContext context) {
        store = new CachedFileStore(context, context.fileStoreSettings());
        this.context = context;
        this.logger = context.logger();
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
