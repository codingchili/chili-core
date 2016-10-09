package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 *         <p>
 *         Store json keys and protocol headers so that they are easily maintained.
 */
public class Strings {
    // Locations to configuration files.
    public static final String PATH_AUTHSERVER = "conf/service/authserver.json";
    public static final String PATH_LOGSERVER = "conf/service/logserver.json";
    public static final String PATH_PATCHSERVER = "conf/service/patchserver.json";
    public static final String PATH_REALMSERVER = "conf/service/realmserver.json";
    public static final String PATH_WEBSERVER = "conf/service/webserver.json";
    public static final String PATH_ROUTING = "conf/service/routingserver.json";

    public static final String PATH_LAUNCHER = "conf/system/launcher.json";
    public static final String PATH_DEPLOY = "conf/system/deployment.json";
    public static final String PATH_VERTX = "conf/system/vertx.json";
    public static final String PATH_REALM = "conf/realm/";
    public static final String PATH_INSTANCE = "conf/game/instances";
    public static final String PATH_GAME_CLASSES = "conf/game/class";
    public static final String PATH_GAME_AFFLICTIONS = "conf/game/player/affliction.json";
    public static final String PATH_GAME_PLAYERTEMPLATE = "conf/game/player/character.json";
    public static final String PATH_GAME_OVERRIDE = "conf/realm/override/";
    public static final String PATH_GAME = "conf/game";

    public static final String DIR_RESOURCES = "resources";
    public static final String DIR_WEBSITE = "website";
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
    public static final String ANY = "*";

    // Protocol attributes.
    public static final String PROTOCOL_CLASS_NAME = "className";
    public static final String PROTOCOL_REAL_IP = "X-Real-IP";
    public static final String PROTOCOL_ACCEPTED = "accepted";
    public static final String PROTOCOL_CONNECTION = "connection";
    public static final String PROTOCOL_ERROR = "error";
    public static final String PROTOCOL_STATUS = "status";
    public static final String PROTOCOL_AUTHENTICATION = "authentication";
    public static final String PROTOCOL_CONFLICT = "conflict";
    public static final String PROTOCOL_UNAUTHORIZED = "unauthorized";
    public static final String PROTOCOL_MISSING = "missing";
    public static final String PROTOCOL_ACTION = "action";
    public static final String PROTOCOL_LOGGING = "logging";

    // General shared attributes.
    public static final String ID_CHARACTER = "character";
    public static final String ID_CHARACTERS = "characters";
    public static final String ID_ACCOUNT = "account";
    public static final String ID_TOKEN = "token";
    public static final String ID_REALM = "realm";
    public static final String ID_REALMS = "realms";
    public static final String ID_NAME = "name";
    public static final String ID_PLAYERS = "players";
    public static final String ID_FILE = "file";
    public static final String ID_RESOURCES = "resources";
    public static final String ID_DATA = "data";
    public static final String ID_INSTANCE = "instance";
    public static final String ID_MESSAGE = "message";
    public static final String ID_ERROR = "error";
    public static final String ID_ACTION = "action";
    public static final String ID_TARGET = "target";
    public static final String ID_VERSION = "version";
    public static final String ID_BYTES = "bytes";
    public static final String ID_PATH = "path";
    public static final String ID_MODIFIED = "modified";
    public static final String ID_SIZE = "size";
    public static final String ID_DOMAIN = "domain";
    public static final String ID_AFFLICTIONS = "afflictions";
    public static final String ID_CLASSES = "classes";
    public static final String ID_TEMPLATE = "template";
    public static final String ID_PING = "ping";
    public static final String ID_CLASS = "className";
    public static final String ID_AUTHENTICATION = "authentication";
    public static final String ID_LIST = "list";
    public static final String ID_FILES = "files";
    public static final String ID_DATE = "date";
    public static final String ID_CHANGES = "changes";
    public static final String ID_CONTENT = "content";
    public static final String ID_TITLE = "title";
    public static final String ID_LICENSE = "license";


    //Database naming.
    public static final String DB_ACCOUNTS = "accounts";
    public static final String DB_USER = "username";
    public static final String DB_SALT = "salt";
    public static final String DB_HASH = "hash";
    public static final String DB_CHARACTERS = "characters";

    // Cluster addressing.
    public static final String NODE_AUTHENTICATION_CLIENTS = "client.authentication.node";
    public static final String NODE_AUTHENTICATION_REALMS = "realm.authentication.node";
    public static final String NODE_WEBSERVER = "webserver.node";
    public static final String NODE_PATCHING = "patching.node";
    public static final String NODE_REALM = ".realm.node";
    public static final String NODE_LOGGING = "logging.node";
    public static final String NODE_ROUTING = "routing.node";

    public static final String LOCAL_LOGGING = "logging.local";

    public static final String VERTICLE_LOGGING = "com.codingchili.core.Logging.Server";
    public static final String VERTICLE_AUTHENTICATION = "com.codingchili.core.Authentication.Server";
    public static final String VERTICLE_PATCHING = "com.codingchili.core.Patching.Server";
    public static final String VERTICLE_ROUTING = "com.codingchili.core.Routing.Server";
    public static final String VERTICLE_WEBSERVER = "com.codingchili.core.Website.Server";
    public static final String VERTICLE_REALM = "com.codingchili.core.Realm.Server";
    public static final String VERTICLE_ALL = "all";

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
    public static final String LOG_INSTANCE_STOP = "instance.stop";
    public static final String LOG_REALM_START = "realm.start";
    public static final String LOG_REALM_STOP = "realm.stop";
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
    public static final String LOG_REALM_DEPLOY_ERROR = "realm.deploy.error";

    //Patching
    public static final String PATCH_IDENTIFIER = "patch";
    public static final String PATCH_GAME_INFO = "gameinfo";
    public static final String PATCH_NEWS = "news";
    public static final String PATCH_DOWNLOAD = "download";
    public static final String PATCH_DATA = "patchdata";

    // Error messages.
    public static final String ERROR_TOKEN_FACTORY = "Token factory failed to generate token.";
    public static final String ERROR_CLUSTERING_REQUIRED = "Clustering required but not enabled.";
    public static final String ERROR_IN_ADDRESS = "The requested node was not found in the cluster.";
    public static final String ERROR_NOT_AUTHORIZED = "The requested resource requires authorization.";
    public static final String ERROR_HANDLER_MISSING = "The requested handler was not found.";
    public static final String ERROR_HANDLER_MISSING_AUTHENTICATOR = "The handler is missing an @Authenticator.";
    public static final String ERROR_LAUNCHER_STARTUP = "Failed to start the launcher with clustering.";
    public static final String ERRROR_LAUNCHER_SHUTDOWN = "System has been shut down..";
    public static final String ERROR_REALM_DEPLOYMENT_FAILED = "Failed to deploy %realm%, already deployed in cluster.";
}
