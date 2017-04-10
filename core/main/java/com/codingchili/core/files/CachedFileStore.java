package com.codingchili.core.files;

import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.exception.ConfigurationMismatchException;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 *         <p>
 *         Caches files from disk in memory and reloads them on change.
 */
public class CachedFileStore<T> implements FileStoreListener {
    private static final HashMap<String, CachedFileStore> stores = new HashMap<>();
    private ConcurrentHashMap<String, Buffer> files = new ConcurrentHashMap<>();
    private CachedFileStoreSettings settings;
    protected CoreContext context;

    /**
     * Maintain a CachedFileStore for each loaded directory.
     *
     * @param context  the context requesting the feature.
     * @param settings the settings to use for the CachedFileStore, if conflicting with
     *                 any existing configuration for the given path the configuration is
     *                 ignored.
     */
    @SuppressWarnings("unchecked")
    public CachedFileStore(CoreContext context, CachedFileStoreSettings settings) {
        CachedFileStore store = stores.get(settings.getDirectory());

        this.context = context;
        this.settings = settings;

        if (store == null) {
            stores.put(settings.getDirectory(), this);
        } else {
            if (!store.settings.equals(settings)) {
                context.logger().onError(new ConfigurationMismatchException());
            }
        }
        this.files = stores.get(settings.getDirectory()).files;
    }

    /**
     * Initializes the filestore by loading the files in specified directory.
     *
     * @return a loaded cachedfilestore.
     */
    public CachedFileStore<T> initialize() {
        try {
            if (files.size() == 0) {
                loadFiles();
            }
            watchDirectory();
        } catch (IOException e) {
            context.logger().onFileLoadError(e.getMessage());
        }
        return this;
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
        try {
            byte[] fileBytes = Files.readAllBytes(path);
            String filePath = CoreStrings.format(path, settings.getDirectory());

            if (settings.isGzip()) {
                fileBytes = Serializer.gzip(fileBytes);
            }

            files.put(filePath, Buffer.buffer(fileBytes));

            context.logger().onFileLoaded(filePath);
        } catch (IOException e) {
            context.logger().onFileLoadError(path.toString());
        }
    }

    @Override
    public void onFileRemove(Path path) {
        files.remove(CoreStrings.format(path, settings.getDirectory()));
    }

    @SuppressWarnings("unchecked")
    public T getFile(String path) throws FileMissingException {
        Buffer buffer = files.get(path);

        if (buffer != null) {
            return (T) buffer;
        } else {
            throw new FileMissingException(path);
        }
    }
}
