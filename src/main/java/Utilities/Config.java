package Utilities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by Robin on 2016-04-07.
 */

public class Config {
    private static boolean loaded = false;

    public static void Load() {
        if (!loaded)
            try {
                Authentication.Load(JsonFileReader.readObject("conf/system/authserver.json"));
                Web.Load(JsonFileReader.readObject("conf/system/webserver.json"));
                Logging.Load(JsonFileReader.readObject("conf/system/logserver.json"));
                Gameserver.Load(JsonFileReader.readObject("conf/system/gameserver.json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        loaded = true;
    }

    public static class Authentication {
        public static byte[] CLIENT_SECRET;
        public static byte[] REALM_SECRET;
        public static Integer REALM_PORT;
        public static Integer CLIENT_PORT;
        public static JsonObject DATABASE;
        public static RemoteAuthentication LOGTOKEN;
        private static JsonArray REALMS;

        public static boolean isPublicRealm(String name) {
            boolean result = false;

            for (Object object : REALMS.getList()) {
                if (object.equals(name))
                    result = true;
            }

            return result;
        }

        public static void Load(JsonObject configuration) {
            REALM_PORT = configuration.getInteger("realm.port");
            CLIENT_PORT = configuration.getInteger("client.port");
            CLIENT_SECRET = configuration.getString("client.secret").getBytes();
            REALM_SECRET = configuration.getString("realm.secret").getBytes();
            REALMS = configuration.getJsonArray("realms");
            LOGTOKEN = LoadLogToken(configuration);
            DATABASE = configuration.getJsonObject("database");
        }
    }

    private static RemoteAuthentication LoadLogToken(JsonObject configuration) {
        return (RemoteAuthentication) Serializer.unpack(configuration.getJsonObject("logserver"), RemoteAuthentication.class);
    }

    public static class Web {
        public static Integer PORT;
        public static RemoteAuthentication LOGTOKEN;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            LOGTOKEN = LoadLogToken(configuration);
        }
    }

    public static class Gameserver {
        public static RemoteAuthentication LOGTOKEN;

        public static void Load(JsonObject configuration) {
            LOGTOKEN = LoadLogToken(configuration);
        }
    }

    public static class Logging {
        public static byte[] SECRET;
        public static Integer PORT;
        public static RemoteAuthentication LOGTOKEN;
        public static Integer ES_PORT;
        public static String ES_REMOTE;
        public static String ES_INDEX;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            SECRET = configuration.getString("secret").getBytes();
            LOGTOKEN = LoadLogToken(configuration);

            JsonObject elastic = configuration.getJsonObject("elastic");
            ES_PORT = elastic.getInteger("port");
            ES_REMOTE = elastic.getString("remote");
            ES_INDEX = elastic.getString("index");
        }
    }

    public static class Address {
        public static final String LOGS = "LOGGING";
    }
}
