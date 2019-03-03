package com.codingchili.core.files;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for filestore providers.
 */
public interface FileStore {

    /**
     * Reads a T object from a specified application-relative path.
     *
     * @param buffer data of the object to read to json
     * @return the loaded json object.
     */
    JsonObject readObject(Buffer buffer);

    /**
     * Writes a json-object to the given path.
     *
     * @param object the object to write.
     * @param path   the path to where the object is written to.
     * @throws RuntimeException on failure to write.
     */
    void writeObject(JsonObject object, Path path);

    /**
     * @return the extension handled by this file store.
     */
    List<String> getExtensions();
}
