package Game.Controller;

import Game.Model.Connection;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         Handles packets from clients.
 */
interface ClientPacketHandler {
    /**
     * @param connection the connection id for writing to.
     * @param packet the raw data.
     */
    void handle(Connection connection, String packet);
}
