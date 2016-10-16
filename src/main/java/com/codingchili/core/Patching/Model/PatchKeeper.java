package com.codingchili.core.Patching.Model;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Patching.Configuration.PatchNotes;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import static com.codingchili.core.Configuration.Strings.PATH_PATCHSERVER;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author Robin Duda
 */
public class PatchKeeper {
    private final HashMap<String, PatchFile> files = new HashMap<>();
    private static PatchKeeper instance;
    private final PatchListener listener;
    private PatchDetails details;
    private PatchNotes notes;
    private final Vertx vertx;

    private PatchKeeper(Vertx vertx, PatchListener listener) {
        PatchServerSettings settings = FileConfiguration.get(PATH_PATCHSERVER, PatchServerSettings.class);
        this.vertx = vertx;
        this.listener = listener;
        this.notes = settings.getPatch();

        try {
            loadFiles();
            watchFiles();
            listener.onPatchLoaded(notes.getName(), notes.getVersion());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void watchFiles() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey watchKey = Paths.get(Strings.DIR_SYSTEM).register(watcher, ENTRY_MODIFY);

        vertx.setPeriodic(2500, handler -> {

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if (PATH_PATCHSERVER.endsWith(event.context().toString())) {
                    try {
                        listener.onPatchReloading(notes.getName(), notes.getVersion());
                        synchronized (this) {
                            loadFiles();
                        }
                        listener.onPatchReloaded(notes.getName(), notes.getVersion());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public static PatchKeeper instance(Vertx vertx, PatchListener listener) {
        if (instance == null) {
            instance = new PatchKeeper(vertx, listener);
        }
        return instance;
    }

    private void loadFiles() throws IOException {
        Path path = Paths.get(Strings.DIR_RESOURCES);
        files.clear();

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();

                String filePath = (path.toString().replaceFirst(Strings.ID_RESOURCES + ".", "")).replace("\\", Strings.DIR_SEPARATOR);
                long fileSize = file.length();
                long fileModified = file.lastModified();
                byte[] fileBytes = Files.readAllBytes(path);

                files.put(filePath, new PatchFile(filePath, fileSize, fileModified, Serializer.gzip(fileBytes)));
                listener.onFileLoaded(filePath);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });

        FileConfiguration.reload(PATH_PATCHSERVER);
        notes = FileConfiguration.<PatchServerSettings>get(PATH_PATCHSERVER, PatchServerSettings.class).getPatch();
        details = new PatchDetails(files, notes.getName(), notes.getVersion());
    }

    public synchronized PatchFile getFile(String path, String requestVersion) throws PatchReloadedException, NoSuchFileException {
        if (notes.getVersion().compareTo(requestVersion) <= 0) {
            if (files.get(path) != null)
                return files.get(path);
            else {
                throw new NoSuchFileException(path);
            }
        } else
            throw new PatchReloadedException();
    }

    public synchronized PatchDetails getDetails() {
        return details;
    }

    public synchronized PatchNotes getPatchNotes() {
        return notes;
    }

    public synchronized String getVersion() {
        return notes.getVersion();
    }
}
