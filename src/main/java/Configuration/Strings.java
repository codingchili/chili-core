package Configuration;

/**
 * @author Robin Duda
 *         <p>
 *         Store json keys and protocol headers so that they are easily maintained.
 */
public class Strings {
    // Locations to configuration files.
    public static final String PATH_AUTHSERVER = "conf/system/authserver.json";
    public static final String PATH_LOGSERVER = "conf/system/logserver.json";
    public static final String PATH_PATCHSERVER = "conf/system/patchserver.json";
    public static final String PATH_GAMESERVER = "conf/system/realmserver.json";
    public static final String PATH_WEBSERVER = "conf/system/webserver.json";
    public static final String PATH_ROUTING = "conf/system/routingserver.json";
    public static final String PATH_VERTX = "conf/system/vertx.json";
    public static final String PATH_REALM = "conf/realm/";
    public static final String PATH_INSTANCE = "conf/game/world";
    public static final String PATH_CLASSES = "conf/game/class";
    public static final String PATH_AFFLICTIONS = "conf/game/player/affliction.json";
    public static final String PATH_PLAYER_TEMPLATE = "conf/game/player/character.json";

    public static final String DIR_RESOURCES = "resources/";
    public static final String DIR_SYSTEM = "conf/system/";
    public static final String DIR_SEPARATOR = "/";

    // File extensions.
    public static final String EXT_JSON = ".json";
    public static final String EXT_SVG = ".svg";

    // Protocol identifiers for clients.
    public static final String CLIENT_CHARACTER_LIST = "character.list";
    public static final String CLIENT_CHARACTER_CREATE = "character.create";
    public static final String CLIENT_CHARACTER_REMOVE = "character.remove";
    public static final String CLIENT_AUTHENTICATE = "authenticate";
    public static final String CLIENT_REGISTER = "register";
    public static final String CLIENT_REALM_TOKEN = "realmtoken";
    public static final String CLIENT_REALM_LIST = "realmlist";
    public static final String CLIENT_CLOSE = "close";

    // Protocol identifiers for realms.
    public static final String REALM_AUTHENTICATE = "realm.authenticate";
    public static final String REALM_REGISTER = "realm.register";
    public static final String REALM_AUTHENTICATION_RESULT = "authentication.result";
    public static final String REALM_CHARACTER_REQUEST = "character.request";
    public static final String REALM_CHARACTER_RESPONSE = "character.response";
    public static final String REALM_UPDATE = "realm.update";


    // Protocol attributes.
    public static final String PROTOCOL_CLASS_NAME = "className";
    public static final String PROTOCOL_REAL_IP = "X-Real-IP";
    public static final String PROTOCOL_ACCEPTED = "accepted";
    public static final String PROTOCOL_CONNECTION = "connection";
    public static final String PROTOCOL_ERROR = "error";
    public static final String PROTOCOL_AUTHENTICATION = "authentication";
    public static final String PROTOCOL_CONFLICT = "conflict";
    public static final String PROTOCOL_UNAUTHORIZED = "unauthorized";
    public static final String PROTOCOL_MISSING = "missing";
    public static final String PROTOCOL_ACTION = "action";
    public static final String PROTOCOL_LOGGING = "logging";

    // General shared attributes.
    public static final String ID_CHARACTER = "character";
    public static final String ID_ACCOUNT = "account";
    public static final String ID_TOKEN = "token";
    public static final String ID_REALM = "realm";
    public static final String ID_NAME = "name";
    public static final String ID_PLAYERS = "players";
    public static final String ID_FILE = "file";
    public static final String ID_RESOURCES = "resources";
    public static final String ID_DATA = "data";
    public static final String ID_INSTANCE = "instance";
    public static final String ID_MESSAGE = "message";
    public static final String ID_ERROR = "error";
    public static final String ID_ACTION = "action";



    //Database naming.
    public static final String DB_COLLECTION = "accounts";
    public static final String DB_USER = "username";
    public static final String DB_SALT = "salt";
    public static final String DB_HASH = "hash";
    public static final String DB_CHARACTERS = "characters";

