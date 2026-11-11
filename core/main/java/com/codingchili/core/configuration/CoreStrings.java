package com.codingchili.core.configuration;

import io.vertx.core.Verticle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.codingchili.core.benchmarking.*;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.KeyStoreReference;

import static com.codingchili.core.files.Configurations.launcher;

/**
 * Store json keys and protocol headers so that they are easily maintained.
 * Extend this class in service implementations to add more constants.
 */
public abstract class CoreStrings {
    // Author.
    public static String VERSION;
    public static final String GITHUB = "https://github.com/codingchili";
    public static final String AUTHOR = "Robin Duda (c) 2022";

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

    // overrides of the cluster settings in launcher.yaml (ClusterSettings): a system property, then an environment variable.
    public static final String PROPERTY_CLUSTER_ENABLED = "chili.cluster.enabled";
    public static final String PROPERTY_CLUSTER_NAME = "chili.cluster.name";
    public static final String PROPERTY_CLUSTER_MULTICAST = "chili.cluster.multicast";
    public static final String PROPERTY_CLUSTER_CLOUD = "chili.cluster.cloud";
    public static final String PROPERTY_CLUSTER_MEMBERS = "chili.cluster.members";
    public static final String PROPERTY_CLUSTER_HOST = "chili.cluster.host";
    public static final String PROPERTY_CLUSTER_BIND = "chili.cluster.bind";
    public static final String PROPERTY_CLUSTER_PORT = "chili.cluster.port";
    public static final String ENV_CLUSTER_ENABLED = "CHILI_CLUSTER_ENABLED";
    public static final String ENV_CLUSTER_NAME = "CHILI_CLUSTER_NAME";
    public static final String ENV_CLUSTER_MULTICAST = "CHILI_CLUSTER_MULTICAST";
    public static final String ENV_CLUSTER_CLOUD = "CHILI_CLUSTER_CLOUD";
    public static final String ENV_CLUSTER_MEMBERS = "CHILI_CLUSTER_MEMBERS";
    public static final String ENV_CLUSTER_HOST = "CHILI_CLUSTER_HOST";
    public static final String ENV_CLUSTER_BIND = "CHILI_CLUSTER_BIND";
    public static final String ENV_CLUSTER_PORT = "CHILI_CLUSTER_PORT";

    // generate --rotate: new values for every secret, preshared key, key pair and token (revokes the old ones).
    public static final String GENERATE_ROTATE = "--rotate";
    // generate --store <dir>: the generated values are read from and written to a secrets store, not the configuration.
    public static final String GENERATE_STORE = "--store";
    // generate --encrypt: the generated values are written encrypted (enc:) to the nodes that read each file.
    public static final String GENERATE_ENCRYPT = "--encrypt";
    // generate --encrypt --nodes <dir>: the public keys of the nodes, <host>.pub (default <store>/nodes).
    public static final String GENERATE_NODES = "--nodes";
    // the folders and files of a secrets store: the public keys of the nodes, the encrypted values, the nodes they
    // were encrypted to.
    public static final String DIR_NODES = "nodes";
    public static final String DIR_ENCRYPTED = "encrypted";
    public static final String NODES_RECORD = "nodes.txt";
    // keygen [--force]: writes the key of this node.
    public static final String KEYGEN = "keygen";
    public static final String KEYGEN_FORCE = "--force";
    // bake [--store <dir>] [--out <dir>] [--blocks a,b]: the configuration files with the secrets of a store.
    public static final String BAKE = "bake";
    public static final String BAKE_OUT = "--out";
    public static final String BAKE_BLOCKS = "--blocks";
    public static final String BAKE_OUT_DEFAULT = "baked";
    // the key file of this node, which opens the encrypted configuration values (enc:).
    public static final String PROPERTY_NODE_KEY = "chili.node.key";
    public static final String ENV_NODE_KEY = "CHILI_NODE_KEY";
    public static final String PATH_NODE_KEY = "conf/system/node.key";
    // a key pair of the security dependencies is written as <name>PrivateKey and <name>PublicKey.
    public static final String PRIVATE_KEY_SUFFIX = "PrivateKey";
    public static final String PUBLIC_KEY_SUFFIX = "PublicKey";
    // written by an earlier 1.4.9 build of the generator (rollover), removed by a rotate.
    public static final String SECRET_PREVIOUS_SUFFIX = "Previous";
    public static final String SECRETS_ROTATED = "secretsRotated";

