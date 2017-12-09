package com.codingchili.core.files;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Robin Duda
 * <p>
 * Handles the loading/writing of json objects and lists to/from disk.
 */
public class JsonFileStore implements FileStore {

    @Override
    public JsonObject readObject(Buffer buffer) {
        return buffer.toJsonObject();
    }

    @Override
    public void writeObject(JsonObject json, Path path) {
        try {
            Files.write(path, json.encodePrettily().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    public List<String> getExtension() {
        return Collections.singletonList(CoreStrings.EXT_JSON);
    }
}
