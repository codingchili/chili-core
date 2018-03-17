package com.codingchili.common;

import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 *         <p>
 *         Override of strings in core and adds some new service-specific.
 */
public abstract class Strings extends CoreStrings {
    public static final String PATH_REALM = "conf/realm/";
    public static final String PATH_INSTANCE = "conf/game/instances/";
    public static final String PATH_GAME_CLASSES = "conf/game/class/";
    public static final String PATH_GAME_AFFLICTIONS = "conf/game/player/affliction.json";
    public static final String PATH_GAME_PLAYERTEMPLATE = "conf/game/player/character.json";
    public static final String PATH_GAME_OVERRIDE = "conf/realm/override/";
    public static final String PATH_GAME = "conf/game";

    public static final String DIR_WEBSITE = "website";
    public static final String DIR_RESOURCES = "resources";
    public static final String DIR_RESOURCES_QUOTED = "/resources/";

    public static final String EXT_JSON = ".json";

    public static final String CLIENT_CHARACTER_LIST = "character.list";
    public static final String CLIENT_CHARACTER_CREATE = "character.create";
    public static final String CLIENT_CHARACTER_REMOVE = "character.remove";
    public static final String CLIENT_INSTANCE_JOIN = "instance.join";
    public static final String CLIENT_INSTANCE_LEAVE = "instance.leave";
    public static final String CLIENT_AUTHENTICATE = "authenticate";
    public static final String CLIENT_REGISTER = "register";
    public static final String CLIENT_REALM_TOKEN = "realmtoken";
    public static final String CLIENT_REALM_LIST = "realmlist";
    public static final String CLIENT_CLOSE = "close";

    public static final String REALM_UPDATE = "realm.update";

    public static final String ID_CHARACTER = "character";
    public static final String ID_CHARACTERS = "characters";
    public static final String ID_PLAYERCLASS = "className";
    public static final String ID_ACCOUNT = "account";
    public static final String ID_REALM = "realm";
    public static final String ID_REALMS = "realms";
    public static final String ID_PLAYERS = "players";
    public static final String ID_RESOURCES = "resources";
    public static final String ID_AFFLICTIONS = "afflictions";
    public static final String ID_CLASSES = "classes";
    public static final String ID_TEMPLATE = "template";
    public static final String ID_AUTHENTICATION = "authentication";
    public static final String ID_LIST = "list";
    public static final String ID_FILES = "files";
    public static final String ID_DATE = "date";
    public static final String ID_CHANGES = "changes";
    public static final String ID_CONTENT = "content";
    public static final String ID_TITLE = "title";
    public static final String ID_LICENSE = "license";
    public static final String ID_DESCRIPTION = "description";
    public static final String ID_INSTANCE = "instance";
    public static final String ID_INSTANCES = "instances";

    public static final String NODE_AUTHENTICATION_CLIENTS = "client.authentication.node";
    public static final String NODE_AUTHENTICATION_REALMS = "realm.authentication.node";
    public static final String NODE_REALM_CLIENTS = "client.realmregistry.node";
    public static final String NODE_WEBSERVER = "webserver.node";
    public static final String NODE_PATCHING = "patching.node";
    public static final String NODE_REALM = "realm.node";
    public static final String NODE_CLIENT_LOGGING = "client.logging.node";
    public static final String NODE_ROUTER = "routing.node";
    public static final String SOCIAL_NODE = "social.node";

    public static final String COLLECTION_REALMS = "realms";
    public static final String COLLECTION_ACCOUNTS = "accounts";
    public static final String COLLECTION_CHARACTERS = "characters";

    public static final String LOG_VERSION = "version";
    public static final String LOG_USER_AGENT = "USER-Agent";
    public static final String LOG_AGENT = "agent";
    public static final String LOG_REMOTE = "remote";
    public static final String LOG_INSTANCE = "instance";
    public static final String LOG_INSTANCE_START = "instance.start";
    public static final String LOG_INSTANCE_STOP = "instance.stop";
    public static final String LOG_REALM_START = "realm.start";
    public static final String LOG_REALM_STOP = "realm.stop";
    public static final String LOG_MESSAGE = "message";
    public static final String LOG_ACCOUNT_UNAUTHORIZED = "account.unauthorized";
    public static final String LOG_ACCOUNT_AUTHENTICATED = "account.authenticated";
    public static final String LOG_ACCOUNT_REGISTERED = "account.registered";
    public static final String LOG_REALM_REGISTERED = "realm.registered";
    public static final String LOG_REALM_DISCONNECT = "realm.disconnected";
    public static final String LOG_REALM_UPDATE = "realm.update";
    public static final String LOG_REALM_REJECTED = "realm.rejected";
    public static final String LOG_PAGE_LOAD = "page.load";
    public static final String LOG_PATCHER_LOADED = "patcher.loaded";
    public static final String LOG_REALM_DEPLOY_ERROR = "realm.deploy.error";
    public static final String LOG_INSTANCE_DEPLOY_ERROR = "instance.deploy.error";
    public static final String ID_PIECE = "piece";
    public static final String LOG_USER = "user";

    //patching
    public static final String PATCH_IDENTIFIER = "patch";
    public static final String PATCH_GAME_INFO = "gameinfo";
    public static final String PATCH_NEWS = "news";
    public static final String PATCH_DOWNLOAD = "download";
    public static final String PATCH_DATA = "patchdata";
    public static final String PATCH_WEBSEED = "webseed";

    // Error messages.
    public static final String ERROR_REALM_UPDATE = "Failed to update realm-list.";
    public static final String ERROR_REALM_DISCONNECT = "Error disconnecting realm.";
    public static final String ERROR_REALM_MISSING = "The requested realm is missing.";

    public static String getCharacterExistsError(String name) {
        return "Character " + name + " already exists.";
    }

    public static String getCharacterMissingError(String character) {
        return "Character " + character + " does not exist.";
    }
}
