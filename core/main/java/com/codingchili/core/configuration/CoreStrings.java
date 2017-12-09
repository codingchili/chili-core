package com.codingchili.core.configuration;

import com.codingchili.core.benchmarking.Benchmark;
import com.codingchili.core.benchmarking.BenchmarkGroup;
import com.codingchili.core.benchmarking.BenchmarkImplementation;
import com.codingchili.core.security.KeyStore;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import io.vertx.core.Verticle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.codingchili.core.files.Configurations.launcher;

/**
 * @author Robin Duda
 * <p>
 * Store json keys and protocol headers so that they are easily maintained.
 * Extend this class in service implementations to add more constants.
 */
public abstract class CoreStrings {
    // Author.
    public static String VERSION;
    public static final String GITHUB = "https://github.com/codingchili";
    public static final String AUTHOR = "Robin Duda \u00a9 2017";

    static {
        VERSION = CoreStrings.class.getPackage().getImplementationVersion();
        if (VERSION == null) {
            // not in jar - no manifest available.
            VERSION = "n/a";
        }
    }

    // paths to configuration files.
    public static final String PATH_LAUNCHER = "conf/system/launcher.yaml";
    public static final String PATH_SECURITY = "conf/system/security.yaml";
    public static final String PATH_SYSTEM = "conf/system/system.yaml";
    public static final String PATH_STORAGE = "conf/system/storage.yaml";
    public static final String PATH_VALIDATOR = "conf/system/validator.json";

    // common directories.
    public static final String DIR_ROOT = "/";
    public static final String DIR_SEPARATOR = "/";
    public static final String DIR_CONFIG = "conf/";
    public static final String DIR_SYSTEM = "conf/system/";
    public static final String DIR_SERVICES = "conf/service/";
    public static final String EMPTY = "";
    public static final String DIR_UP = "../";

    // storage constants.
    public static final String STORAGE_ARRAY = "[]";
    public static final String DB_DIR = "db";
    public static final String WATCHER_COMPLETED = "executed";
    public static final String WATCHER_PAUSED = "paused";
    public static final String WATCHER_RESUMED = "resumed";
    public static final String EXT_JSON = ".json";
    public static final String EXT_YAML = ".yaml";
    public static final String EXT_YML = ".yml";
    public static final String EXT_KRYO = ".kryo";
    public static final String EXT_HTML = ".html";
    public static final String EXT_DB = ".db";
    public static final String EXT_TXT = ".txt";
    public static final String ANY = "ANY";
    public static final String NODE_LOGGING = "syslog.node";
    public static final String LOCALHOST = "localhost";

    // protocol constants.
    public static final String PROTOCOL_REAL_IP = "X-Real-IP";
    public static final String PROTOCOL_CONNECTION = "connection";
    public static final String PROTOCOL_ROUTES = "routes";
    public static final String PROTOCOL_STATUS = "status";
    public static final String PROTOCOL_ROUTE = "route";
    public static final String PROTOCOL_TARGET = "target";
    public static final String PROTOCOL_LOGGING = "logging";
    public static final String PROTOCOL_DOCUMENTATION = "documentation";

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
    public static final String PARAM_HTML = getCommand("html");
    public static final String PARAM_TEMPLATE = getCommand("template");

    // keys used in json objects.
    public static final String ID_TOKEN = "token";
    public static final String ID_NAME = "name";
    public static final String ID_FILE = "file";
    public static final String ID_NODE = "node";
    public static final String ID_DATA = "data";
    public static final String PROTOCOL_MESSAGE = "message";
    public static final String ID_VERSION = "version";
    public static final String ID_BYTES = "bytes";
    public static final String ID_PATH = "path";
    public static final String ID_MODIFIED = "modified";
    public static final String ID_UPDATED = "updated";
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
    public static final String ID_REGISTERED = "registered";
    public static final String ID_STATUS = "status";
    public static final String ID_COUNT = "count";
    public static final String ID_MESSAGE = "message";
    public static final String ID_QUERY = "query";
    public static final String ID_OPTIONS = "options";
    public static final String ID_HANDLER = "handler";
    public static final String ID_UNDEFINED = "undefined";

