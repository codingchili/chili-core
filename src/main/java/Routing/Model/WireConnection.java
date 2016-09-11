package Routing.Model;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
@FunctionalInterface
public interface WireConnection {
    void write(JsonObject data);
}
