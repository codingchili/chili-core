package com.codingchili.core.Configuration;

import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.Util.Serializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.codingchili.core.Configuration.Strings.ID_CONFIGURATION;

/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration {
    private static final ConcurrentHashMap<String, ConfigEntry> cache = new ConcurrentHashMap<>();
    private static final Logger logger = new DefaultLogger();

    /**
     * Loads a configurable from specified path and instantiates a settings object.
     *
     * @param path  the path to the json configuration.
     * @param clazz a class with settings that extends a Configurable.
     * @return an instantiated configurable.
     */
    public static <T extends LoadableConfigurable> T get(String path, Class clazz) {
        if (cache.containsKey(path)) {
            return (T) cache.get(path).configurable;
        } else {
            return load(path, clazz);
        }
    }

    /**
     * Reloads a configuration file from specified path.
     *
     * @param path of the configurable to reload.
     * @return an instantiated configurable.
     */
    public static void reload(String path) {
        if (cache.containsKey(path)) {
            FileConfiguration.load(path, cache.get(path).clazz);
        }
    }

    /**
     * Saves a configuration to file without updating the cache.
     *
     * @param configurable the configurable to be written.
     */
    public static void save(LoadableConfigurable configurable) {
        JsonFileStore.writeObject(configurable.serialize(), configurable.getPath());
        logger.onFileSaved(ID_CONFIGURATION, configurable.getPath());
    }

    /**
     * Get all loaded configurables, useful for modifying them before saving.
     *
     * @return a list of all the configurables loaded.
     */
    public static Collection<LoadableConfigurable> loaded() {
        ArrayList<LoadableConfigurable> loaded = new ArrayList<>();

        cache.values().forEach(entry -> loaded.add(entry.configurable));

        return loaded;
    }

    /**
     * List all available configuration files.
     *
     * @param path root path to search from.
     * @return a list of paths to configuration files.
     */
    public static Collection<String> available(String path) {
        try {
            return Files.walk(Paths.get(path))
                    .filter(file -> !file.toFile().isDirectory())
                    .map(Path::toString)
                    .map(text -> text.replaceAll("\\\\", Strings.DIR_SEPARATOR))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileReadException(path);
        }
    }

    /**
     * List all available configuration files from the server root.
     *
     * @return a list of paths to configuration files.
     */
    public static Collection<String> available() {
        return available("");
    }

    private static <T extends LoadableConfigurable> T load(String path, Class clazz) {
        try {
            T config = Serializer.unpack(JsonFileStore.readObject(path), clazz);
            cache.put(path, new ConfigEntry(config, clazz));
            logger.onFileLoaded(ID_CONFIGURATION, path);
            return config;
        } catch (IOException e) {
            logger.onFileLoadError(path);
            throw new FileReadException(path);
        }
    }

    /**
     * Clears all loaded configuration and reloads on next get.
     */
    public static void unload() {
        cache.clear();
        logger.onCacheCleared(ID_CONFIGURATION);
    }

    private static class ConfigEntry {
        final LoadableConfigurable configurable;
        final Class clazz;

        ConfigEntry(LoadableConfigurable configurable, Class clazz) {
            this.configurable = configurable;
            this.clazz = clazz;
        }
    }
}