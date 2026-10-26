package com.codingchili.core.files;

import java.util.Objects;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;

/**
 * File watcher builder.
 */
public class FileWatcherBuilder {
    private final FileWatcher watcher;

    FileWatcherBuilder(CoreContext context) {
        this.watcher = new FileWatcher(context);
    }

    /**
     * Sets the listener that is called on file changes.
     *
     * @param listener the listener to be used.
     * @return fluent.
     */
    public FileWatcherBuilder withListener(FileStoreListener listener) {
        watcher.setListener(listener);
        return this;
    }

    /**
     * Defines the directory that should be watched relative from the application root.
     *
     * @param directory the directory to be watched, includes its subdirectories.
     * @return fluent
     */
    public FileWatcherBuilder onDirectory(String directory) {
        watcher.setDirectory(directory);
        return this;
    }

    /**
     * The time to wait for further changes before the listener is notified, changes to the
     * same file within this time are reported once. Limited to {@link FileWatcher#MAX_DEBOUNCE_MS}.
     *
     * @param rate a timersource in milliseconds.
     * @return fluent
     */
    public FileWatcherBuilder rate(TimerSource rate) {
        watcher.setRate(rate);
        return this;
    }

    /**
     * Constructs the new FileWatcher.
     *
     * @return fluent
     */
    public FileWatcher build() {
        Objects.requireNonNull(watcher.directory, "Directory is not set for the file watcher.");
        Objects.requireNonNull(watcher.listener, "No listener is set for the file watcher.");
        Objects.requireNonNull(watcher.rate, "No polling rate is set for the file watcher.");
        watcher.initialize();
        return watcher;
    }
}
