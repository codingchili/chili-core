package com.codingchili.core.security;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Moves the saved values of renamed configuration files in a secrets store (security settings {@code renames}), so
 * that a store made before the rename keeps its keys: {@code <store>/<old>.<ext>} to {@code <store>/<new>.<ext>},
 * the same in {@code encrypted/} (encrypted values are not bound to the file), and the file's line of the record of
 * the encrypted values ({@code encrypted/nodes.txt}). Once: a store without the old file is left as it is. Used by
 * {@code generate --store} and {@code bake --store}.
 */
public final class SecretStoreRenames {
    private SecretStoreRenames() {
    }

    /**
     * @param store   the secrets store, nothing happens when it does not exist.
     * @param renames the old path to the new one, relative to the configuration directory, without the extension.
     * @param logger  logs each move.
     * @return the number of files moved.
     */
    public static int apply(Path store, Map<String, String> renames, Logger logger) {
        int moved = 0;
        if (store == null || renames == null || renames.isEmpty() || !Files.isDirectory(store)) {
            return moved;
        }
        Path encrypted = store.resolve(DIR_ENCRYPTED);
        Map<String, String> recorded = new LinkedHashMap<>();

        for (Map.Entry<String, String> rename : renames.entrySet()) {
            for (String extension : ConfigurationFactory.supported()) {
                String from = rename.getKey() + extension;
                String to = rename.getValue() + extension;
                for (Path root : List.of(store, encrypted)) {
                    if (move(root, from, to, logger)) {
                        moved++;
                        if (root.equals(encrypted)) {
                            recorded.put(from, to);
                        }
                    }
                }
            }
        }
        if (!recorded.isEmpty()) {
            record(encrypted.resolve(NODES_RECORD), recorded);
        }
        return moved;
    }

    private static boolean move(Path root, String from, String to, Logger logger) {
        Path source = root.resolve(from);
        Path target = root.resolve(to);
        if (!Files.isRegularFile(source)) {
            return false;
        }
        if (Files.exists(target)) {
            logger.log(getStoreRenameConflict(source.toString(), target.toString()));
            return false;
        }
        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            logger.log(getStoreRenamed(source.toString(), target.toString()));
            return true;
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }

    // the record: the node lines first, then one line per file ("<relative path> [hosts]"), sorted by the path.
    private static void record(Path record, Map<String, String> renamed) {
        if (!Files.isRegularFile(record)) {
            return;
        }
        try {
            List<String> nodes = new ArrayList<>();
            TreeMap<String, String> files = new TreeMap<>();
            for (String line : Files.readAllLines(record)) {
                if (line.isBlank()) {
                    continue;
                }
                String path = line.substring(0, Math.max(0, line.indexOf(' ')));
                if (line.endsWith("]") && !path.isEmpty()) {
                    String name = renamed.getOrDefault(path, path);
                    files.put(name, name + line.substring(path.length()));
                } else {
                    nodes.add(line);
                }
            }
            List<String> lines = new ArrayList<>(nodes);
            lines.addAll(files.values());
            Files.writeString(record, String.join("\n", lines) + "\n");
        } catch (IOException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
