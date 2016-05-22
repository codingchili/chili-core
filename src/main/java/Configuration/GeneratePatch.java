package Configuration;

import Utilities.JsonFileStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Robin Duda
 */
public class GeneratePatch {
    private static String resourcePath = Paths.get("resources/game/").toString();
    private static String outputPath = Paths.get("resources/patch.json").toString();

    public static void main(String[] args) throws IOException {
        GeneratePatch.generate();
    }

    public static void generate() throws IOException {
        JsonObject patch = JsonFileStore.readObject(outputPath);
        patch.remove("files");
        patch.put("files", listFiles());
        JsonFileStore.writeObject(patch, outputPath);
    }

    private static JsonArray listFiles() throws IOException {
        JsonArray files = new JsonArray();
        Path path = Paths.get(resourcePath);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                File file = path.toFile();

                files.add(new JsonObject()
                        .put("path", path.toString())
                        .put("size", file.length())
                        .put("modified", file.lastModified()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }
}
