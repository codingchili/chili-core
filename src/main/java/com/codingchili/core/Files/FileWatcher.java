package com.codingchili.core.Files;

import com.hazelcast.util.Preconditions;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.TimerSource;

import static com.codingchili.core.Configuration.Strings.DIR_SEPARATOR;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Robin Duda
 *
 * Watches changes to files in a registered directory and all its subdirectories.
 */
class FileWatcher {
    private final HashMap<Path, WatchKey> keys = new HashMap<>();
    private final CoreContext context;
    private FileStoreListener listener;
    private String directory;
    private TimerSource rate;

    static class FileWatcherBuilder {
        private final FileWatcher watcher;

        FileWatcherBuilder(CoreContext context) {
            this.watcher = new FileWatcher(context);
        }

        /**
         * Sets the listener that is called on file changes.
         * @param listener the listener to be used.
         */
        FileWatcherBuilder withListener(FileStoreListener listener) {
            watcher.setListener(listener);
            return this;
        }

        /**
         * Defines the directory that should be watched relative from the application root.
         * @param directory the directory to be watched, includes its subdirectories.
         */
        FileWatcherBuilder onDirectory(String directory) {
            watcher.setDirectory(directory);
            return this;
        }

        /**
         * The rate in which to poll the file change events.
         * @param rate a timersource in milliseconds.
         */
        FileWatcherBuilder rate(TimerSource rate) {
            watcher.setRate(rate);
            return this;
        }

        /**
         * Constructs the new FileWatcher.
         */
        void build() {
            Preconditions.checkNotNull(watcher.directory);
            Preconditions.checkNotNull(watcher.listener);
            Preconditions.checkNotNull(watcher.rate);
            watcher.initialize();
        }
    }

    private FileWatcher(CoreContext context) {
        this.context = context;
    }

    private void initialize() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(directory);

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    keys.put(dir, dir.register(watcher, ENTRY_MODIFY, ENTRY_DELETE));
                    return FileVisitResult.CONTINUE;
                }
            });

            this.start();
        } catch (IOException e) {
            context.console().onError(e);
        }
    }

    private void start() {
        context.periodic(rate, this.getClass().getSimpleName(), event -> poll());
    }

    private void poll() {
        for (Path path : keys.keySet()) {
            WatchKey key = keys.get(path);

            key.pollEvents().stream()
                    .filter(event -> getPath(path, event).toFile().isFile())
                    .forEach(event -> {

                        if (event.kind().equals(ENTRY_DELETE)) {
                            listener.onFileRemove(getPath(path, event));
                        } else {
                            listener.onFileModify(getPath(path, event));
                        }
                    });
        }
    }

    private Path getPath(Path path, WatchEvent event) {
        return Paths.get(path + DIR_SEPARATOR + event.context());
    }

    private void setRate(TimerSource rate) {
        this.rate = rate;
    }

    private void setDirectory(String directory) {
        this.directory = directory;
    }

    private void setListener(FileStoreListener listener) {
        this.listener = listener;
    }
}
