package com.codingchili.core.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.ConfigurationFactory;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Who may open the secrets of a configuration file ({@code generate --encrypt}), and which files a block reads
 * ({@code bake}), from the launcher settings:
 * <ul>
 *     <li>{@code configs}: the configuration files of each deployable (class name to path patterns relative to the
 *     configuration directory, without the extension),</li>
 *     <li>{@code blocks}: the deployables of each block,</li>
 *     <li>{@code hosts}: the block of a host; a host named as a block runs that block,</li>
 * </ul>
 * and the public keys of the nodes: one file per host, {@code <nodes>/<host>.pub}, holding the public key that
 * {@code keygen} printed on the node ({@code x25519:<base64>}). The secrets of a file are encrypted to every node
 * whose block has a deployable that reads the file.
 */
public class SecretRecipients {
    private static final String PUBLIC_KEY_EXTENSION = ".pub";
    private final LauncherSettings launcher;
    private final String directory;
    private final Map<String, NodeKey.Public> keys = new TreeMap<>();
    private final Map<String, String> blocks = new TreeMap<>();

    /**
     * @param launcher  the launcher settings: configs, blocks and hosts.
     * @param directory the configuration directory the paths are relative to, for example {@code conf/}.
     * @param nodes     the directory with the public keys of the nodes, {@code <host>.pub}.
     * @throws CoreRuntimeException when there is no public key, a key is invalid or a host runs no block.
     */
    public SecretRecipients(LauncherSettings launcher, String directory, Path nodes) {
        this.launcher = launcher;
        this.directory = directory;

        List<Path> files;
        try (Stream<Path> listed = Files.list(nodes)) {
            files = listed.filter(file -> file.getFileName().toString().endsWith(PUBLIC_KEY_EXTENSION)).sorted().toList();
        } catch (IOException e) {
            files = List.of();
        }
        if (files.isEmpty()) {
            throw new CoreRuntimeException(getNoNodeKeys(nodes.toString()));
        }
        for (Path file : files) {
            String name = file.getFileName().toString();
            String host = name.substring(0, name.length() - PUBLIC_KEY_EXTENSION.length());
            String block = launcher.getHosts().get(host);

            if (block == null && launcher.getBlocks().containsKey(host)) {
                block = host;
            }
            if (block == null || !launcher.getBlocks().containsKey(block)) {
                throw new CoreRuntimeException(getNodeWithoutBlock(host, file.toString(), launcher.getBlocks().keySet()));
            }
            try {
                String text = Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .findFirst().orElse("");
                keys.put(host, NodeKey.Public.parse(text));
            } catch (IOException e) {
                throw new CoreRuntimeException(getNodeKeyUnreadable(file.toString(), e.getMessage()));
            }
            blocks.put(host, block);
        }
    }

    /**
     * @param path a configuration file.
     * @return the hosts whose block reads the file, sorted.
     */
    public List<String> hosts(String path) {
        return blocks.entrySet().stream()
                .filter(entry -> reads(launcher, directory, entry.getValue(), path))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * @param path a configuration file.
     * @return the public keys of the nodes that may open its secrets, empty when no node reads the file.
     */
    public List<NodeKey.Public> keys(String path) {
        return hosts(path).stream().map(keys::get).toList();
    }

    /**
     * @param paths the configuration files.
     * @return the nodes (host, block and public key) and the hosts each file is encrypted to, one line each, sorted.
     * Kept with encrypted values to notice when the nodes, the blocks or the files changed since.
     */
    public String record(Collection<String> paths) {
        var lines = new ArrayList<String>();
        keys.forEach((host, key) -> lines.add(host + " " + blocks.get(host) + " " + key.text()));
        new TreeSet<>(paths).forEach(path -> lines.add(relative(directory, path) + " " + hosts(path)));
        return String.join("\n", lines) + "\n";
    }

    /**
     * @param launcher  the launcher settings.
     * @param directory the configuration directory.
     * @param block     the name of a block.
     * @param path      a configuration file.
     * @return true when a deployable of the block reads the file (launcher settings configs).
     */
    public static boolean reads(LauncherSettings launcher, String directory, String block, String path) {
        String relative = relative(directory, path);
        return launcher.getBlocks().getOrDefault(block, List.of()).stream()
                .flatMap(deployable -> launcher.getConfigs().getOrDefault(deployable, List.of()).stream())
                .anyMatch(pattern -> matches(pattern, relative));
    }

    /**
     * @param launcher the launcher settings.
     * @param block    the name of a block.
     * @return the deployables of the block that have no configuration files in the launcher settings configs.
     */
    public static List<String> unmapped(LauncherSettings launcher, String block) {
        return launcher.getBlocks().getOrDefault(block, List.of()).stream()
                .filter(deployable -> !launcher.getConfigs().containsKey(deployable))
                .toList();
    }

    /**
     * @param directory the configuration directory, for example {@code conf/}.
     * @param path      a file in it.
     * @return the path of the file relative to the directory, with forward slashes.
     */
    public static String relative(String directory, String path) {
        return Paths.get(directory).toAbsolutePath().normalize()
                .relativize(Paths.get(path).toAbsolutePath().normalize())
                .toString().replace('\\', '/');
    }

    // a pattern of configs: the whole relative path without its extension.
    private static boolean matches(String pattern, String relative) {
        String extensions = ConfigurationFactory.supported().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile(pattern + "(" + extensions + ")").matcher(relative).matches();
    }
}
