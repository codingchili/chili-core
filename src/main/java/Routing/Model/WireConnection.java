package Routing.Model;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
interface WireConnection {
    void write(JsonObject data);
    void write(Buffer buffer);
}
