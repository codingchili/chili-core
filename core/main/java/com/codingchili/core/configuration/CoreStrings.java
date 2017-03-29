package com.codingchili.core.configuration;

import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

/**
 * @author Robin Duda
 *         <p>
 *         Store json keys and protocol headers so that they are easily maintained.
 *         Extend this class in service implementations to add more constants.
 */
public abstract class CoreStrings {
    // paths to configuration files.
    public static final String PATH_VALIDATOR = "conf/system/validator.json";
    public static final String PATH_LAUNCHER = "conf/system/launcher.json";
    public static final String PATH_SECURITY = "conf/system/security.json";
    public static final String PATH_SYSTEM = "conf/system/system.json";
    public static final String PATH_STORAGE = "conf/system/storage.json";

    // common directories.
    public static final String DIR_ROOT = "/";
    public static final String DIR_SEPARATOR = "/";
    public static final String DIR_CONFIG = "conf/";
    public static final String DIR_SYSTEM = "conf/system/";
    private static final String DIR_TEST = "test/resources/";
    public static final String DIR_SERVICES = "conf/service/";
    public static final String EMPTY = "";
    public static final String DIR_UP = "../";

    // network constants
    private static final String IP6_HOST = "::";
    private static final String IP4_HOST = "0.0.0.0";
    private static final IPVersion ipVersion = IPVersion.IP4;

    private enum IPVersion {IP4, IP6}

    public static String getBindAddress() {
        return (ipVersion.equals(IPVersion.IP4) ? IP4_HOST : IP6_HOST);
    }

    public static String getLoopbackAddress() {
        return (ipVersion.equals(IPVersion.IP4) ? "127.0.0.1" : "::1");
    }

    // storage constants.
    public static final String STORAGE_ARRAY = "[]";
    public static final String DB_DIR = "db";
    public static final String WATCHER_COMPLETED = "executed";
    public static final String WATCHER_PAUSED = "paused";
    public static final String WATCHER_RESUMED = "resumed";

    public static final String EXT_JSON = ".json";
    public static final String EXT_HTML = ".html";
    public static final String EXT_TXT = ".txt";
    public static final String ANY = "*";
    public static final String NODE_LOGGING = "syslog.node";

    // protocol constants.
    public static final String PROTOCOL_REAL_IP = "X-Real-IP";
    public static final String PROTOCOL_CONNECTION = "connection";
    public static final String PROTOCOL_STATUS = "status";
    public static final String PROTOCOL_ROUTE = "route";
    public static final String PROTOCOL_TARGET = "target";
    public static final String PROTOCOL_LOGGING = "logging";

    // launcher commands.
    public static final String COMMAND_PREFIX = "--";
    public static final String GENERATE_SECRETS = getCommand("generate-secrets");
    public static final String GENERATE_TOKENS = getCommand("generate-tokens");
    public static final String GENERATE_PRESHARED = getCommand("generate-preshared");
    public static final String GENERATE = getCommand("generate");
    public static final String RECONFIGURE = getCommand("reconfigure");
    public static final String HELP = getCommand("help");
    public static final String BENCHMARK = getCommand("benchmark");
    public static final String PARAM_ITERATIONS = getCommand("iterations");

    public static String getCommand(String command) {
        return COMMAND_PREFIX + command;
    }

    // keys used in json objects.
    public static final String ID_TOKEN = "token";
    public static final String ID_NAME = "name";
    public static final String ID_FILE = "file";
    public static final String ID_DATA = "data";
    public static final String PROTOCOL_MESSAGE = "message";
    public static final String ID_VERSION = "version";
    public static final String ID_BYTES = "bytes";
    public static final String ID_PATH = "path";
    public static final String ID_MODIFIED = "modified";
    public static final String ID_SIZE = "size";
    public static final String ID_DOMAIN = "domain";
    public static final String ID_PING = "ping";
    public static final String ID_BUFFER = "buffer";
    public static final String ID_TYPE = "type";
    public static final String ID_REMOTE = "remote";
    public static final String ID_ATTRIBUTES = "attributes";
    public static final String ID_CONFIGURATION = "configuration";
    public static final String ID_RANGE = "Range";
    public static final String ID_DEFAULT = "default";
    public static final String ID_IDENTITY = "identity";
    public static final String ID_SYSTEM = "system";
    public static final String ID_TIME = "time";
    public static final String ID_KEY = "key";
    public static final String ID_PLUGIN = "plugin";
    public static final String ID_DB = "DB";
    public static final String ID_COLLECTION = "collection";
    public static final String ID_CLASS = "class";
    public static final String ID_CONTEXT = "context";
    public static final String ID_USERNAME = "username";
    public static final String ID_PASSWORD = "password";
    public static final String ID_STATUS = "status";
    public static final String ID_COUNT = "count";
    public static final String ID_MESSAGE = "message";
    public static final String ID_QUERY = "query";
    public static final String ID_OPTIONS = "options";

