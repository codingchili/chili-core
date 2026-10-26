package com.codingchili.core.files;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Shares one {@link WatchService} and one thread between all file watchers.
 * <p>
 * Each watch service has a thread in the JDK and so does each file watcher that blocks on it, sharing
 * keeps the number of threads constant. The thread and the watch service are released when the last
 * watcher is removed, and created again when a new watcher is added.
 */
final class WatchRegistry {
    private static final Map<WatchKey, FileWatcher> OWNERS = new ConcurrentHashMap<>();
    private static WatchService service;

    private WatchRegistry() {
    }

    /**
     * Watches the directory, changes are passed to {@link FileWatcher#collect(WatchKey)}.
     *
     * @param owner     the file watcher that is notified on changes in the directory.
     * @param directory the directory to watch, subdirectories are not included.
     */
    static synchronized void register(FileWatcher owner, Path directory) throws IOException {
        if (service == null) {
            var created = FileSystems.getDefault().newWatchService();
            var thread = new Thread(() -> watch(created), "FileWatchers");
            thread.setDaemon(true);
            service = created;
            thread.start();
        }
        OWNERS.put(directory.register(service, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE), owner);
    }

    /**
     * Stops watching all directories of the file watcher.
     *
     * @param owner the file watcher to remove.
     */
    static synchronized void unregister(FileWatcher owner) {
        OWNERS.entrySet().removeIf(entry -> {
            if (entry.getValue() == owner) {
                entry.getKey().cancel();
                return true;
            }
            return false;
        });
        if (OWNERS.isEmpty() && service != null) {
            try {
                // ends the thread that is waiting on the service.
                service.close();
            } catch (IOException e) {
                // nothing to release.
            }
            service = null;
        }
    }

    private static void watch(WatchService watcher) {
        // watchers with changes waiting to be delivered and when, in nanoseconds.
        var due = new HashMap<FileWatcher, Long>();

        try {
            while (true) {
                WatchKey key = due.isEmpty() ? watcher.take() : watcher.poll(untilNext(due), TimeUnit.NANOSECONDS);

                if (key != null) {
                    var owner = OWNERS.get(key);
                    if (owner != null) {
                        owner.collect(key);
                        due.putIfAbsent(owner, System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(owner.debounce()));
                    } else {
                        key.pollEvents();
                        key.reset();
                    }
                }
                deliver(due);
            }
        } catch (ClosedWatchServiceException | InterruptedException e) {
            // stopped.
        }
    }

    private static long untilNext(Map<FileWatcher, Long> due) {
        long next = Long.MAX_VALUE;
        for (long deadline : due.values()) {
            next = Math.min(next, deadline);
        }
        return Math.max(0, next - System.nanoTime());
    }

    private static void deliver(Map<FileWatcher, Long> due) {
        long now = System.nanoTime();
        Iterator<Map.Entry<FileWatcher, Long>> pending = due.entrySet().iterator();

        while (pending.hasNext()) {
            var entry = pending.next();
            if (entry.getValue() - now <= 0) {
                entry.getKey().dispatch();
                pending.remove();
            }
        }
    }
}
