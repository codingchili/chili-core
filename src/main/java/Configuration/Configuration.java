package Configuration;

import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-07.
 */
public class Configuration {
    private static JsonObject json;

    public static void Load(JsonObject configuration) {
        json = configuration;
        Authentication.Load(configuration.getJsonObject("authentication.server"));
        Web.Load(configuration.getJsonObject("web.server"));
        Game.Load(configuration.getJsonObject("game.server"));
        Database.Load(configuration.getJsonObject("database"));
        Security.Load(configuration.getJsonObject("security"));
    }

    public static JsonObject getSource() {
        return json;
    }


    public static class Authentication {
        public static Integer PORT;

        public static void Load(JsonObject configuration) {
            PORT = configuration.getInteger("port");
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

    public static class Database {
        public static JsonObject configuration;

        public static void Load(JsonObject configuration) {
            Database.configuration = configuration;
        }
    }

    public static class Address {
        public static final String LOGS = "LOGGING";
    }

    public static class Security {
        public static byte[] secret;

        public static void Load(JsonObject configuration) {
            secret = configuration.getString("secret").getBytes();
        }
    }
}
