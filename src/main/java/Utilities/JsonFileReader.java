package Utilities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-25.
 */
public abstract class JsonFileReader {

    public static JsonObject readObject(String path) throws IOException {
        return new JsonObject(readFile(path));
    }

    public static JsonArray readList(String path) throws IOException {
        return new JsonArray(readFile(path));
    }

    public static ArrayList<JsonObject> readDirectoryObjects(String path) throws IOException {
        File[] files = new File(path).listFiles();
        ArrayList<JsonObject> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonObject(readFile(path + "\\" + file.getName())));
            }
        }
        return objects;
    }

    public static ArrayList<JsonArray> readDirectoryList(String path) throws IOException {
        File[] files = new File(path).listFiles();
        ArrayList<JsonArray> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonArray(readFile(path + "\\" + file.getName())));
            }
        }
        return objects;
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(FileSystems.getDefault().getPath(currentPath() + "\\" + path)));
    }

    private static String currentPath() {
        return Paths.get("").toAbsolutePath().normalize().toString();
    }
}
