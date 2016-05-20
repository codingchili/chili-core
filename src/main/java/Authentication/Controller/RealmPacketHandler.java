package Authentication.Controller;

import io.vertx.core.http.ServerWebSocket;

/**
 * @author Robin Duda
 *         Template pattern method for handling realm packets.
 */
public interface RealmPacketHandler {
    void handle(ServerWebSocket connection, String data);
}
