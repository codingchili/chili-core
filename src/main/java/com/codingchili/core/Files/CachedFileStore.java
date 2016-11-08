package com.codingchili.core.Files;

import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.Configuration.CachedFileStoreSettings;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Context.SystemContext;
import com.codingchili.core.Exception.ConfigurationMismatchException;
import com.codingchili.core.Exception.FileMissingException;
import com.codingchili.core.Protocol.Serializer;

/**
 * @author Robin Duda
 */
public class CachedFileStore<T> implements FileStoreListener {
    private static final HashMap<String, CachedFileStore> stores = new HashMap<>();
    private final ConcurrentHashMap<String, Buffer> files = new ConcurrentHashMap<>();
    private final CachedFileStoreSettings settings;
    private final SystemContext context;


    /**
     * Maintain a singleton for each root directory.
     */
    public static CachedFileStore instance(SystemContext context, CachedFileStoreSettings settings) {
        CachedFileStore store = stores.get(settings.getDirectory());

        if (store == null) {
            stores.put(settings.getDirectory(), new CachedFileStore(context, settings));
        } else {
            if (!store.settings.equals(settings)) {
                context.console().onError(new ConfigurationMismatchException());
            }
        }

        return stores.get(settings.getDirectory());
    }

    protected CachedFileStore(SystemContext context, CachedFileStoreSettings settings) {
        this.settings = settings;
        this.context = context;
        try {
            loadFiles();
            watchDirectory();
        } catch (IOException e) {
            context.console().onFileLoadError(e.getMessage());
        }
    }

    private void watchDirectory() {
        new FileWatcher.FileWatcherBuilder(context)
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
            String filePath = Strings.format(path, settings.getDirectory());

            if (settings.isGzip()) {
                fileBytes = Serializer.gzip(fileBytes);
            }

            files.put(filePath, Buffer.buffer(fileBytes));

            context.console().onFileLoaded(filePath);
        } catch (IOException e) {
            context.console().onFileLoadError(path.toString());
        }
    }

    @Override
    public void onFileRemove(Path path) {
        files.remove(Strings.format(path, settings.getDirectory()));
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
