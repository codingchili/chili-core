package com.codingchili.core.Files;

import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.*;
import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Exception.FileReadException;
import com.codingchili.core.Logging.ConsoleLogger;
import com.codingchili.core.Protocol.Serializer;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class Configurations {
    private static final ConcurrentHashMap<String, ConfigEntry> configs = new ConcurrentHashMap<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean monitoring = new AtomicBoolean(true);
    private static final ConsoleLogger logger = new ConsoleLogger();

    public static void initialize(CoreContext context) {
        if (initialized.get()) {
            context.console().onAlreadyInitialized();
        } else {
            new FileWatcher.FileWatcherBuilder(context)
                    .rate(Configurations::getConfigurationPoll)
                    .onDirectory(Strings.DIR_CONFIG)
                    .withListener(new ConfigurationFileWatcher())
                    .build();

            initialized.set(true);
        }
    }

    private static int getConfigurationPoll() {

        return system().getConfigurationPoll();
    }

    private static class ConfigurationFileWatcher implements FileStoreListener {
        @Override
        public void onFileModify(Path path) {
            if (monitoring.get()) {
                Configurations.reload(Strings.format(path, DIR_CONFIG));
            }
        }

        @Override
        public void onFileRemove(Path path) {
            if (monitoring.get()) {
                Configurations.reload(Strings.format(path, DIR_CONFIG));
            }
        }
    }

    /**
     * Loads a configurable from specified path and instantiates a settings object.
     *
     * @param path  the path to the json configuration.
     * @param clazz a class with settings that extends a Configurable.
     * @return an instantiated configurable.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Configurable> T get(String path, Class clazz) {
        if (configs.containsKey(path) && configs.get(path).clazz.equals(clazz)) {
            return (T) configs.get(path).configurable;
        } else {
            return load(path, clazz);
        }
    }

    /**
     * Disables monitoring of changes, saves all configurations back to disk.
     */
    public static void shutdown() {
        monitoring.set(false);
        loaded().stream().forEach(Configurations::save);
    }

    private static <T extends Configurable> T load(String path, Class clazz) {
        try {
            JsonObject json = JsonFileStore.readObject(path);
            T config = Serializer.unpack(json, clazz);

            config.setPath(path);

            configs.put(path, new ConfigEntry(config, clazz));
            logger.onFileLoaded(path);
            return config;
        } catch (IOException e) {
            logger.onFileLoadError(Strings.getFileReadError(path));
            throw new FileReadException(path);
        }
    }

    /**
     * Checks if an alternate file with the same name exists in another folder.
     *
     * @param filePath  The file to check if exists in another directory.
     * @param overrideDir The other directory to check in.
     * @return a path to the overridden resource if exists or the filePath itself.
     */
    public static String override(String rootDir, String overrideDir, String filePath) {
        String overrideFile = filePath.replace(rootDir, overrideDir);

        File override = Paths.get(overrideFile).toFile();

        if (override.exists()) {
            return Strings.format(override.toPath());
        } else {
            return filePath;
        }
    }

    /**
     * Reloads a configuration file from specified path.
     *
     * @param path of the configurable to reload.
     */
    static void reload(String path) {
        if (configs.containsKey(path)) {
            Configurations.load(path, configs.get(path).clazz);
        }
    }

    /**
     * Saves a configuration to file without updating the cache.
     *
     * @param configurable the configurable to be written.
     */
    static void save(Configurable configurable) {
        JsonFileStore.writeObject(configurable.serialize(), configurable.getPath());
        logger.onFileSaved(ID_CONFIGURATION, configurable.getPath());
    }

    /**
     * Get all loaded configurables, useful for modifying them before saving.
     *
     * @return a list of all the configurables loaded.
     */
    public static Collection<Configurable> loaded() {
        ArrayList<Configurable> loaded = new ArrayList<>();

        configs.values().forEach(entry -> loaded.add(entry.configurable));

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
    static Collection<String> available() {
        return available("");
    }

    /**
     * Clears all loaded configuration and reloads on next get.
     */
    public static void unload() {
        configs.clear();
        logger.onCacheCleared(ID_CONFIGURATION);
    }

    public static SystemSettings system() {
        return get(PATH_VERTX, SystemSettings.class);
    }

    public static SecuritySettings security() {
        return get(PATH_SECURITY, SecuritySettings.class);
    }

    public static ValidatorSettings validator() {
        return get(PATH_VALIDATOR, ValidatorSettings.class);
    }

    public static LauncherSettings launcher() {
        return get(PATH_LAUNCHER, LauncherSettings.class);
    }

    private static class ConfigEntry {
        final Configurable configurable;
        final Class clazz;

        ConfigEntry(Configurable configurable, Class clazz) {
            this.configurable = configurable;
            this.clazz = clazz;
        }
    }
}