    // Node names.
    public static final String NODE_LOCAL = "local";

    // Storage constants
    public static final String DEFAULT_DB = "chili";

    // logging constants
    public static final String LOG_APPLICATION = "application";
    public static final String LOG_AT = "@";
    public static final String LOG_EVENT = "event";
    public static final String LOG_VERSION = "version";
    public static final String LOG_USER_AGENT = "User-Agent";
    public static final String LOG_AGENT = "agent";
    public static final String LOG_TIME = "time";
    public static final String LOG_NODE = "node";
    public static final String LOG_HOST = "host";
    public static final String LOG_SECURITY = "security";
    public static final String LOG_SERVER_START = "server.start";
    public static final String LOG_SERVER_STOP = "server.stop";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_LEVEL = "level";
    public static final String LOG_VERTX = "vertx";
    public static final String LOG_METRICS = "metrics";
    public static final String LOG_TRACE = "trace";
    public static final String LOG_HANDLER_MISSING = "handler.missing";
    public static final String LOG_FILE_LOADED = "file.load";
    public static final String LOG_FILE_SAVED = "file.save";
    public static final String LOG_FILE_ERROR = "file.error";
    public static final String LOG_CONFIG_DEFAULTED = "config.defaults";
    public static final String LOG_CONFIGURATION_INVALID = "config.error";
    public static final String LOG_CACHE_CLEARED = "cache.clear";
    public static final String LOG_ERROR = "error";
    public static final String LOG_TIMER_CHANGE = "timer.changed";
    public static final String LOG_PREVIOUS = "previous";
    public static final String LOG_NEW = "new";
    public static final String LOG_VALUE_EXPIRED = "storage.expired";
    public static final String LOG_VALUE_EXPIRED_MISSING = "storage.failure";
    public static final String LOG_STORAGE_CLEARED = "storage.cleared";
    public static final String LOG_STORAGE_COLLECTION = "collection";
    public static final String LOG_STORAGE_DB = "dabatase";
    public static final String LOG_STORAGE_WATCHER = "storage.watcher";
    public static final String[] LOG_HIDDEN_TAGS = new String[]{"dev", "LOCAL", "3.6.3"};

    public static final String ERROR_REUSABLEQUERY_UNBOUND = "Reusable query not bound to a storage.";
    public static final String ERROR_TOKEN_FACTORY = "Token factory error to generate token.";
    public static final String ERROR_CLUSTERING_REQUIRED = "Running in non-clustered mode.";
    public static final String ERROR_NOT_AUTHORIZED = "Insufficient authorization level to access resource.";
    public static final String ERROR_HANDLER_MISSING = "The requested handler was not found.";
    public static final String ERROR_LAUNCHER_STARTUP = "Failed to start the launcher with clustering.";
    public static final String ERRROR_LAUNCHER_SHUTDOWN = "system has been shut down..";
    public static final String ERROR_VALIDATION_FAILURE = "Provided data did not pass validation.";
    public static final String ERROR_REQUEST_SIZE_TOO_LARGE = "Maximum request size exceeded.";
    public static final String ERROR_CONFIGURATION_MISMATCH = "configuration mismatches with currently loaded.";
    public static final String ERROR_ALREADY_INITIALIZED = "Error already initialized.";
    public static final String ERROR_STORAGE_EXCEPTION = "Failed to perform a storage operation.";
    public static final String CONFIGURED_BLOCKS = "Configured deployment blocks";
    public static final String ERROR_PATCH_RELOADED = "The patch version changed during patch session.";

