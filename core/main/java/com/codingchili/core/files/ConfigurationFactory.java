package com.codingchili.core.files;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
        store.getExtensions().forEach(ext -> implementations.put(ext, store));
    }

    /**
     * @return a set of all supported file extensions. The file
     * extension should include the dot if appropriate, for example .yaml.
     */
    public static Set<String> supported() {
        Set<String> supported = new HashSet<>();
        implementations.values().forEach(implementation -> {
            supported.addAll(implementation.getExtensions());
        });
        return supported;
    }

    /**
     * @param path contains the file extension that is mapped to a filestore implementation
     * @return a filestore that is capable of handling the file extension, throws an
     * exception if none is registered.
     */
    public static FileStore get(String path) {
        Optional<String> extension = extension(path);

        if (extension.isPresent()) {
            if (implementations.containsKey(extension.get())) {
                return implementations.get(extension.get());
            } else {
                throw new NoFileStoreRegisteredException(path, extension.get());
            }
        } else {
            throw new NoFileStoreRegisteredException(path, "<missing>");
        }
    }

    /**
     * Reads a JsonObject from a specified application-relative path.
     *
     * @param path the path from where to load the json object. If the file does not exist
     *             or if it does not contain an extension - all supported extensions will
     *             be attempted.
     * @return the loaded json object.
     */
    public static JsonObject readObject(String path) {
        AtomicReference<String> detectedExtension = new AtomicReference<>("");
        Optional<Buffer> buffer = new Resource(path).read();

        // not found - attempt all supported extensions.
        if (!buffer.isPresent()) {
            // but only if the path does not include an extension.
            if (!extension(path).isPresent()) {
                buffer = supported().stream()
                        .map(extension -> {
                            detectedExtension.set(extension);
                            return new Resource(path + extension).read();
                        })
                        .filter(Optional::isPresent)
                        .findFirst().orElse(Optional.empty());
            }

            if (!buffer.isPresent()) {
                throw new NoSuchResourceException(path);
            }
        }
        return get(path + detectedExtension.get()).readObject(buffer.get());
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
        return enumerate(path).parallelStream()
                .map(file -> {
                    Optional<Buffer> buffer = new Resource(file).read();
                    if (buffer.isPresent()) {
                        return get(file).readObject(buffer.get());
                    } else {
                        throw new NoSuchResourceException(path);
                    }
                }).collect(Collectors.toList());
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

    private static Optional<String> extension(String path) {
        int index = path.lastIndexOf(".");

        if (index > 0) {
            return Optional.of(path.substring(index, path.length()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param path the path to a file to check if it exists.
     * @return true if the file exists.
     */
    public static boolean exists(String path) {
        boolean exists = Paths.get(path).toFile().exists();

        // make sure to check all supported file extensions if none is provided.
        if (!extension(path).isPresent()) {
            exists = supported().stream()
                    .anyMatch(ext -> Paths.get(path + ext)
                            .toFile().exists()
                    );
        }
        return exists;
    }
}
