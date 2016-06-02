package Meta.Model;

import Configuration.ConfigurationLoader;
import Configuration.JsonFileStore;
import Logging.Model.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Robin Duda
 */
public class PatchKeeper {
    private static String resources = ConfigurationLoader.RESOURCES;
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
        WatchKey watchKey = Paths.get(resources).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        vertx.setPeriodic(2500, handler -> {

            if (watchKey.pollEvents().size() != 0) {
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
        });
    }

    public static PatchKeeper instance(Vertx vertx, Logger logger) {
        if (instance == null) {
            instance = new PatchKeeper(vertx, logger);
        }
        return instance;
    }

    private void loadVersion() throws IOException {
        JsonObject version = JsonFileStore.readObject(resources + "/version.json");

        this.name = version.getString("name");
        this.version = version.getString("version");
    }

    private void loadFiles() throws IOException {
        Path path = Paths.get(resources);
        files.clear();

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();

                String filePath = path.toString().replace("resources", "").replace("\\", "/");
                long fileSize = file.length();
                long fileModified = file.lastModified();
                byte[] fileBytes = Files.readAllBytes(path);

                files.put(filePath, new PatchFile(filePath, fileSize, fileModified, fileBytes));

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });

        patchInfo = new PatchInfo(files, name, version);
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
