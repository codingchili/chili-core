package Configuration;

import Utilities.JsonFileReader;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

/**
 * Created by Robin on 2016-04-07.
 */

public class Config {
   // private static volatile boolean loaded = false;

    public static void Load() {
       // if (!loaded)
            try {
                Authentication.Load(JsonFileReader.readObject("conf/system/authserver.json"));
                Web.Load(JsonFileReader.readObject("conf/system/webserver.json"));
                Logging.Load(JsonFileReader.readObject("conf/system/logserver.json"));
                Database.Load(JsonFileReader.readObject("conf/system/database.json"));
                Gameserver.Load(JsonFileReader.readObject("conf/system/gameserver.json"));
            //    loaded = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public static class Authentication {
        public static byte[] CLIENT_SECRET;
        public static byte[] SERVER_SECRET;
        public static Integer REALM_PORT;
        public static Integer CLIENT_PORT;
        public static String REMOTE;

        public static void Load(JsonObject configuration) {
            REALM_PORT = configuration.getInteger("realm.port");
            CLIENT_PORT = configuration.getInteger("client.port");
            CLIENT_SECRET = configuration.getString("client.secret").getBytes();
            SERVER_SECRET = configuration.getString("server.secret").getBytes();
            REMOTE = configuration.getString("remote");
        }
    }

    public static class Web {
        public static Integer PORT;
        public static byte[] SECRET;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            SECRET = configuration.getString("secret").getBytes();
        }
    }

    public static class Gameserver {
        public static Integer PORT;
        public static byte[] SECRET;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            SECRET = configuration.getString("secret").getBytes();
        }
    }

    public static class Logging {
        public static byte[] SECRET;
        public static Integer PORT;
        public static String NAME;
        public static String REMOTE;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            NAME = configuration.getString("name");
            REMOTE = configuration.getString("remote");
            SECRET = configuration.getString("secret").getBytes();
        }
    }

    public static class Database {
        public static JsonObject CONFIGURATION;

        public static void Load(JsonObject configuration) {
            Database.CONFIGURATION = configuration;
        }
    }

    public static class Address {
        public static final String LOGS = "LOGGING";
    }
}
