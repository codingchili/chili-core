package com.codingchili.core.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches changes to files in a registered directory and all its subdirectories.
 * <p>
 * All file watchers share a single thread that blocks on the {@link WatchService} and is woken by
 * the operating system, an idle watcher does not poll and does not allocate. Changes are collected
 * for a short time before the listener is notified so that a burst of events for one file, as
 * emitted by most editors on save, results in a single notification.
 */
public class FileWatcher {
    /**
     * the longest time that a change is held back before the listeners are notified.
     */
    static final int MAX_DEBOUNCE_MS = 500;
    private final CoreContext context;
    private final Logger logger;
    // only accessed by the watch thread.
    private Map<Path, Boolean> changes = new LinkedHashMap<>();
    private volatile boolean stopped = false;
    protected FileStoreListener listener;
    protected String directory;
    protected TimerSource rate;

    FileWatcher(CoreContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
    }

    /**
     * Creates a new file watcher builder.
     *
     * @param core the core context to run on.
     * @return a new builder instance.
     */
    public static FileWatcherBuilder builder(CoreContext core) {
        return new FileWatcherBuilder(core);
    }

    /**
     * Stops the filewatcher.
     */
    public void stop() {
        stopped = true;
        WatchRegistry.unregister(this);
    }

    void initialize() {
        try {
            Path path = Paths.get(directory);

            if (path.toFile().exists()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                        WatchRegistry.register(FileWatcher.this, dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            logger.onError(e);
        }
    }

    /**
     * Collects the pending events of the key, the most recent event of each file is kept.
     * Called by the watch thread.
     *
     * @param key the key that was signalled, is reset once its events are collected.
     */
    void collect(WatchKey key) {
        Path directory = (Path) key.watchable();

        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.context() instanceof Path file) {
                Path path = directory.resolve(file);
                changes.remove(path);
                changes.put(path, event.kind().equals(ENTRY_DELETE));
            }
        }
        key.reset();
    }

    /**
     * Notifies the listener of the collected changes on the context. Called by the watch thread.
     */
    void dispatch() {
        Map<Path, Boolean> batch = changes;
        changes = new LinkedHashMap<>();

        if (stopped) {
            return;
        }
        try {
            context.vertx().runOnContext(done -> batch.forEach((path, removed) -> {
                if (removed) {
                    listener.onFileRemove(path);
                } else if (!path.toFile().isDirectory()) {
                    listener.onFileModify(path);
                }
            }));
        } catch (RejectedExecutionException e) {
            // the context was closed, there is nobody left to notify.
            stop();
        }
    }

    /**
     * @return the time in milliseconds to wait for more changes before notifying the listener.
     */
    int debounce() {
        return Math.min(rate.getMS(), MAX_DEBOUNCE_MS);
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
