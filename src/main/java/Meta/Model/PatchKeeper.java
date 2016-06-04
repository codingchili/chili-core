package Meta.Model;

import Configuration.ConfigurationLoader;
import Configuration.JsonFileStore;
import Logging.Model.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Robin Duda
 */
public class PatchKeeper {
    private static final String RESOURCE_DIR = ConfigurationLoader.RESOURCES;
    private static final String VERSION_FILE = ConfigurationLoader.RESOURCES + "version.json";
    private HashMap<String, PatchFile> files = new HashMap<>();
    private static PatchKeeper instance;
    private Logger logger;
    private PatchInfo patchInfo;
    private String name;
    private String version;
    private Vertx vertx;

    private PatchKeeper(Vertx vertx, Logger logger) {
        this.vertx = vertx;
        this.logger = logger;

        try {
            loadVersion();
            loadFiles();
            watchFiles();
            logger.patchLoaded(name, version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void watchFiles() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey watchKey = Paths.get(RESOURCE_DIR).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        vertx.setPeriodic(2500, handler -> {

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if (VERSION_FILE.endsWith(event.context().toString())) {
                    try {
                        logger.patchReloading(name, version);
                        synchronized (this) {
                            loadVersion();
                            loadFiles();
                        }
                        logger.patchReloaded(name, version);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public static PatchKeeper instance(Vertx vertx, Logger logger) {
        if (instance == null) {
            instance = new PatchKeeper(vertx, logger);
        }
        return instance;
    }

    private void loadVersion() throws IOException {
        JsonObject version = JsonFileStore.readObject(VERSION_FILE);

        this.name = version.getString("name");
        this.version = version.getString("version");
    }

    private void loadFiles() throws IOException {
        Path path = Paths.get(RESOURCE_DIR);
        files.clear();

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();

                String filePath = path.toString().replaceFirst("resources.", "").replace("\\", "/");
                long fileSize = file.length();
                long fileModified = file.lastModified();
                byte[] fileBytes = Files.readAllBytes(path);

                files.put(filePath, new PatchFile(filePath, fileSize, fileModified, gzip(fileBytes)));

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });

        patchInfo = new PatchInfo(files, name, version);
    }

    private byte[] gzip(byte[] file) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(output);
            gzip.write(file);
            gzip.close();
            output.close();
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized PatchFile getPatchFile(String path, String requestVersion) throws PatchReloadedException, NoSuchFileException {
        if (version.compareTo(requestVersion) <= 0) {
            if (files.get(path) != null)
                return files.get(path);
            else {
                throw new NoSuchFileException(path);
            }
        } else
            throw new PatchReloadedException();
    }

    public synchronized PatchInfo getPatchInfo() {
        return patchInfo;
    }

    public synchronized String getVersion() {
        return version;
    }
}