    /**
     * Replaces tags in a logging message.
     *
     * @param text the source text
     * @param tags the name of the tag without enclosing brackets.
     * @return the source text with any matching tags removed.
     */
    public static String replaceTags(String text, String[] tags) {
        for (String tag : tags) {
            text = text.replaceAll(" ?(\\[" + tag + "\\]) ?", "");
        }
        return text;
    }

    public static String getDeployFailError(String service) {
        return "Failed to deploy " + service + ", already deployed in cluster";
    }

    public static String getFileReadError(String file) {
        return "Failed to read file " + file + ".";
    }

    public static String getDBIdentifier(String DB, String collection) {
        return DB + "-" + collection;
    }

    public static String getFileMissingError(String filename) {
        return "Could not find file " + filename;
    }

    public static String getDBPath(String file) {
        if (!file.endsWith(EXT_JSON)) {
            file += EXT_JSON;
        }
        return DB_DIR + DIR_SEPARATOR + file;
    }

    /**
     * Formats a path object using a base root.
     *
     * @param path the path to be formatted
     * @param root the relative root to remove from the path
     * @return a relative path string that is the same on all filesystems.
     */
    public static String format(Path path, String root) {
        String format = (path.toString()
                .replaceAll("\\\\", DIR_SEPARATOR))
                .replace(root.replaceAll("\\\\", DIR_SEPARATOR), "");

        if (format.startsWith(DIR_SEPARATOR)) {
            return format.replaceFirst(DIR_SEPARATOR, "");
        } else {
            return format;
        }
    }

    public static String getRemoteBlockNotConfigured(String remote, String block) {
        return "Error: host block '" + remote + "' or launcher block '" + block + "' not found in " + PATH_LAUNCHER;
    }

    public static String getBlockNotConfigured(String block) {
        return "Error: Service block '" + block + "' missing in " + PATH_LAUNCHER;
    }

    public static String getNoServicesConfiguredForBlock(String block) {
        return "Error: no services are configured for block '" + block + "'.";
    }

    public static String getNodeNotVerticle(String node) {
        return "Error: Service '" + node + "' must extend 'ClusterNode'.";
    }

    public static String getNodeNotFound(String node) {
        return "Error: Configured node in service block not found '" + node + "'.";
    }

    public static String getGeneratingSecret(String name, String key) {
        return "[Generating] secret '" + name + "' for " + key + " ..";
    }

    public static String getNoSuchCommand(String command) {
        return "Failed to execute '" + command + "' no such command, list available with --help";
    }

    public static String getGeneratingToken(String owner, String secret, String service) {
        return "[Generating] token '" + owner + "/" + secret + "' for " + service + " ..";
    }

    public static String getGeneratingShared(String key, String path) {
        return "[Generating] preshared key '" + key + "' for " + path + " ..";
    }

    public static String getService(String config) {
        return CoreStrings.DIR_SERVICES + config + EXT_JSON;
    }

    public static String format(Path path) {
        return format(path, "");
    }

