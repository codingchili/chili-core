package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.files.Configurations.launcher;

/**
 * Writes the configuration files that have security dependencies with the secrets of a store ({@code bake}), for a
 * release (an image) that carries its secrets:
 * <ul>
 *     <li>{@code <out>/<path in the configuration directory>}: every file,</li>
 *     <li>{@code <out>/blocks/<block>/<path in the configuration directory>}: the files each block reads (launcher
 *     settings {@code configs}), for an image per block.</li>
 * </ul>
 * The generated keys of the configuration (development secrets) are removed, the values of the store take their
 * place: the encrypted values ({@code <store>/encrypted}) when the store has them, else the plain ones. Without a
 * store (or an empty one) the files are written without any secrets. A file the store has no values for fails the
 * bake (generate adds them), except with encrypted values for a file that no node reads.
 */
public class SecretBaker {
    /**
     * the folder of the files of each block in the output.
     */
    public static final String DIR_BLOCKS = "blocks";
    private final Logger logger = new ConsoleLogger(getClass());
    private final String directory;
    private Path store;
    private Path out = Paths.get(BAKE_OUT_DEFAULT);
    private List<String> blocks;

    /**
     * What the baked files hold.
     */
    public enum Secrets {
        /**
         * no secrets: no store.
         */
        NONE,
        /**
         * the plain values of the store.
         */
        PLAIN,
        /**
         * the encrypted values of the store.
         */
        ENCRYPTED;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * @param directory the configuration directory, for example {@code conf/}.
     */
    public SecretBaker(String directory) {
        this.directory = directory;
    }

    /**
     * @param store the secrets store, null or missing for none.
     * @return fluent
     */
    public SecretBaker setStore(Path store) {
        this.store = store;
        return this;
    }

    /**
     * @param out the directory to write to, replaced.
     * @return fluent
     */
    public SecretBaker setOut(Path out) {
        this.out = out;
        return this;
    }

    /**
     * @param blocks the blocks to write the files of, null for every block of the launcher settings.
     * @return fluent
     */
    public SecretBaker setBlocks(List<String> blocks) {
        this.blocks = blocks;
        return this;
    }

    /**
     * Writes the files, see the class.
     *
     * @return what the written files hold.
     * @throws CoreRuntimeException when the store misses values or its encrypted values are for other nodes.
     */
    public Secrets bake() {
        LauncherSettings launcher = launcher();
        Secrets mode = mode();
        Path source = (mode == Secrets.ENCRYPTED) ? store.resolve(DIR_ENCRYPTED) : store;
        SecretRecipients recipients = null;
        List<String> selected = (blocks == null) ? new ArrayList<>(new TreeSet<>(launcher.getBlocks().keySet())) : blocks;

        selected.forEach(block -> {
            if (!launcher.getBlocks().containsKey(block)) {
                throw new CoreRuntimeException(getBlockNotConfigured(block));
            }
        });
        var referenced = AuthenticationGenerator.referenced(directory);
        if (mode == Secrets.ENCRYPTED) {
            recipients = new SecretRecipients(launcher, directory, store.resolve(DIR_NODES));
            if (!recipients.record(referenced.keySet()).equals(read(source.resolve(NODES_RECORD)))) {
                throw new CoreRuntimeException(getBakeNodesChanged(store.toString()));
            }
        }
        Map<String, JsonObject> baked = new LinkedHashMap<>();

        for (Map.Entry<String, List<AuthenticationDependency>> entry : referenced.entrySet()) {
            String path = entry.getKey();
            String relative = SecretRecipients.relative(directory, path);
            Set<String> keys = AuthenticationGenerator.generatedKeys(entry.getValue());
            JsonObject json = ConfigurationFactory.readObject(path);
            keys.forEach(json::remove);

            if (mode != Secrets.NONE) {
                Path saved = source.resolve(relative);
                JsonObject values = Files.exists(saved) ? ConfigurationFactory.readObject(saved.toString()) : new JsonObject();
                Set<String> missing = new TreeSet<>(keys);
                missing.removeAll(values.fieldNames());

                if (mode == Secrets.ENCRYPTED && recipients.keys(path).isEmpty()) {
                    logger.log(getBakeWithoutSecrets(path));
                } else if (!missing.isEmpty()) {
                    throw new CoreRuntimeException(getBakeMissing(path, missing, store.toString()));
                } else {
                    json.mergeIn(values);
                }
            }
            baked.put(relative, json);
        }
        if (Files.exists(out)) {
            AuthenticationGenerator.delete(out);
        }
        baked.forEach((relative, json) -> AuthenticationGenerator.writePrivate(json, out.resolve(relative)));

        for (String block : selected) {
            List<String> files = new ArrayList<>();
            baked.forEach((relative, json) -> {
                if (SecretRecipients.reads(launcher, directory, block, Paths.get(directory, relative).toString())) {
                    AuthenticationGenerator.writePrivate(json, out.resolve(DIR_BLOCKS).resolve(block).resolve(relative));
                    files.add(relative);
                }
            });
            List<String> unmapped = SecretRecipients.unmapped(launcher, block);
            if (!unmapped.isEmpty()) {
                logger.log(getBakeUnmapped(block, unmapped));
            }
            logger.log(getBakedBlock(block, files));
        }
        logger.log(getBaked(baked.size(), out.toString(), mode.toString()));
        return mode;
    }

    private Secrets mode() {
        if (store == null || !Files.isDirectory(store)) {
            return Secrets.NONE;
        } else if (Files.isDirectory(store.resolve(DIR_ENCRYPTED))) {
            return Secrets.ENCRYPTED;
        } else {
            try (var files = Files.walk(store)) {
                boolean values = files.anyMatch(file -> !file.startsWith(store.resolve(DIR_NODES)) &&
                        ConfigurationFactory.supported().stream().anyMatch(file.toString()::endsWith));
                return (values) ? Secrets.PLAIN : Secrets.NONE;
            } catch (IOException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private static String read(Path path) {
        try {
            return Files.exists(path) ? Files.readString(path) : "";
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
