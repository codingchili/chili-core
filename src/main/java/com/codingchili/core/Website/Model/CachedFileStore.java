package com.codingchili.core.Website.Model;

import com.codingchili.core.Configuration.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Robin Duda
 */
public class CachedFileStore {
    private HashMap<String, Buffer> files = new HashMap<>();
    private static CachedFileStore instance;
    private String directory;
    private Vertx vertx;

    public static CachedFileStore instance(Vertx vertx, String directory) {
        if (instance == null) {
            instance = new CachedFileStore(vertx, directory);
        }
        return instance;
    }

    private CachedFileStore(Vertx vertx, String path) {
        this.vertx = vertx;
        this.directory = path;

        try {
            loadFiles();
            watchFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void watchFiles() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey watchKey = Paths.get(directory).register(watcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);

        vertx.setPeriodic(2500, handler -> {

            if (watchKey.pollEvents().size() != 0)
                try {
                    synchronized (this) {
                        loadFiles();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        });
    }

    private void loadFiles() throws IOException {
        Path filePath = Paths.get(directory);
        files.clear();

        Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                byte[] fileBytes = Files.readAllBytes(path);

                String filePath = (path.toString().replaceFirst(directory, "")).replace("\\", Strings.DIR_SEPARATOR);
                files.put(filePath, Buffer.buffer(fileBytes));

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public synchronized Buffer getFile(String path) throws NoSuchFileException {
        if (files.containsKey(path)) {
            return files.get(path);
        } else {
            throw new NoSuchFileException(path);
        }
    }
}