    // Storage constants
    public static final String DEFAULT_DB = "db";

    // logging constants
    public static final String LOG_APPLICATION = "application";
    public static final String LOG_AT = "@";
    public static final String LOG_EVENT = "event";
    public static final String LOG_SOURCE = "source";
    public static final String LOG_VERSION = "version";
    public static final String LOG_USER_AGENT = "USER-Agent";
    public static final String LOG_AGENT = "agent";
    public static final String LOG_CONTEXT = "context";
    public static final String LOG_TIME = "timestamp";
    public static final String LOG_NODE = "node";
    public static final String LOG_HOST = "host";
    public static final String LOG_SECURITY = "security";
    public static final String LOG_SERVICE_START = "service.start";
    public static final String LOG_SERVICE_STOP = "service.stop";
    public static final String LOG_SERVICE_FAIL = "service.fail";
    public static final String LOG_LISTENER_STOP = "listener.stop";
    public static final String LOG_LISTENER_START = "listener.start";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_LEVEL = "level";
    public static final String LOG_VERTX = "vertx";
    public static final String LOG_METRICS = "metrics";
    public static final String LOG_STACKTRACE = "stacktrace";
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
    public static final String LOG_BENCHMARK = "benchmark";
    public static final String LOG_NODE_TIMEOUT = "timeout";
    public static final String LOG_NODE_UNREACHABLE = "unreachable";
    public static final String LOG_NODE_FAILURE = "failure";
    public static final String LOG_VALUE_EXPIRED = "storage.expired";
    public static final String LOG_VALUE_EXPIRED_MISSING = "storage.failure";
    public static final String LOG_STORAGE_CLEARED = "storage.cleared";
    public static final String LOG_STORAGE_COLLECTION = "collection";
    public static final String LOG_STORAGE_DB = "dabatase";
    public static final String LOG_STORAGE_WATCHER = "storage.watcher";
    public static final String[] LOG_HIDDEN_TAGS = new String[]{"dev", "LOCAL", "3.6.3"};
    public static final String ERROR_TOKEN_FACTORY = "Token factory error when generating token.";
    public static final String ERROR_NOT_CLUSTERED = "Running in non-clustered mode.";
    public static final String ERROR_NOT_AUTHORIZED = "Failed to authorize request.";
    public static final String ERROR_LAUNCHER_STARTUP = "Failed to start the launcher with clustering.";
    public static final String LAUNCHER_SHUTDOWN_STARTED = "system shutdown initiated..";
    public static final String LAUNCHER_SHUTDOWN_COMPLETED = "system has been shut down.";
    public static final String ERROR_VALIDATION_FAILURE = "Provided data did not pass validation.";
    public static final String ERROR_CONFIGURATION_MISMATCH = "configuration mismatches with currently loaded.";
    public static final String ERROR_ALREADY_INITIALIZED = "Error already initialized.";
    public static final String ERROR_STORAGE_EXCEPTION = "Failed to perform a storage operation.";
    public static final String CONFIGURED_BLOCKS = "Configured deployment blocks";
    public static final String ERROR_PATCH_RELOADED = "The patch version changed during patch session.";
    public static final String[] BENCHMARK_CONSOLE_REPORT_COLUMNS =
            {"\n[GROUP]", " [IMPLEMENTATION]", " [BENCHMARK]", " [OP/s]", " [TIME]"};
    private static final String DIR_TEST = "test/resources/";

    // network constants
    private static final String IP6_HOST = "::";
    private static final String IP4_HOST = "0.0.0.0";
    private static final IPVersion ipVersion = IPVersion.IP4;

    public static String cannotDocumentBeforeUse() {
        return "Cannot add documentation before adding a route.";
    }

    public static String cannotSetModelBeforeUse() {
        return "Cannot add model before adding a route.";
    }

    public static String getMissingRole(String role) {
        return String.format("Role '%s' is not configured in the role map.", role);
    }