    // common directories.
    public static final String DIR_ROOT = "/";
    public static final String DIR_CURRENT = "./";
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
    public static final String DEFAULT_KEYSTORE = "keystore.jks";
    public static final String DEFAULT_KEYSTORE_AUTO_ALIAS = "keystore-self-signed";
    public static final String DEFAULT_QUIC_PROTOCOL = "default";
    public static final String HTTP3_ALT_SVC = "h3=\":%d\"; ma=86400";

    // launcher commands.
    public static final String COMMAND_PREFIX = "--";
    public static final String GENERATE_SECRETS = "generate-secrets";
    public static final String GENERATE_TOKENS = "generate-tokens";
    public static final String GENERATE_PRESHARED = "generate-preshared";
    public static final String GENERATE = "generate";
    public static final String RECONFIGURE = "reconfigure";
    public static final String HELP = "help";
    public static final String BENCHMARK = "benchmark";
    public static final String PARAM_ITERATIONS = getParam("iterations");
    public static final String PARAM_HTML = getParam("html");
    public static final String PARAM_TEMPLATE = getParam("template");
    public static final String PARAM_SUITE = getParam("suite");
    public static final String SUITE_MAPS = "maps";
    public static final String SUITE_PROTOCOL = "protocol";
    public static final String SUITE_ALL = "all";
    public static final String DEPLOY = "deploy";

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
    public static final String ID_SERVICE = "service";
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
    public static final String ID_STATISTICS = "statistics";

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
    public static final String LOG_LISTENER = "listener";
    public static final String LOG_SERVICE = "service";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_LEVEL = "level";
    public static final String LOG_VERTX = "vertx";
    public static final String LOG_SLF4J = "slf4j";
    public static final String LOG_METRICS = "metrics";
    public static final String LOG_STACKTRACE = "stacktrace";
    public static final String LOG_HANDLER_MISSING = "handler.missing";
    public static final String LOG_FILE_LOADED = "file.load";
    public static final String LOG_FILE_SAVED = "file.save";
    public static final String LOG_FILE_ERROR = "file.error";
    public static final String LOG_CONFIG_DEFAULTED = "config.defaults";
    public static final String LOG_CONFIGURATION_INVALID = "config.error";
    public static final String LOG_CACHE_CLEARED = "cache.clear";
    public static final String LOG_RELOAD_DISABLED = "reload.disabled";
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
    public static final String LAUNCHER_SHUTDOWN_STARTED = "context shutdown initiated..";
    public static final String LAUNCHER_SHUTDOWN_COMPLETED = "context has been shut down.";

    public static String getShutdownInterrupted(int tasks, long timeoutMS) {
        return String.format("%d blocking task(s) did not complete within %d ms and are interrupted.", tasks, timeoutMS);
    }

    public static String getShutdownTimedOut(long timeoutMS) {
        return String.format("context shutdown did not complete within %d ms, exiting.", timeoutMS);
    }
    public static final String ERROR_VALIDATION_FAILURE = "Provided data did not pass validation.";
    public static final String ERROR_CONFIGURATION_MISMATCH = "configuration mismatches with currently loaded.";
    public static final String ERROR_ALREADY_INITIALIZED = "Error already initialized.";
    public static final String ERROR_STORAGE_EXCEPTION = "Failed to perform a storage operation.";
    public static final String CONFIGURED_BLOCKS = "Configured deployment blocks and remotes available";
    public static final String ERROR_PATCH_RELOADED = "The patch version changed during patch session.";
    public static final String[] BENCHMARK_CONSOLE_REPORT_COLUMNS =
            {"[GROUP]", "[IMPLEMENTATION]", "[BENCHMARK]", "[OP/s]", "[TIME]"};
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

    public static String getInvalidListenerConfig(String type, String reason) {
        return String.format("Invalid listener configuration, expected the properties of %s: %s", type, reason);
    }

    public static String getHttp3RequiresTls() {
        return "HTTP/3 requires TLS: set secure: true on the listener, or remove HTTP_3 from the versions.";
    }

    public static String getRequestMalformed(String reason) {
        return String.format("Malformed request: %s", reason);
    }

    public static String getQuicRequiresSecure(String listener) {
        return String.format("QUIC requires TLS, %s must be configured with secure: true and a keystore.", listener);
    }

    public static String getParam(String command) {
        return COMMAND_PREFIX + command;
    }

    public static String getHandlerMissing() {
        return "Requested handler not found.";
    }

