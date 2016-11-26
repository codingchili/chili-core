package com.codingchili.services.patching.model;

import io.vertx.core.buffer.Buffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.configuration.exception.FileMissingException;
import com.codingchili.core.files.CachedFileStore;
import com.codingchili.core.protocol.Serializer;

import com.codingchili.services.patching.configuration.PatchContext;
import com.codingchili.services.patching.configuration.PatchNotes;
import com.codingchili.services.Shared.Strings;


/**
 * @author Robin Duda
 */
public class PatchKeeper<T extends PatchFile> extends CachedFileStore {
    private ConcurrentHashMap<String, PatchFile> files = new ConcurrentHashMap<>();
    private PatchContext context;

    public PatchKeeper(PatchContext context) {
        super(context, new CachedFileStoreSettings().setDirectory(context.directory()));

        this.context = context;
        initialize();

        context.onPatchLoaded(getName(), getVersion());
    }

    @Override
    public void onFileModify(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);

            if (context.gzip()) {
                bytes = Serializer.gzip(bytes);
            }

            File file = path.toFile();
            String relativePath = Strings.format(path, context.directory());
            files.put(relativePath, new PatchFile(relativePath, file.length(), file.lastModified(), bytes));

            context.console().onFileLoaded(relativePath);
        } catch (IOException e) {
            context.console().onFileLoadError(Strings.format(path, context.directory()));
        }
    }

    @Override
    public void onFileRemove(Path path) {
        files.remove(Strings.format(path, context.directory()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getFile(String path) {
        return (T) files.get(path);
    }

    public PatchFile getFile(String path, String version) throws FileMissingException, PatchReloadedException {
        if (version.compareTo(getVersion()) < 0) {
            throw new PatchReloadedException();
        } else {
            PatchFile file = files.get(path);

            if (file != null) {
                return files.get(path);
            } else {
                throw new FileMissingException(path);
            }
        }
    }

    public Buffer getBuffer(String path, int start, int end) throws FileMissingException {
        if (files.containsKey(path)) {
            return Buffer.buffer(files.get(path).getBytes()).getBuffer(start, end);
        } else {
            throw new FileMissingException(path);
        }
    }

    public PatchDetails getDetails() {
        return new PatchDetails(files.values(), getPatchNotes());
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