    public static String getBindAddress() {
        return (ipVersion.equals(IPVersion.IP4) ? IP4_HOST : IP6_HOST);
    }

    public static String getLoopbackAddress() {
        return (ipVersion.equals(IPVersion.IP4) ? "127.0.0.1" : "::1");
    }

    public static String getRequestTooLarge(int maxRequestBytes) {
        return String.format("Maximum request size of %d bytes exceeded.", maxRequestBytes);
    }

    public static String getCommand(String command) {
        return COMMAND_PREFIX + command;
    }

    public static String getHandlerMissing(String name) {
        return String.format("The requested handler '%s' was not found.", name);
    }

    /**
     * Converts a throwables stack trace element into text.
     *
     * @param e the throwable to convert.
     * @return a string of the throwables stack elements.
     */
    public static String throwableToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Replaces tags in a logging message.
     *
     * @param text the source text
     * @param tags the handler of the tag without enclosing brackets.
     * @return the source text with any matching tags removed.
     */
    public static String replaceTags(String text, String[] tags) {
        for (String tag : tags) {
            text = text.replaceAll(" ?(\\[" + tag + "\\]) ?", "");
        }
        return text;
    }

    public static String remove(String source, String toRemove) {
        return source.replace(toRemove, "");
    }

    public static String getDeployFailError(String service) {
        return "Failed to deploy " + service + ", already deployed in cluster";
    }

    public static String getdeployInstanceError(String instance, Throwable throwable) {
        return String.format("Failed to deploy instance '%s', error was '%s'",
                instance, throwable.getMessage());
    }

    public static String getFileReadError(String file) {
        return "Failed to read file '" + file + "'.";
    }

    public static String getFileWriteError(String file) {
        return "Failed to write file '" + file + "'.";
    }

    public static String getDBIdentifier(String DB, String collection, String plugin) {
        return DB + "-" + collection + plugin;
    }

    public static String getFileMissingError(String filename) {
        return "Could not find file " + filename;
    }

