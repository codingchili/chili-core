package com.codingchili.core.files;

import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.exception.InvalidConfigurableException;
import com.codingchili.core.configuration.system.*;
import com.codingchili.core.context.StartupListener;
import com.codingchili.core.files.exception.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Handles loading and parsing of the configuration files.
 */
public abstract class Configurations {
    private static final ConcurrentHashMap<String, ConfigEntry> configs = new ConcurrentHashMap<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Logger logger = new ConsoleLogger(Configurations.class);

    /*
      When uninitialized default in-memory configuration is used, this configuration is
      flushed on initialization. This allows testing against the default configuration and
      without file access or modification to configuration paths when testing.
      Also allows configuration files to not be present at startup and reset.
     */
    static {
        init();
        reloadAll();

        StartupListener.subscribe(core -> {
            if (initialized.get()) {
                logger.onAlreadyInitialized();
            } else {
                logger = core.logger(Configurations.class);
                new FileWatcherBuilder(core)
                        .rate(Configurations::getConfigurationPoll)
                        .onDirectory(Paths.get(launcher().getConfigurationDirectory()).toString())
                        .withListener(new ConfigurationFileWatcher())
                        .build();

                initialized.set(true);
            }
        });
    }

    private static void init() {
        configs.put(PATH_LAUNCHER, new ConfigEntry(new LauncherSettings(), LauncherSettings.class));
        configs.put(PATH_SECURITY, new ConfigEntry(new SecuritySettings(), SecuritySettings.class));
        configs.put(PATH_SYSTEM, new ConfigEntry(new SystemSettings(), SystemSettings.class));
        configs.put(PATH_STORAGE, new ConfigEntry(new StorageSettings(), StorageSettings.class));
    }

    private static void reloadAll() {
        loaded().forEach(configurable -> reload(configurable.getPath()));
    }

    private static int getConfigurationPoll() {
        return system().getConfigurationPoll();
    }

    /**
     * Loads a configurable from specified path and instantiates a settings object.
     *
     * @param <T>   type of the configurable bound by clazz
     * @param path  the path to the json configuration.
     * @param clazz a class with settings that extends a Configurable.
     * @return an instantiated configurable.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Configurable> T get(String path, Class<T> clazz) {
        Supplier<Boolean> loaded = () -> configs.containsKey(path) && configs.get(path).clazz.equals(clazz);

        if (path == null) {
            throw new InvalidConfigurationPath(clazz);
        }

        if (loaded.get()) {
            return (T) configs.get(path).configurable;
        } else {
            // if not loaded: synchronization is required to avoid multiple loads of a single file.
            synchronized (Configurations.class) {
                // synchronized check if loaded: for second and later threads waiting.
                if (loaded.get()) {
                    return (T) configs.get(path).configurable;
                } else {
                    // load synchronized for first thread.
                    return load(path, clazz);
                }
            }
        }
    }

    /**
     * Inserts a configuration file from memory into the configuration store.
     *
     * @param configurable the configurable to be stored.
     */
    public static void put(Configurable configurable) {
        configs.put(configurable.getPath(), new ConfigEntry(configurable, configurable.getClass()));
    }

    /**
     * Disables monitoring of changes, saves all configurations back to disk.
     */
    static void shutdown() {
        initialized.set(false);
        saveAll();
        init();
    }

    private static void saveAll() {
        loaded().forEach(Configurations::save);
    }

    /**
     * Reads a configurable from the given path into loaded configurables. If the
     * given path does not resolve to a configuration file the configuration file
     * is instead instantiated from the given class.
     *
     * @param path  the path to the class to instantiate.
     * @param clazz the configurable class on the path.
     */
    private static <T extends Configurable> T load(String path, Class<T> clazz) {
        boolean defaultsLoaded = false;
        T config;

        if (ConfigurationFactory.exists(path)) {
            try {
                JsonObject json = ConfigurationFactory.readObject(path);
                config = Serializer.unpack(json, clazz);
            } catch (NoSuchResourceException e) {
                logger.onFileLoadError(CoreStrings.getFileReadError(path));
                throw e;
            }
        } else {
            try {
                config = clazz.<T>getConstructor().newInstance();
                defaultsLoaded = true;
            } catch (ReflectiveOperationException e) {
                logger.onInvalidConfigurable(clazz);
                throw new InvalidConfigurableException(clazz);
            }
        }
        config.setPath(path);
        configs.put(path, new ConfigEntry(config, clazz));

        if (defaultsLoaded) {
            if (!clazz.getName().equals(LauncherSettings.class.getName())) {
                // we cannot warn if the launcher defaults are loaded.
                if (launcher().isWarnOnDefaultsLoaded()) {
                    logger.onConfigurationDefaultsLoaded(path, clazz);
                }
            }
        } else {
            logger.onFileLoaded(path);
        }
        return config;
    }

    public static void reset() {
        configs.clear();
        init();
        saveAll();
    }

    /**
     * Checks if an alternate file with the same name exists in another folder.
     *
     * @param rootDir     The root dir is the union of the path and override.
     * @param filePath    The file to check if exists in another directory.
     * @param overrideDir The other directory to check in.
     * @return a path to the overridden resource if exists or the filePath itself.
     */
    public static String override(String rootDir, String overrideDir, String filePath) {
        String overrideFile = filePath.replace(rootDir, overrideDir);

        File override = Paths.get(overrideFile).toFile();

        if (override.exists()) {
            return CoreStrings.format(override.toPath());
        } else {
            return filePath;
        }
    }

    /**
     * Reloads a configuration file from specified path.
     *
     * @param path of the configurable to reload.
     */
    @SuppressWarnings("unchecked")
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
        ConfigurationFactory.writeObject(configurable.serialize(), configurable.getPath());
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
                    .map(text -> text.replaceAll("\\\\", CoreStrings.DIR_SEPARATOR))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileReadException(path);
        }
    }

    /**
     * List all available configuration files from the server configuration root.
     *
     * @return a list of paths to configuration files.
     */
    static Collection<String> available() {
        return available("");
    }

    /**
     * Clears all loaded configuration and reloads on next get.
     */
    static void unload() {
        configs.clear();
        init();
        logger.onCacheCleared(ID_CONFIGURATION);
    }

    /**
     * @return system settings stored in the cache.
     */
    public static SystemSettings system() {
        return get(PATH_SYSTEM, SystemSettings.class);
    }

    /**
     * @return security settings from the cache.
     */
    public static SecuritySettings security() {
        return get(PATH_SECURITY, SecuritySettings.class);
    }

    /**
     * @return launcher settings stored in the cache.
     */
    public static LauncherSettings launcher() {
        return get(PATH_LAUNCHER, LauncherSettings.class);
    }

    /**
     * @return storage settings stored in the cache.
     */
    public static StorageSettings storage() {
        return get(PATH_STORAGE, StorageSettings.class);
    }

    /**
     * @param plugin the plugin to get configurations for.
     * @return storage settings for the given plugin
     */
    public static RemoteStorage storage(Class<?> plugin) {
        return storage().getSettingsForPlugin(plugin);
    }

    private static class ConfigurationFileWatcher implements FileStoreListener {
        @Override
        public void onFileModify(Path path) {
            Configurations.reload(path.toString().replaceAll("\\\\", DIR_ROOT));
        }

        @Override
        public void onFileRemove(Path path) {
            Configurations.reload(path.toString().replaceAll("\\\\", DIR_ROOT));
        }
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