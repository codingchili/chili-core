package com.codingchili.core.files;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.exception.NoFileStoreRegisteredException;
import com.codingchili.core.files.exception.NoSuchResourceException;

/**
 * Supports reading/writing to multiple configuration formats.
 */
public class ConfigurationFactory {
    private static Map<String, FileStore> implementations = new HashMap<>();

    static {
        add(new JsonFileStore());
        add(new YamlFileStore());
    }

    /**
     * @param store the new implementation to add.
     */
    public static void add(FileStore store) {
        store.getExtension().forEach(ext -> implementations.put(ext, store));
    }

    /**
     * @param path contains the file extension that is mapped to a filestore implementation
     * @return a filestore that is capable of handling the file extension, throws an
     * exception if none is registered.
     */
    public static FileStore get(String path) {
        int index = path.lastIndexOf(".");

        if (index > 0) {
            String ext = path.substring(index, path.length());

            if (implementations.containsKey(ext)) {
                return implementations.get(ext);
            } else {
                throw new NoFileStoreRegisteredException(path, ext);
            }
        } else {
            throw new NoFileStoreRegisteredException(path, "<missing>");
        }
    }

    /**
     * Reads a JsonObject from a specified application-relative path.
     *
     * @param path the path from where to load the json object.
     * @return the loaded json object.
     */
    public static JsonObject readObject(String path) {
        Optional<Buffer> buffer = new Resource(path).read();
        if (buffer.isPresent()) {
            return get(path).readObject(buffer.get());
        } else {
            throw new NoSuchResourceException(path);
        }
    }

    /**
     * Lists all items in the given directory of type file.
     *
     * @param directory the directory to enumerate
     * @return a list of absolute file paths.
     */
    public static Collection<String> enumerate(String directory) {
        File[] files = new File(directory).listFiles(file -> !file.isDirectory());

        if (files == null) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(files).map(File::getPath)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Reads a directory of json-files formatted as JsonObjects.
     *
     * @param path the application-relative directory to read from.
     * @return a list of jsonobjects where each object corresponds to a file.
     * returns nothing when more than zero files fails to load.
     */
    public static Collection<JsonObject> readDirectory(String path) {
        List<JsonObject> list = new ArrayList<>();

        for (String file : enumerate(path)) {
            Optional<Buffer> buffer = new Resource(file).read();

            if (buffer.isPresent()) {
                list.add(get(file).readObject(buffer.get()));
            } else {
                throw new NoSuchResourceException(path);
            }
        }
        return list;
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
        boolean pathExists = path.getParent().toFile().exists() || path.getParent().toFile().mkdirs();

        if (pathExists) {
            get(target).writeObject(json, path);
        } else {
            throw new CoreRuntimeException(CoreStrings.getErrorCreateDirectory(target));
        }
    }

    /**
     * Deletes the file at the given path.
     *
     * @param path to the file to be deleted.
     * @return true if the file was deleted successfully.
     */
    public static boolean delete(String path) {
        File file = new File(path);
        return file.delete();
    }

    /**
     * @param path the path to a file to check if it exists.
     * @return true if the file exists.
     */
    public static boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }
}
