package com.codingchili.core.files;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.exception.NoSuchResourceException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.codingchili.core.configuration.CoreStrings.DIR_SEPARATOR;

/**
 * @author Robin Duda
 * <p>
 * Handles the loading/writing of json objects and lists to/from disk.
 */
public abstract class JsonFileStore {

    /**
     * Reads a JsonObject from a specified application-relative path.
     *
     * @param path the path from where to load the json object.
     * @return the loaded json object.
     */
    public static JsonObject readObject(String path) {
        Optional<Buffer> buffer = new Resource(path).read();
        if (buffer.isPresent()) {
            return buffer.get().toJsonObject();
        } else {
            throw new NoSuchResourceException(path);
        }
    }

    /**
     * Reads a JsonArray from the specified application-relative path.
     *
     * @param path the path from where to load the json array.
     * @return the loaded json array.
     */
    public static JsonArray readList(String path) {
        Optional<Buffer> buffer = new Resource(path).read();
        if (buffer.isPresent()) {
            return buffer.get().toJsonArray();
        } else {
            throw new NoSuchResourceException(path);
        }
    }

    /**
     * Reads a directory of json-files formatted as JsonObjects.
     *
     * @param path the application-relative directory to read from.
     * @return a list of jsonobjects where each object corresponds to a file.
     * returns nothing when more than zero files fails to load.
     */
    public static List<JsonObject> readDirectoryObjects(String path) {
        return readMultiple(path, Buffer::toJsonObject);
    }

    /**
     * Reads a directory of json-files formatted as lists.
     *
     * @param path the application-relative directory to read from.
     * @return a list of jsonarrays where each array corresponds to a file.
     */
    public static List<JsonArray> readDirectoryList(String path) {
        return readMultiple(path, buffer -> new JsonArray(buffer.toString()));
    }

    private static <T> List<T> readMultiple(String path, Function<Buffer, T> transform) {
        File[] files = new File(path).listFiles(file -> !file.isDirectory());
        List<T> objects = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                Optional<Buffer> item = new Resource(path + DIR_SEPARATOR + file.getName()).read();

                if (item.isPresent()) {
                    objects.add(transform.apply(item.get()));
                } else {
                    throw new NoSuchResourceException(path);
                }
            }
        }
        return objects;
    }

    /**
     * Writes a json-object to the given path.
     *
     * @param json   the json-object to write.
     * @param target the path to where the json-object is written to.
     * @throws RuntimeException on failure to write.
     */
    public static void writeObject(JsonObject json, String target) {
        Path path = Paths.get(target).toAbsolutePath();
        try {
            boolean pathExists = path.getParent().toFile().exists() || path.getParent().toFile().mkdirs();

            if (pathExists) {
                Files.write(path, json.encodePrettily().getBytes());
            } else {
                throw new IOException(CoreStrings.getErrorCreateDirectory(target));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the file at the given path.
     *
     * @param path to the file to be deleted.
     * @return true if the file was deleted successfully.
     */
    static boolean deleteObject(String path) {
        File file = new File(path);
        return file.delete();
    }

    /**
     * @param path the path to a file to check if it exists.
     * @return true if the file exists.
     */
    static boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }
}
