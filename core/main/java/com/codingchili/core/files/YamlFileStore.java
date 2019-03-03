package com.codingchili.core.files;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Serializer;

/**
 * Parses YAML configuration files into JsonObjects.
 */
public class YamlFileStore implements FileStore {

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject readObject(Buffer buffer) {
        return new JsonObject(Serializer.unyaml(buffer.toString(StandardCharsets.UTF_8), Map.class));
    }

    @Override
    public void writeObject(JsonObject object, Path path) {
        try {
            Files.write(path, Serializer.yaml(object).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(CoreStrings.EXT_YAML, CoreStrings.EXT_YML);
    }
}
