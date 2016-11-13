package com.codingchili.core.Files;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Handles the loading/writing of json objects and lists to/from disk.
 */
public abstract class JsonFileStore {

    /**
     * Reads a JsonObject from a specified application-relative path.
     * @param path the path from where to load the json object.
     * @return the loaded json object.
     * @throws IOException when the file cannot be found or read.
     */
    public static JsonObject readObject(String path) throws IOException {
        return new JsonObject(readFile(path));
    }

    /**
     * Reads a JsonArray from the specified application-relative path.
     * @param path the path from where to load the json array.
     * @return the loaded json array.
     * @throws IOException when the file cannot be found or read.
     */
    public static JsonArray readList(String path) throws IOException {
        return new JsonArray(readFile(path));
    }

    /**
     * Reads a directory of json-files formatted as JsonObjects.
     * @param path the application-relative directory to read from.
     * @return a list of jsonobjects where each object corresponds to a file.
     * @throws IOException when the file cannot be found or read.
     */
    public static ArrayList<JsonObject> readDirectoryObjects(String path) throws IOException {
        File[] files = new File(path).listFiles(file -> !file.isDirectory());
        ArrayList<JsonObject> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonObject(readFile(path + Strings.DIR_SEPARATOR + file.getName())));
            }
        }
        return objects;
    }

    /**
     * Reads a directory of json-files formatted as lists.
     * @param path the application-relative directory to read from.
     * @return a list of jsonarrays where each array corresponds to a file.
     * @throws IOException when the file cannot be found or read.
     */
    public static ArrayList<JsonArray> readDirectoryList(String path) throws IOException {
        File[] files = new File(path).listFiles();
        ArrayList<JsonArray> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                objects.add(new JsonArray(readFile(path + Strings.DIR_SEPARATOR + file.getName())));
            }
        }
        return objects;
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(FileSystems.getDefault().getPath(currentPath() + Strings.DIR_SEPARATOR + path)));
    }

    /**
     * Writes a json-object to the given path.
     * @param json the json-object to write.
     * @param target the path to where the json-object is written to.
     * @throws RuntimeException on failure to write.
     */
    public static void writeObject(JsonObject json, String target) {
        Path path = Paths.get(target);
        try {
            boolean pathExists = path.getParent().toFile().exists() || path.getParent().toFile().mkdirs();

            if (pathExists) {
                Files.write(path, json.encodePrettily().getBytes());
            } else {
                throw new IOException(Strings.getErrorCreateDirectory(target));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String currentPath() {
        return Paths.get("").toAbsolutePath().normalize().toString();
    }

    /**
     * Deletes the file at the given path.
     * @param path to the file to be deleted.
     * @return true if the file was deleted successfully.
     */
    static boolean deleteObject(String path) {
        File file = new File(path);
        return file.delete();
    }

    static boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }
}