    public static String getHandlerMissing(String handlerName) {
        return String.format("Requested handler '%s' not found.", handlerName);
    }

    public static String getMessageTypeMismatch(String route, String expected, String actual) {
        return String.format("Route '%s' expects a message of type %s but received %s.", route, expected, actual);
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
        return sw.toString().trim();
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

    public static String getHostNotConfigured(String hostname, java.util.Collection<String> hosts,
                                              java.util.Collection<String> blocks) {
        return "Error: no block for host '" + hostname + "' in " + PATH_LAUNCHER + ": the hostname is not a block " +
                new java.util.TreeSet<>(blocks) + " and neither it nor an address of this host is in hosts " +
                new java.util.TreeSet<>(hosts) + ". Name the block: deploy <block>.";
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

    public static String getUnableToDetermineKeystoreType(String path) {
        return String.format("Unable to determine keystore type for keystore at '%s', make sure" +
                "the filename ends with a known file extension. (.p12/.jks)", path);
    }

    public static String getNodeNotFound(String node) {
        return "Error: Configured node in service block not found '" + node + "'.";
    }

    public static String getGeneratingSecret(String name, String key) {
        return "[Generated] secret '" + name + "' for " + key + " ..";
    }

    public static String getCommandError(String command) {
        return "Failed to execute command '" + command + "', list available with '" + HELP + "'";
    }

    public static String generatedToken(String owner, String secret, String service) {
        return "[Generated] token '" + owner + "/" + secret + "' for " + service + " ..";
    }

    public static String getKeptSecret(String key, String path) {
        return "[Kept] '" + key + "' in " + path + " ..";
    }

    public static String getRotatedSecret(String key, String path) {
        return "[Rotated] '" + key + "' in " + path + ": replaced, the old value is not kept ..";
    }

    public static String getRemovedPrevious(String key, String path) {
        return "[Removed] '" + key + "' in " + path + " (a previous value of an earlier generator) ..";
    }

    public static String getGeneratingKeyPair(String name, String path, boolean signer) {
        return "[Generated] key pair '" + name + "' for " + path + ((signer) ? " (private and public key)" :
                " (public key)") + " ..";
    }

    public static String getKeyPairWithoutSigner(String name, Collection<String> paths) {
        return "[Warning] key pair '" + name + "' has no file with its private key (keypairs), the public key of " +
                paths + " is kept ..";
    }

    public static String getNoRecipients(String path, boolean store) {
        return "[Warning] no node runs a block that reads " + path + ((store) ?
                ": it has no encrypted values, a bake leaves it without secrets .." :
                ": its values are not encrypted ..");
    }

    public static String getEncryptedTo(String path, Collection<String> hosts) {
        return "[Encrypted] " + path + " to " + hosts + " ..";
    }

    public static String getEncryptionRemoved(String directory) {
        return "[Removed] the encrypted values of " + directory + ": generate without " + GENERATE_ENCRYPT +
                ", a bake uses the plain values ..";
    }

    public static String getStoreSaved(int files, String store, boolean encrypted) {
        return "[Saved] the secrets of " + files + " configuration files to " + store +
                ((encrypted) ? " (plain) and " + DIR_ENCRYPTED + "/ (encrypted)" : "") + " ..";
    }

    public static String getStoreEncrypted(String file, String key) {
        return file + ": '" + key + "' is encrypted (enc:), a secrets store holds the plain values (generate " +
                GENERATE_STORE + " writes encrypted values to " + DIR_ENCRYPTED + "/ only).";
    }

    public static String getNoNodeKeys(String directory) {
        return "no node public keys in " + directory + ": run '" + KEYGEN + "' on each node and save the public key " +
                "it prints as " + directory + "/<host>.pub.";
    }

    public static String getNodeWithoutBlock(String host, String file, Collection<String> blocks) {
        return "the node '" + host + "' (" + file + ") runs no block: name the file as a block " + blocks +
                " or map the host to its block in the launcher's hosts.";
    }

    public static String getNodeKeyUnreadable(String path, String reason) {
        return "can't read the node key " + path + ": " + reason;
    }

    public static String getNodeKeyExists(String path) {
        return "the node key " + path + " exists: '" + KEYGEN + " " + KEYGEN_FORCE + "' replaces it (the values " +
                "encrypted to the old key can't be opened with the new one: generate " + GENERATE_ENCRYPT + " again).";
    }

    public static String getNodeKeyWritten(String path) {
        return "[Written] the node key " + path + " (private: keep it on this node). Its public key, for the " +
                "build machine's store (nodes/<host>.pub):";
    }

    public static String getNodePublicKeyInvalid(String text) {
        return "not a node public key (x25519:<base64>): '" + text + "'";
    }

    public static String getEncryptedValueFormat() {
        return "not an encrypted value (enc:v1:<base64>)";
    }

    public static String getEncryptedValueVersion(String value) {
        int end = value.indexOf(':', 4);
        return "an encrypted value of an unsupported format version '" +
                ((end > 0) ? value.substring(4, end) : value.substring(0, Math.min(value.length(), 8))) +
                "' (this build reads v1)";
    }

    public static String getEncryptedValueNotRecipient(String fingerprint, int recipients) {
        return "this node's key can't open it: the key " + fingerprint + " is not one of its " + recipients +
                " recipients (generate " + GENERATE_ENCRYPT + " encrypts a file to the nodes that run its services)";
    }

    public static String getEncryptedValueNoKey(String path) {
        return "this node's key can't open it: there is no node key (" + path + ", or " + ENV_NODE_KEY + " / -D" +
                PROPERTY_NODE_KEY + " naming the key file)";
    }

    public static String getEncryptedValueTampered() {
        return "the value was changed after it was encrypted, or it is corrupt";
    }

    public static String getEncryptedConfigurationError(String file, String key, String reason) {
        return file + ": can't open the encrypted value of '" + key + "': " + reason + ".";
    }

    public static String getEncryptedConfigurationNotSaved(String file) {
        return "[Skipped] saving " + file + ": it was loaded with encrypted values, its decrypted values are never " +
                "written ..";
    }

    public static String getBaked(int files, String out, String mode) {
        return "[Baked] " + files + " configuration files (" + mode + ") to " + out + " ..";
    }

    public static String getBakedBlock(String block, Collection<String> files) {
        return "[Baked] block '" + block + "': " + files + " ..";
    }

    public static String getBakeWithoutSecrets(String path) {
        return "[Warning] " + path + " is baked without secrets: no node runs a block that reads it ..";
    }

    public static String getBakeMissing(String path, Collection<String> keys, String store) {
        return "the store " + store + " has no " + keys + " for " + path + ": run generate " + GENERATE_STORE +
                " (it keeps the existing secrets and adds the missing ones).";
    }

    public static String getBakeNodesChanged(String store) {
        return "the nodes or the files they read changed since the values in " + store + "/" + DIR_ENCRYPTED +
                " were encrypted (" + DIR_NODES + "/*.pub, the launcher's blocks, hosts or configs, or a new " +
                "configuration file): run generate " + GENERATE_STORE + " " + GENERATE_ENCRYPT + " again.";
    }

    public static String getBakeUnmapped(String block, Collection<String> deployables) {
        return "[Warning] block '" + block + "': no configuration files (launcher configs) for " + deployables + " ..";
    }

    public static String getKeygenDescription() {
        return "writes a new key for this node (" + PATH_NODE_KEY + ", or " + ENV_NODE_KEY + ") and prints its " +
                "public key; " + KEYGEN_FORCE + " replaces an existing key.";
    }

    public static String getBakeDescription() {
        return "writes the configuration files with the secrets of a store: " + GENERATE_STORE + " <dir> (none: " +
                "without secrets), " + BAKE_OUT + " <dir> (default " + BAKE_OUT_DEFAULT + "), " + BAKE_BLOCKS +
                " a,b (the files of each block, default every block).";
    }

    public static String getInvalidKey(String reason) {
        return "Invalid key for a key pair token factory: " + reason;
    }

    public static String getTokenFactoryVerifyOnly() {
        return "This token factory has no private key: it verifies tokens and cannot sign them.";
    }

    public static String getTokenFactoryNoSecret() {
        return "This token factory has no secret: a key pair factory signs with sign(token), not hmac.";
    }

    public static String getPreshareConflict(String key, Collection<String> paths) {
        return "[Warning] preshared key '" + key + "' differs between files, keeping the most common value: " +
                "replaced in " + paths + " ..";
    }

    public static String getGeneratingShared(String key, String path) {
        return "[Generated] preshared key '" + key + "' for " + path + " ..";
    }

    public static String getService(String config) {
        return CoreStrings.DIR_SERVICES + config + EXT_YAML;
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
        return "[" + name + "] Error: '" + ID_NODE + "' must be configured for token generation.";
    }

    public static String getStorageLoaderError(String plugin, String database, String collection) {
        return String.format(
                "Error: Failed to load storage plugin '%s' for '%s%s%s'.",
                plugin,
                database,
                DIR_SEPARATOR,
                collection
        );
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

    public static String getDeployDescription() {
        return "deploys the services in the given block. example: deploy blockName";
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
        return "generates the missing secrets, preshared keys, key pairs and tokens and keeps existing ones " +
                "(reissues tokens that are invalid or expire soon). " + GENERATE_ROTATE + ": new values for all " +
                "of them, the old ones are not kept: every token issued before is rejected (restart the services). " +
                GENERATE_STORE + " <dir>: the values of a secrets store, not of the configuration. " + GENERATE_ENCRYPT +
                ": encrypted (enc:) to the nodes that read each file (" + GENERATE_NODES + " <dir>, <host>.pub).";
    }

    public static String getCommandExecutorHelpDescription() {
        return "prints this help text.";
    }

    public static String getBenchmarkDescription() {
        return "Executes benchmarks. [--iterations ?, --html, --suite maps|protocol|all]";
    }

    public static String getUnknownBenchmarkSuite(String suite) {
        return String.format("Unknown benchmark suite '%s', expected %s, %s or %s.", suite,
                SUITE_MAPS, SUITE_PROTOCOL, SUITE_ALL);
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

    public static String getBenchmarkProgressUpdate(BenchmarkImplementation implementation, Benchmark benchmark, String progress) {
        return "Tests for " + implementation.getName() + "::" + benchmark.getName() +
                " " + progress + "%";
    }

    public static String getBenchmarkReportSaved(String path) {
        return "Benchmark report saved to " + path;
    }

    public static String getBenchmarkCompleted(BenchmarkImplementation implementation, Benchmark benchmark) {
        return "Completed benchmark " + implementation.getName() +
                "::" + benchmark.getName() + " in " + benchmark.getTimeFormatted() + ".";
    }

    public static String getIllegalTemplateTokenCount(String token, int count) {
        return "template must accept " + count + " tokens ('" + token + "s').";
    }

    public static String formatAsPercent(Double value) {
        return new DecimalFormat("#.00").format(value);
    }

    public static String getMissingEntity(String key) {
        return "Error: could not get '" + key + "' in storage.";
    }

    public static String getKeystoreAutocreate(String alias) {
        return String.format("Keystore '%s' not configured: generating self signed certificate.", alias);
    }

    public static String getMissingKeyStore(String alias) {
        return String.format("Keystore '%s' not configured.", alias);
    }

    public static String getEmptyKeyStore(String keystorePath) {
        return String.format("Could not find any keys in keystore at '%s'.", keystorePath);
    }

    public static String getSecurityDependencyMissing(String target, String identifier) {
        return "Error: missing security identifier '" + identifier +
                "' in service configuration for '" + target + "'.";
    }

    public static String timestamp(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.systemDefault())
                .toString().split("T")[1];
    }

    public static String getHashMismatchException() {
        return "Error: hash comparison has failed.";
    }

    public static String getHashingFailed() {
        return "Error: hash operation failed.";
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
        if (Paths.get(relative).isAbsolute()) {
            // an absolute path (a secrets store outside the application) is used as it is.
            return Paths.get(relative).normalize().toString();
        }
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

    public static String getKeystorePrompt(KeyStoreReference store) {
        return "Enter password for '" + store.getPath() + "': ";
    }

    public static String getValueByPathContainsNull(String field, String[] fields) {
        return String.format("Value at '%s' in path '%s' is null.", field, Arrays.stream(fields)
                .collect(Collectors.joining()));
    }

    public static String getReflectionErrorInSerializer(String path) {
        return String.format("Reflection failed to retrieve value at '%s'.", path);
    }

    public static String getRestNotSupportedByWebsocketListener() {
        return "REST Requests are not supported by the websocket listener.";
    }

    public static String getKeystoreTooManyEntries(String path) {
        return String.format("Keystore at '%s' contains too many entries, only one entry supported.", path);
    }

    public static String pad(String text, int spaces) {
        int padding = spaces - text.length();
        if (padding > 0) {
            return text + String.join("", Collections.nCopies(padding, " "));
        } else {
            return text;
        }
    }

    public static String leftPad(String text, int spaces) {
        return pad("", spaces) + text;
    }

    private enum IPVersion {IP4, IP6;}
}
