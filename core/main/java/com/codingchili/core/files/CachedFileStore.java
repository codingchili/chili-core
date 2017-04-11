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
import com.codingchili.core.protocol.*;

import io.vertx.core.*;
import io.vertx.core.buffer.*;

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
        this.context = context;
        this.settings = settings;

        synchronized (this) {
            CachedFileStore store = stores.get(settings.getDirectory());
            if (store == null) {
                stores.put(settings.getDirectory(), this);
                initialize();
            } else {
                if (!store.settings.equals(settings)) {
                    context.logger().onError(new ConfigurationMismatchException());
                }
            }
            this.files = stores.get(settings.getDirectory()).files;
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
            context.logger().onFileLoadError(e.getMessage());
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
                addFile(path, readFileSync(path));
                context.logger().onFileLoaded(path.toString());
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
        readFile(done -> {
            if (done.succeeded()) {
                addFile(path, done.result());
                context.logger().onFileLoaded(path.toString());
            } else {
                context.logger().onFileLoadError(path.toString());
            }
        }, path);
    }

    private void addFile(Path path, Buffer buffer) {
        byte[] fileBytes = buffer.getBytes();
        String filePath = CoreStrings.format(path, settings.getDirectory());

        if (settings.isGzip()) {
            fileBytes = Serializer.gzip(fileBytes);
        }
        files.put(filePath, Buffer.buffer(fileBytes));
    }

    private Buffer readFileSync(Path path) {
        return context.fileSystem().readFileBlocking(path.toAbsolutePath().toString());
    }

    private void readFile(Handler<AsyncResult<Buffer>> handler, Path path) {
        context.fileSystem().readFile(path.toAbsolutePath().toString(), handler);
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