    // Cluster addressing.
    public static final String ADDRESS_AUTHENTICATION_CLIENTS = "clients.authentication.node";
    public static final String ADDRESS_AUTHENTICATION_REALMS = "realms.authentication.node";
    public static final String ADDRESS_WEBSERVER = "webserver.node";
    public static final String ADDRESS_PATCHING = "patching.node";
    public static final String ADDRESS_LOGGING = "logging.node";
    public static final String ADDRESS_REALM = "realm.node";

    public static final String LOCAL_LOGGING = "logging.local";


    // Routing
    public static final String ROUTER_MISSING_MAP = "router.missing";

    // Shared memory maps.
    public static final String MAP_REALMS = "realms";
    public static final String MAP_ACCOUNTS = "accounts";

    // Logging
    public static final String LOG_ID = "LOG_ID";
    public static final String LOG_AT = "@";
    public static final String LOG_EVENT = "event";
    public static final String LOG_VERSION = "version";
    public static final String LOG_USER_AGENT = "user-agent";
    public static final String LOG_AGENT = "agent";
    public static final String LOG_ORIGIN = "origin";
    public static final String LOG_REMOTE = "remote";
    public static final String LOG_INSTANCE = "instance";
    public static final String LOG_TIME = "time";
    public static final String LOG_SYSTEM = "system";
    public static final String LOG_HOST = "host";
    public static final String LOG_SERVER_START = "server.start";
    public static final String LOG_SERVER_STOP = "server.stop";
    public static final String LOG_INSTANCE_START = "instance.start";
    public static final String LOG_REALM_START = "realm.start";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_FILE_ERROR = "file.error";
    public static final String LOG_ACCOUNT_UNAUTHORIZED = "account.unauthorized";
    public static final String LOG_ACCOUNT_AUTHENTICATED = "account.authenticated";
    public static final String LOG_ACCOUNT_REGISTERED = "account.registered";
    public static final String LOG_REALM_REGISTERED = "realm.registered";
    public static final String LOG_REALM_DISCONNECT = "realm.disconnected";
    public static final String LOG_REALM_UPDATE = "realm.update";
    public static final String LOG_REALM_REJECTED = "realm.rejected";
    public static final String LOG_PAGE_LOAD = "page.load";
    public static final String LOG_PATCHER_RELOAD = "patcher.reload";
    public static final String LOG_PATCHER_RELOADED = "patcher.reloaded";
    public static final String LOG_PATCHER_LOADED = "patcher.loaded";
    public static final String LOG_DATABASE_ERROR = "database.error";
    public static final String LOG_CONNECTION_ERROR = "connection.error";
    public static final String LOG_LEVEL = "level";
    public static final String LOG_LEVEL_SEVERE = "SEVERE";
    public static final String LOG_LEVEL_WARNING = "WARNING";
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_STARTUP = "STARTUP";
    public static final String LOG_VERTX = "vertx";
    public static final String LOG_METRICS = "metrics";
    public static final String LOG_TRACE = "trace";
    public static final String LOG_HANDLER_MISSING = "handler.missing";


    //Patching
    public static final String PATCH_IDENTIFIER = "patch";
    public static final String PATCH_GAME_INFO = "gameinfo";
    public static final String PATCH_NEWS = "news";
    public static final String PATCH_AUTHSERVER = "authserver";
    public static final String PATCH_DOWNLOAD = "download";
    public static final String PATCH_DATA = "patchdata";
    public static final String PATCH_MAX_VERSION = "9999.999.999";

    // Game
    public static final String GAME_AFFLICTIONS = "afflictions";
    public static final String GAME_CLASSES = "classes";

    // Error messages.
    public static final String ERROR_TOKEN_FACTORY = "Token factory failed to generate token.";
    public static final String ERROR_CLUSTERING_REQUIRED = "Clustering required but not enabled.";
    public static final String ERROR_IN_ADDRESS = "The requested node was not found in the cluster.";
    public static final String ERROR_NOT_AUTHORIZED = "The requested resource requires authorization.";
    public static final String ERROR_HANDLER_MISSING = "The requested handler was not found.";
    public static final String ERROR_HANDLER_MISSING_AUTHENTICATOR = "The handler is missing an @Authenticator.";
}
