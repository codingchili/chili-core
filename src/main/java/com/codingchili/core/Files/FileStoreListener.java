package com.codingchili.core.files;

import java.nio.file.Path;

/**
 * @author Robin Duda
 *         <p>
 *         Interface to be used with a FileWatcher to be notified of changes.
 */
interface FileStoreListener {
    /**
     * Triggered when a file on the registered path has been modified or created.
     *
     * @param path the basedir-relative path to the file that was modified.
     */
    default void onFileModify(Path path) {
    }

    /**
     * Triggered when a file on the registered path has been deleted.
     *
     * @param path the basedir-relative path to the file that was modified.
     */
    default void onFileRemove(Path path) {
    }
}
