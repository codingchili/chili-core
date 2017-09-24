package com.codingchili.core.files;

import com.codingchili.core.files.exception.FileWriteException;
import io.vertx.core.buffer.Buffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.codingchili.core.configuration.CoreStrings.getAbsolutePath;

/**
 * @author Robin Duda
 * <p>
 * Reads a file from the filesystem or the classpath.
 */
public class Resource {
    private String path;


    /**
     * Creates a new resource reference.
     *
     * @param path path to the resource (classpath or file.)
     */
    public Resource(String path) {
        this.path = path;
    }

    /**
     * Reads a file from the filesystem or the classpath.
     * Prefers resources on the filesystem as bundled files
     * on the classpath may be overriden.
     *
     * @return a buffer if found on the classpath or filesystem.
     */
    public Optional<Buffer> read() {
        Optional<Buffer> file = readFromFS();
        if (!file.isPresent()) {
            file = readFromCP();
        }
        return file;
    }

    public void write(Buffer buffer) {
        Path path = Paths.get(this.path);
        try {
            Files.write(path, buffer.getBytes());
        } catch (IOException e) {
            throw new FileWriteException(this.path);
        }
    }

    /**
     * Reads a resource from the filesystem.
     *
     * @return a buffer if the file is loaded successfully.
     */
    public Optional<Buffer> readFromFS() {
        Path path = Paths.get(getAbsolutePath(this.path));
        try {
            if (path.toFile().exists()) {
                return Optional.of(Buffer.buffer(
                        Files.readAllBytes(path)));
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Reads a resource from the classpath.
     *
     * @return a buffer if the classpath resource is loaded successfully
     */
    public Optional<Buffer> readFromCP() {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return Optional.empty();
        } else {
            return Optional.of(Buffer.buffer(
                    new BufferedReader(
                            new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"))));
        }
    }

}
