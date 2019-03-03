package com.codingchili.core.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.DIR_SEPARATOR;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches changes to files in a registered directory and all its subdirectories.
 */
public class FileWatcher {
    private final HashMap<Path, WatchKey> keys = new HashMap<>();
    private final CoreContext context;
    private Logger logger;
    private AtomicBoolean running = new AtomicBoolean(true);
    protected FileStoreListener listener;
    protected String directory;
    protected TimerSource rate;

    FileWatcher(CoreContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
    }

    public static FileWatcherBuilder builder(CoreContext core) {
        return new FileWatcherBuilder(core);
    }

    void initialize() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(directory);

            if (path.toFile().exists()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                        keys.put(dir, dir.register(watcher, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE));
                        return FileVisitResult.CONTINUE;
                    }
                });
                this.start();
            }
        } catch (IOException e) {
            logger.onError(e);
        }
    }

    private void start() {
        context.periodic(rate, this.getClass().getSimpleName(), event -> {
            if (running.get()) {
                poll();
            } else {
                context.cancel(event);
            }
        });
    }

    public void stop() {
        running.set(false);
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
