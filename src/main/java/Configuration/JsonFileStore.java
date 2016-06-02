package Configuration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Robin Duda
 *         handles the loading/writing of json objects and lists to/from disk.
 */
public abstract class JsonFileStore {

    public static JsonObject readObject(String path) throws IOException {
        return new JsonObject(readFile(path));
    }

    public static JsonArray readList(String path) throws IOException {
        return new JsonArray(readFile(path));
    }

    public static ArrayList<JsonObject> readDirectoryObjects(String path) throws IOException {
        File[] files = new File(path).listFiles(file -> !file.isDirectory());
        ArrayList<JsonObject> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonObject(readFile(path + "/" + file.getName())));
            }
        }
        return objects;
    }

    public static ArrayList<JsonArray> readDirectoryList(String path) throws IOException {
        File[] files = new File(path).listFiles();
        ArrayList<JsonArray> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonArray(readFile(path + "/" + file.getName())));
            }
        }
        return objects;
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(FileSystems.getDefault().getPath(currentPath() + "/" + path)));
    }

    public static void writeObject(JsonObject json, String path) {
        Path file = Paths.get(path);
        try {
            Files.write(file, json.encodePrettily().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String currentPath() {
        return Paths.get("").toAbsolutePath().normalize().toString();
    }
}
