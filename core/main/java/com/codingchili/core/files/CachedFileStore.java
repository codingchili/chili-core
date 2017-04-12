package com.codingchili.core.files;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;

import com.codingchili.core.configuration.*;
import com.codingchili.core.configuration.exception.*;
import com.codingchili.core.context.*;
import com.codingchili.core.files.exception.*;
import com.codingchili.core.logging.*;
import com.codingchili.core.protocol.*;

import io.vertx.core.buffer.*;

/**
 * @author Robin Duda
 *         <p>
 *         Caches files from disk in memory and reloads them on change.
 */
public final class CachedFileStore implements FileStoreListener {
    private static final HashMap<String, CachedFileStore> stores = new HashMap<>();
    private ConcurrentHashMap<String, CachedFile> files = new ConcurrentHashMap<>();
    private List<FileStoreListener> listeners = new LinkedList<>();
    private CachedFileStoreSettings settings;
    private Logger logger;
    protected CoreContext context;

    /**
     * Maintain a CachedFileStore for each loaded directory.
     *
     * @param context  the context requesting the feature.
     * @param settings the settings to use for the CachedFileStore, if conflicting with
     *                 any existing configuration for the given path the configuration is
     *                 ignored.
     */
    public CachedFileStore(CoreContext context, CachedFileStoreSettings settings) {
        this.context = context;
        this.logger = context.logger();
        this.settings = settings;

        synchronized (this) {
            CachedFileStore store = stores.get(settings.getDirectory());
            if (store == null) {
                stores.put(settings.getDirectory(), this);
                initialize();
            } else {
                if (!store.settings.equals(settings)) {
                    logger.onError(new ConfigurationMismatchException());
                }
            }
            this.files = stores.get(settings.getDirectory()).files;
            this.listeners = stores.get(settings.getDirectory()).listeners;
        }
    }

    /**
     * Initializes the filestore by loading the files in specified directory.
     */
    private void initialize() {
        try {
            if (files.size() == 0) {
                loadFiles();
            }
            watchDirectory();
        } catch (IOException e) {
            logger.onFileLoadError(e.getMessage());
        }
    }

    /**
     * Unloads all loaded files.
     */
    static void reset() {
        stores.clear();
    }

    private void watchDirectory() {
        new FileWatcherBuilder(context)
                .onDirectory(settings.getDirectory())
                .rate(context.system()::getCachedFilePoll)
                .withListener(this)
                .build();
    }

    private void loadFiles() throws IOException {
        Files.walkFileTree(Paths.get(settings.getDirectory()), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                onFileModify(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void onFileModify(Path path) {
        context.fileSystem().readFile(path.toAbsolutePath().toString(), done -> {
            if (done.succeeded()) {
                addFile(path, done.result());
                logger.onFileLoaded(path.toString());
            } else {
                logger.onFileLoadError(path.toString());
            }
        });
    }

    private void addFile(Path path, Buffer buffer) {
        byte[] fileBytes = buffer.getBytes();
        String filePath = CoreStrings.format(path, settings.getDirectory());

        if (settings.isGzip()) {
            fileBytes = Serializer.gzip(fileBytes);
        }
        files.put(filePath, new CachedFile(fileBytes, path, filePath));
        listeners.forEach(listener -> listener.onFileModify(path));
    }

    @Override
    public void onFileRemove(Path path) {
        listeners.forEach(listener -> listener.onFileRemove(path));
        files.remove(CoreStrings.format(path, settings.getDirectory()));
    }

    public Collection<CachedFile> getFiles() {
        return files.values();
    }

    public CachedFile getFile(String path) throws FileMissingException {
        CachedFile file = files.get(path);

        if (file != null) {
            return file;
        } else {
            throw new FileMissingException(path);
        }
    }

    /**
     * @param listener a modify/remove event listener.
     * @return fluent
     */
    public CachedFileStore addListener(FileStoreListener listener) {
        listeners.add(listener);
        return this;
    }
}
