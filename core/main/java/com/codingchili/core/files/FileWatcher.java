package com.codingchili.core.files;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

import com.codingchili.core.context.*;

import static com.codingchili.core.configuration.CoreStrings.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Robin Duda
 *         <p>
 *         Watches changes to files in a registered directory and all its subdirectories.
 */
class FileWatcher {
    private final HashMap<Path, WatchKey> keys = new HashMap<>();
    private final CoreContext context;
    FileStoreListener listener;
    String directory;
    TimerSource rate;

    FileWatcher(CoreContext context) {
        this.context = context;
    }

    void initialize() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(directory);

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    keys.put(dir, dir.register(watcher, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE));
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
                    .filter(event -> !getPath(path, event).toFile().isDirectory())
                    .forEach(event -> {
                        if (event.kind().equals(ENTRY_DELETE)) {
                            listener.onFileRemove(getPath(path, event));
                        } else {
                            listener.onFileModify(getPath(path, event));
                        }
                    });
            key.reset();
        }
    }

    private Path getPath(Path path, WatchEvent event) {
        return Paths.get(path + DIR_SEPARATOR + event.context());
    }

    void setRate(TimerSource rate) {
        this.rate = rate;
    }

    void setDirectory(String directory) {
        this.directory = directory;
    }

    void setListener(FileStoreListener listener) {
        this.listener = listener;
    }
}
