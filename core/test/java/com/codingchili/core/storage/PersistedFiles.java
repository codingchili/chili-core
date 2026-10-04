package com.codingchili.core.storage;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;

import java.io.File;
import java.util.List;

/**
 * Releases and removes the files of disk based storages that were created by tests.
 * <p>
 * The SQLite database of {@link IndexedMapPersisted} stays open (and cannot be deleted
 * on Windows) until it is closed, closing is only safe once no more tests use the storage as
 * the storage loader shares one instance per database and collection.
 */
final class PersistedFiles {
    // SQLite creates these next to the database when running in WAL mode or with a journal.
    private static final List<String> SQLITE_SUFFIXES = List.of("", "-wal", "-shm", "-journal");

    private PersistedFiles() {
    }

    /**
     * Closes the database of a disk based storage and deletes its files, the directory
     * that contained the files is deleted if it is empty afterwards. Does nothing for
     * storages that are not stored on disk using SQLite.
     *
     * @param storage the storage to release.
     */
    static void remove(AsyncStorage<?> storage) {
        if (storage instanceof IndexedMap<?> map && map.getDatabase().getPersistence() instanceof DiskPersistence<?, ?> disk) {
            File database = disk.getFile();
            disk.close();

            SQLITE_SUFFIXES.forEach(suffix -> new File(database.getPath() + suffix).delete());

            File directory = database.getAbsoluteFile().getParentFile();
            if (directory != null) {
                // does nothing when the directory is not empty.
                directory.delete();
            }
        }
    }
}
