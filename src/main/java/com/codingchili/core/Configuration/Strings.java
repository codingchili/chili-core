package com.codingchili.core.Configuration;

import java.nio.file.Path;

/**
 * @author Robin Duda
 *
 * Store json keys and protocol headers so that they are easily maintained.
 * Extend this class in service implementations to add more constants.
 */
public class Strings {
    // paths to configuration files.
    public static final String PATH_VALIDATOR = "conf/system/validator.json";
    public static final String PATH_LAUNCHER = "conf/system/launcher.json";
    public static final String PATH_SECURITY = "conf/system/security.json";
    public static final String PATH_SYSTEM = "conf/system/system.json";

    // common directories.
    public static final String DIR_ROOT = "/";
    public static final String DIR_SEPARATOR = "/";
    public static final String DIR_CONFIG = "conf/";
    public static final String DIR_SYSTEM = "conf/system/";
    public static final String DIR_SERVICES = "conf/service/";
    private static final String DIR_TEST = "src/test/resources/";

    public static final String EXT_JSON = ".json";
    public static final String ANY = "*";

    // protocol constants.
    public static final String PROTOCOL_REAL_IP = "X-Real-IP";
    public static final String PROTOCOL_CONNECTION = "connection";
    public static final String PROTOCOL_STATUS = "status";
    public static final String PROTOCOL_ACTION = "action";
    public static final String PROTOCOL_LOGGING = "logging";

    // launcher commands.
    public static final String GENERATE_SECRETS = "--generate-secrets";
    public static final String GENERATE_TOKENS = "--generate-tokens";
    public static final String GENERATE_PRESHARED = "--generate-preshared";
    public static final String GENERATE = "--generate";
    public static final String HELP = "--help";

    // keys used in json objects.
    public static final String ID_TOKEN = "token";
    public static final String ID_NAME = "name";
    public static final String ID_FILE = "file";
    public static final String ID_DATA = "data";
    public static final String ID_MESSAGE = "message";
    public static final String ID_ACTION = "action";
    public static final String ID_TARGET = "target";
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

    // logging constants
    public static final String LOG_AT = "@";
    public static final String LOG_EVENT = "event";
    public static final String LOG_VERSION = "version";
    public static final String LOG_USER_AGENT = "User-Agent";
    public static final String LOG_AGENT = "agent";
    public static final String LOG_REMOTE = "remote";
    public static final String LOG_TIME = "time";
    public static final String LOG_NODE = "node";
    public static final String LOG_HOST = "host";
    public static final String LOG_SERVER_START = "server.start";
    public static final String LOG_SERVER_STOP = "server.stop";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_FILE_ERROR = "file.error";
    public static final String LOG_LEVEL = "level";
    public static final String LOG_VERTX = "vertx";
    public static final String LOG_METRICS = "metrics";
    public static final String LOG_TRACE = "trace";
    public static final String LOG_HANDLER_MISSING = "handler.missing";
    public static final String LOG_FILE_LOADED = "file.load";
    public static final String LOG_FILE_SAVED = "file.save";
    public static final String LOG_CACHE_CLEARED = "cache.clear";
    public static final String LOG_ERROR = "error";
    public static final String LOG_TIMER_CHANGE = "timer.changed";
    public static final String LOG_PREVIOUS = "previous";
    public static final String LOG_NEW = "new";
    public static final String[] LOG_HIDDEN_TAGS = new String[]{"dev", "LOCAL", "3.6.3"};

    public static final String ERROR_TOKEN_FACTORY = "Token factory failed to generate token.";
    public static final String ERROR_CLUSTERING_REQUIRED = "Clustering required but not enabled.";
    public static final String ERROR_NOT_AUTHORIZED = "Insufficient authorization level to access resource.";
    public static final String ERROR_HANDLER_MISSING = "The requested handler was not found.";
    public static final String ERROR_LAUNCHER_STARTUP = "Failed to start the launcher with clustering.";
    public static final String ERRROR_LAUNCHER_SHUTDOWN = "System has been shut down..";
    public static final String ERROR_PROTOCOL_ATTRIBUTE_MISSING = "A required field was missing in request.";
    public static final String ERROR_VALIDATION_FAILURE = "Provided data did not pass validation.";
    public static final String ERROR_REQUEST_SIZE_TOO_LARGE = "Maximum request size exceeded.";
    public static final String ERROR_CONFIGURATION_MISMATCH = "Configuration mismatches with currently loaded.";
    public static final String ERROR_ALREADY_INITIALIZED = "Error already initialized.";
    public static final String CONFIGURED_BLOCKS = "Configured deployment blocks";

    /**
     * Replaces tags in a logging message.
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

    public static String getFileMissingError(String filename) {
        return "Could not find file " + filename;
    }

    /**
     * Formats a path object using a base root.
     * @param path the path to be formatted
     * @param root the relative root to remove from the path
     * @return a relative path string that is the same on all filesystems.
     */
    public static String format(Path path, String root) {
        return (path.toString()
                .replace("\\", Strings.DIR_SEPARATOR)
                .replaceFirst(root, ""));
    }

    public static String getRemoteBlockNotConfigured(String remote, String block) {
        return "Error: host block '" + remote + "' or launcher block '" + block + "' not found in " + PATH_LAUNCHER;
    }

    public static String getBlockNotConfigured(String block) {
        return "Error: Service block '" + block + "' missing in " + PATH_LAUNCHER;
    }

    public static String getNodeNotVerticle(String node) {
        return "Error: Configured node is not of acceptable type '" + node + "', must extend 'ClusterNode'.";
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
        return Strings.DIR_SERVICES + config + EXT_JSON;
    }

    public static String format(Path path) {
        return format(path, "");
    }

    public static String getStartupText(String version) {
        return "\n\t\t\t\tSystem: Starting launcher [" + version + "] ..\n" +
                "\t\t\t\t\t    Robin Duda &copy2016;\n";
    }

    public static String quote(String action) {
        return "'" + action + "'";
    }

    public static String testDirectory(String name) {
        return DIR_TEST + name + DIR_SEPARATOR;
    }

    public static String testFile(String directory, String name) {
        return testDirectory(directory) + name;
    }

    public static String getSystemNotInitialized(String name) {
        return "System '" + name + "' has not been initialized!";
    }
}
