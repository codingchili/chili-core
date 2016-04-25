package Utilities;

import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-07.
 */
public interface Logger {
    void log(JsonObject message);
}
