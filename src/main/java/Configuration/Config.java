package Configuration;

import Utilities.JsonFileReader;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

/**
 * Created by Robin on 2016-04-07.
 */

public class Config {
    private static JsonObject json;

    public static void Load() {
        try {
            Authentication.Load(JsonFileReader.readObject("conf/authserver.json"));
            Web.Load(JsonFileReader.readObject("conf/webserver.json"));
            Logging.Load(JsonFileReader.readObject("conf/logserver.json"));
            Database.Load(JsonFileReader.readObject("conf/database.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Authentication {
        public static byte[] SECRET;
        public static Integer PORT;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            SECRET = configuration.getString("secret").getBytes();
        }
    }

    public static class Web {
        public static Integer PORT;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
        }
    }

    public static class Game {
        public static Integer PORT;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
        }
    }

    public static class Logging {
        public static Integer PORT;
        public static String NAME;
        public static String REMOTE;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
            NAME = configuration.getString("name");
            REMOTE = configuration.getString("remote");
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