    public static String getStartupText(String version) {
        return String.format("\n\t\t\t\t\t\t\t\tsystem: Starting launcher [" + version + "] ..\n" +
                        "\t\t\t\t\t\t\t\t\t\t     Robin Duda © 2017\n\n\t\t\t" +
                        "     %s %s on system %s %s\n",
                System.getProperty("java.vm.name"),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.version"));
    }

    public static String quote(String string) {
        return "'" + string + "'";
    }

    public static String testDirectory() {
        return testDirectory("");
    }

    public static String testDirectory(String name) {
        if (!Paths.get(DIR_TEST + name).toFile().exists()) {
            new ConsoleLogger().log("Test directory not found at path '" +
                    Paths.get(DIR_TEST + name).toAbsolutePath() + "'", Level.WARNING);
        }
        return DIR_TEST + name;

    }

    public static String testFile(String name) {
        return testDirectory() + DIR_SEPARATOR + name;
    }

    public static String testFile(String directory, String name) {
        return testDirectory(directory) + DIR_SEPARATOR + name;
    }

    public static String getSystemNotInitialized(String name) {
        return "Subsystem '" + name + "' has not been initialized!";
    }

    public static String getIdentityNotConfigured(String name) {
        return "[" + name + "] Error: Identity must be configured.";
    }

    public static String getStorageLoaderError(String plugin, String mapName, String collection) {
        return "Error: Failed to load storage plugin '" + plugin + "' for db '" + mapName + "." + collection + "'.";
    }

    public static String getErrorCreateDirectory(String target) {
        return "Error: Failed to create directories for '" + target + "'.";
    }

    public static String getErrorInvalidConfigurable(Class clazz) {
        return "The given class '" + clazz.getSimpleName() + "' is not of configurable type.";
    }

    public static String getFileLoadDefaults(String path, Class<?> clazz) {
        return "configuration '" + path + "' not found, using defaults from '" + clazz.getSimpleName() + "'.";
    }

    public static String getNoSuchResource(String name) {
        return "The resource identified by '" + name + "' was not found on the local filesystem " +
                "nor on the classpath.";
    }

    public static String getStorageLoaderMissingArgument(String type) {
        return "storage loader is missing argument for attribute '" + type + "'.";
    }

    public static String getNothingToReplaceException(String key) {
        return "Error: nothing to replace for given key '" + key + "'.";
    }

    public static String getNothingToRemoveException(String id) {
        return "Error: nothing to remove for given key '" + id + "'.";
    }

    public static String getValueAlreadyPresent(String key) {
        return "Error: could not put value, key '" + key + "' already present.";
    }

    public static String getCommandAlreadyExistsException(String name) {
        return "Error: the command " + name + " is already registered";
    }

    public static String getReconfigureDescription() {
        return "resets system configuration files.";
    }

    public static String getGeneratePresharedDescription() {
        return "generates pre-shared keys for authentication.";
    }

    public static String getGenerateSecretsDescription() {
        return "generates authentication secrets.";
    }

    public static String getGenerateTokensDescription() {
        return "generates tokens from existing secrets.";
    }

    public static String getGenerateAllDescription() {
        return "generates secrets, tokens and preshared keys..";
    }

    public static String getCommandExecutorHelpDescription() {
        return "prints this help text.";
    }

    public static String getRemotesAvailable() {
        return "remotes available";
    }

    public static String getBenchmarkDescription() {
        return "Executes all registered benchmarks. [--iterations]";
    }

    public static List<String> getCommandExecutorText() {
        List<String> list = new ArrayList<>();
        list.add("=================================================== HELP ====================================================");
        list.add("\t\t<block-name>\t\t\tdeploys the services configured in the given block.");
        list.add("\t\t<remote-name>\t\t\tdeploys configured blocks on a remote host.");
        return list;
    }

    public static String getMissingEntity(String key) {
        return "Error: could not get '" + key + "' in storage.";
    }

    public static String getSecurityDependencyMissing(String target, String identifier) {
        return "Error: missing security identifier '" + identifier +
                "' in service configuration for '" + target + "'.";
    }

    public static String timestamp(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.UTC).toString().split("T")[1];
    }

    public static String getHashMismatchException() {
        return "Error: hash comparison has failed.";
    }

    public static String getWatcherFailed(String cause) {
        return "failed: " + cause;
    }

    public static String getInvalidQueryFormat(String query) {
        return "Unable to parse query: " + query;
    }

    public static String getSemaphoreTimeout(int timeoutMS) {
        return "Error: semaphore timed out after waiting for " + timeoutMS + " ms.";
    }

    public static String getFileFriendlyDate() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss"));
    }

    public static String getAbsolutePath(String relative) {
        return FileSystems.getDefault().getPath(
                currentPath() + CoreStrings.DIR_SEPARATOR + relative).toString();
    }

    public static String currentPath() {
        return Paths.get("").toAbsolutePath().normalize().toString();
    }

    public static String getDeserializePayloadException() {
        return "Failed to deserialize invalid payload.";
    }
}