    public static String getDBPath(String file) {
        if (!file.endsWith(EXT_DB)) {
            file += EXT_DB;
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

    public static String getUnsupportedDeployment(String node) {
        return String.format("Error: deployment target '%s' is not among the supported types [%s, %s, %s, %s].",
                node, CoreService.class.getSimpleName(), CoreListener.class.getSimpleName(),
                CoreHandler.class.getSimpleName(), Verticle.class.getSimpleName());
    }

    public static String getNodeNotFound(String node) {
        return "Error: Configured node in service block not found '" + node + "'.";
    }

    public static String getGeneratingSecret(String name, String key) {
        return "[Generating] secret '" + name + "' for " + key + " ..";
    }

    public static String getCommandError(String command) {
        return "Failed to execute command '" + command + "', list available with --help";
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

    public static String getStartupText() {
        return String.format("\n%56s\n%42s\n%38s %s %s %s\n",
                "system: Starting " + launcher().getApplication() + " [" + launcher().getVersion() + "] ..",
                launcher().getAuthor(),
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

    public static String getIllegalPathToConfigurable(Class clazz) {
        return String.format("Illegal path to configurable with class '%s', was null.", clazz);
    }

    public static String testDirectory(String name) {
        if (!Paths.get(DIR_TEST + name).toFile().exists()) {
            new ConsoleLogger(CoreStrings.class).log("Test directory not found at path '" +
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

    public static String getStorageLoaderError(Class plugin, String database, String collection) {
        return "Error: Failed to load storage plugin '" + plugin.getSimpleName() + "' for '" + database + DIR_SEPARATOR + collection + "'.";
    }

    public static String getErrorCreateDirectory(String target) {
        return "Error: Failed to create directories for '" + target + "'.";
    }

    public static String getErrorInvalidConfigurable(Class clazz) {
        return "The given class '" + clazz.getSimpleName() + "' is not of configurable type.";
    }

    public static String getFileLoadDefaults(String path, Class<?> clazz) {
        return "'" + path + "' not found: using '" + clazz.getSimpleName() + "'.";
    }

    public static String getNoSuchResource(String name) {
        return "The resource identified by '" + name + "' was not found on the local filesystem " +
                "nor on the classpath.";
    }

    public static String getStorageLoaderMissingArgument(String type) {
        return "storage loader is missing argument for attribute '" + type + "'.";
    }

    public static String getNothingToUpdateException(String key) {
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

    public static String getDescriptionMissing() {
        return "No description provided.";
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
        return "Executes benchmarks. [--iterations ?, --html]";
    }

    public static String getBenchmarkGroupStarted(BenchmarkGroup group) {
        return "Starting group " + group.getName();
    }

    public static String getBenchmarkGroupCompleted(BenchmarkGroup group) {
        return "Completed group " + group.getName();
    }

    public static String getBenchmarkImplementationWarmup(BenchmarkImplementation implementation) {
        return "Warmup started for " + implementation.getName();
    }

    public static String getBenchmarkImplementationWarmupComplete(BenchmarkImplementation implementation) {
        return "Warmup completed for " + implementation.getName();
    }

    public static String getBenchmarkImplementationTestBegin(BenchmarkImplementation implementation) {
        return "Starting tests for " + implementation.getName();
    }

    public static String getBenchmarkImplementationComplete(BenchmarkImplementation implementation) {
        return "Tests completed for " + implementation.getName();
    }

    public static String getBenchmarkProgressUpdate(Benchmark benchmark, String progress) {
        return "Tests for " + benchmark.getImplementation() + "::" + benchmark.getName() +
                " " + progress + "%";
    }

    public static String getBenchmarkCompleted(Benchmark benchmark) {
        return "Completed benchmark " + benchmark.getImplementation() +
                "::" + benchmark.getName() + " in " + benchmark.getElapsedMS() + " ms.";
    }

    public static String getIllegalTemplateTokenCount(String token, int count) {
        return "template must accept " + count + " tokens ('" + token + "s').";
    }

    public static String formatAsPercent(Double value) {
        return new DecimalFormat("#.00").format(value);
    }

    public static List<String> getCommandExecutorText() {
        List<String> list = new ArrayList<>();
        list.add("================================ HELP ================================");
        list.add("\t\t<block-name>\t\t\tdeploys the services configured in the given block.");
        list.add("\t\t<remote-name>\t\t\tdeploys configured blocks on a remote host.");
        return list;
    }

    public static String getMissingEntity(String key) {
        return "Error: could not get '" + key + "' in storage.";
    }

    public static String getMissingKeyStore() {
        return "Keystore not configured: generating self signed certificate.";
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

    public static String getNoSuchValidator(String name) {
        return "Error: no validator exists with the name '" + name + "'.";
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

    public static String getDeserializePayloadException(String message, Class clazz) {
        return "Failed to deserialize invalid payload, into '" + clazz.getSimpleName() + "' " +
                "error was '" + message + "'.";
    }

    public static String getTimeOutEcxeption(String target, int timeout) {
        return "Timed out after waiting for  " + timeout + "(ms) for address: " + target;
    }

    public static String getNodeNotReachable(String target, String route) {
        return "The remote node '" + target + "' not available, requested handler '" + route + "'.";
    }

    public static String getNodeFailedToAcknowledge(String target, String route) {
        return String.format("The remote node '%s' did not acknowledge handler '%s'.",
                target, route);
    }

    public static String getServiceTimeout(String target, String route, int timeout) {
        return String.format("Timed out waiting for '%s' to handle '%s' after %s ms.",
                target, route, timeout);
    }

    public static String getKeystorePrompt(KeyStore store) {
        return "Enter password for '" + store.getPath() + "': ";
    }

    public static String getValueByPathContainsNull(String field, String[] fields) {
        return String.format("Value at '%s' in path '%s' is null.", field, Arrays.stream(fields)
                .collect(Collectors.joining()));
    }

    public static String getReflectionErrorInSerializer(String path) {
        return String.format("Reflection failed to retrieve value at '%s'.", path);
    }

    private enum IPVersion {IP4, IP6;}
}
